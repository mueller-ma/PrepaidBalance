package com.github.muellerma.prepaidbalance.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationBuilderWithBuilderAccessor
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.room.BalanceEntry
import com.github.muellerma.prepaidbalance.utils.NotificationUtils
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.CHANNEL_ID_BALANCE_INCREASED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.CHANNEL_ID_ERROR
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.CHANNEL_ID_THRESHOLD_REACHED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.NOTIFICATION_ID_BALANCE_INCREASED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.NOTIFICATION_ID_THRESHOLD_REACHED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.getBaseNotification
import com.github.muellerma.prepaidbalance.utils.ResponseParser
import com.github.muellerma.prepaidbalance.utils.formatAsCurrency
import com.github.muellerma.prepaidbalance.utils.isValidUssdCode
import kotlinx.coroutines.*

class CheckBalanceWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams)
{
    override fun doWork(): Result {
            checkBalance(applicationContext) { result ->
                @StringRes val errorMessage = when (result) {
                    CheckResult.USSD_FAILED -> R.string.ussd_failed
                    CheckResult.MISSING_PERMISSIONS -> R.string.permissions_required
                    CheckResult.PARSER_FAILED -> R.string.unable_get_balance
                    CheckResult.USSD_INVALID -> R.string.invalid_ussd_code
                    CheckResult.OK -> null
                }

                errorMessage?.let {
                    val notification = getBaseNotification(context, CHANNEL_ID_ERROR)
                        .setContentTitle(context.getString(errorMessage))

                    NotificationUtils
                        .manager(context)
                        .notify(NotificationUtils.NOTIFICATION_ID_ERROR, notification.build())
                }
            }

        return Result.success()
    }

    companion object {
        private val TAG = CheckBalanceWorker::class.java.simpleName

        fun checkBalance(context: Context, callback: (CheckResult)->Unit) {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase
                    .get(context)
                    .balanceDao()
                    .deleteBefore(System.currentTimeMillis() - 6L * 30 * 24 * 60 * 60 * 1000) // 6 months
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return callback(CheckResult.MISSING_PERMISSIONS)
            }

            val ussdResponseCallback = object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager?,
                    request: String?,
                    response: CharSequence?
                ) {
                    Log.d(TAG, "onReceiveUssdResponse($response)")
                    val balance = ResponseParser.getBalance(response as String?)
                        ?: return callback(CheckResult.PARSER_FAILED)

                    handleNewBalance(context, balance, callback)
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager?,
                    request: String?,
                    failureCode: Int
                ) {
                    Log.d(TAG, "onReceiveUssdResponseFailed($failureCode)")
                    return callback(CheckResult.USSD_FAILED)
                }
            }

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val ussdCode = prefs.getString("ussd_code", "").orEmpty()
            if (!ussdCode.isValidUssdCode()) {
                return callback(CheckResult.USSD_INVALID)
            }

            context.getSystemService(TelephonyManager::class.java)
                .sendUssdRequest(
                    ussdCode,
                    ussdResponseCallback,
                    Handler(Looper.getMainLooper())
                )
        }

        private fun handleNewBalance(context: Context, balance: Double, callback: (CheckResult) -> Unit) =
            GlobalScope.launch(Dispatchers.IO) {
                val database = AppDatabase.get(context)

                val latestInDb = database.balanceDao().getLatest()
                val new = BalanceEntry(timestamp = System.currentTimeMillis(), balance = balance)

                database
                    .balanceDao()
                    .insert(new)

                if (latestInDb?.nearlyEquals(new) == true) {
                    Log.d(TAG, "Remove $latestInDb from db")
                    database.balanceDao().delete(latestInDb)
                }

                callback(CheckResult.OK)

                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                if (prefs.getBoolean("notify_balance_increased", false) &&
                    latestInDb != null &&
                    new.balance > latestInDb.balance
                ) {
                    val diff = new.balance - latestInDb.balance
                    Log.d(TAG, "New balance is larger: $diff")

                    NotificationUtils.createChannels(context)

                    val notification = getBaseNotification(context, CHANNEL_ID_BALANCE_INCREASED)
                        .setContentTitle(context.getString(R.string.balance_increased, diff.formatAsCurrency()))

                    NotificationUtils
                        .manager(context)
                        .notify(NOTIFICATION_ID_BALANCE_INCREASED, notification.build())
                }

                val threshold = try {
                    prefs
                        .getString("notify_balance_under_threshold_value", "")
                        ?.replace(',', '.')
                        ?.toDouble() ?: Double.MAX_VALUE
                } catch (e: Exception) {
                    Double.MAX_VALUE
                }
                if (
                    prefs.getBoolean("notify_balance_under_threshold", false) &&
                    new.balance < threshold
                ) {
                    NotificationUtils.createChannels(context)

                    val notification = getBaseNotification(context, CHANNEL_ID_THRESHOLD_REACHED)
                        .setContentTitle(context.getString(R.string.threshold_reached, new.balance.formatAsCurrency()))

                    NotificationUtils
                        .manager(context)
                        .notify(NOTIFICATION_ID_THRESHOLD_REACHED, notification.build())
                }
            }

        enum class CheckResult {
            OK,
            MISSING_PERMISSIONS,
            PARSER_FAILED,
            USSD_FAILED,
            USSD_INVALID
        }
    }
}