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
import androidx.core.content.edit
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.room.BalanceEntry
import com.github.muellerma.prepaidbalance.utils.*
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.CHANNEL_ID_BALANCE_INCREASED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.CHANNEL_ID_ERROR
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.CHANNEL_ID_THRESHOLD_REACHED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.NOTIFICATION_ID_BALANCE_INCREASED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.NOTIFICATION_ID_THRESHOLD_REACHED
import com.github.muellerma.prepaidbalance.utils.NotificationUtils.Companion.getBaseNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckBalanceWorker(
    private val context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams)
{
    override fun doWork(): Result {
            checkBalance(applicationContext) { result, _ ->
                Log.d(TAG, "Got result $result")
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

                    NotificationUtils.createChannels(context)
                    NotificationUtils
                        .manager(context)
                        .notify(NotificationUtils.NOTIFICATION_ID_ERROR, notification.build())
                }
            }

        return Result.success()
    }

    companion object {
        private val TAG = CheckBalanceWorker::class.java.simpleName

        fun checkBalance(context: Context, callback: (CheckResult, String?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "Remove entries older than 6 months")
                AppDatabase
                    .get(context)
                    .balanceDao()
                    .deleteBefore(System.currentTimeMillis() - 6L * 30 * 24 * 60 * 60 * 1000)
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return callback(CheckResult.MISSING_PERMISSIONS, null)
            }

            val ussdResponseCallback = object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager?,
                    request: String?,
                    response: CharSequence?
                ) {
                    Log.d(TAG, "onReceiveUssdResponse($response)")

                    context.prefs().edit {
                        putString("last_ussd_response", response?.toString())
                    }

                    val balance = ResponseParser.getBalance(response as String?)
                        ?: return callback(CheckResult.PARSER_FAILED, null)

                    handleNewBalance(context, balance, response, callback)
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager?,
                    request: String?,
                    failureCode: Int
                ) {
                    Log.d(TAG, "onReceiveUssdResponseFailed($failureCode)")

                    context.prefs().edit {
                        putString(
                            "last_ussd_response",
                            context.getString(R.string.debug_last_ussd_response_invalid, failureCode)
                        )
                    }

                    return callback(CheckResult.USSD_FAILED, null)
                }
            }

            val ussdCode = context.prefs().getString("ussd_code", "").orEmpty()
            if (!ussdCode.isValidUssdCode()) {
                return callback(CheckResult.USSD_INVALID, null)
            }

            Log.d(TAG, "Send USSD request to $ussdCode")
            context.getSystemService(TelephonyManager::class.java)
                .sendUssdRequest(
                    ussdCode,
                    ussdResponseCallback,
                    Handler(Looper.getMainLooper())
                )
        }

        private fun handleNewBalance(
            context: Context,
            balance: Double,
            response: String?,
            callback: (CheckResult, String?) -> Unit
        ) = CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.get(context)

                val latestInDb = database.balanceDao().getLatest()
                val new = BalanceEntry(timestamp = System.currentTimeMillis(), balance = balance)

                Log.d(TAG, "Insert $new")
                database
                    .balanceDao()
                    .insert(new)

                val prefs = context.prefs()
                if (
                    prefs.getBoolean("no_duplicates", true) &&
                    latestInDb?.balance == new.balance
                ) {
                    Log.d(TAG, "Remove $latestInDb from db")
                    database.balanceDao().delete(latestInDb)
                }

                callback(CheckResult.OK, response)

                NotificationUtils.createChannels(context)

                if (
                    prefs.getBoolean("notify_balance_increased", false) &&
                    latestInDb != null &&
                    new.balance > latestInDb.balance
                ) {
                    val diff = new.balance - latestInDb.balance
                    Log.d(TAG, "New balance is larger: $diff")

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
                    Log.d(TAG, "Below threshold")
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