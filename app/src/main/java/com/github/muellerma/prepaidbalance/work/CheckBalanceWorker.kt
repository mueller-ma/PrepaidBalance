package com.github.muellerma.prepaidbalance.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL
import android.telephony.TelephonyManager.USSD_RETURN_FAILURE
import android.util.Log
import androidx.work.*
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
import java.time.Duration

class CheckBalanceWorker(
    private val context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        checkBalance(applicationContext) { result, data ->
            Log.d(TAG, "Got result $result")
            val errorMessage = when (result) {
                CheckResult.USSD_FAILED -> context.getString(R.string.ussd_failed)
                CheckResult.MISSING_PERMISSIONS -> context.getString(R.string.phone_permissions_required)
                CheckResult.PARSER_FAILED -> context.getString(
                    R.string.unable_get_balance,
                    data
                )
                CheckResult.SUBSCRIPTION_INVALID -> context.getString(R.string.invalid_ussd_code)
                CheckResult.USSD_INVALID -> context.getString(R.string.invalid_ussd_code)
                CheckResult.OK -> return@checkBalance
            }

            val retryIntent = Intent(context, RetryBroadcastReceiver::class.java)
            val retryPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                retryIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )

            val notification = getBaseNotification(context, CHANNEL_ID_ERROR)
                .setContentTitle(errorMessage)
                .addAction(
                    R.drawable.ic_baseline_refresh_24,
                    context.getString(R.string.retry),
                    retryPendingIntent
                )

            NotificationUtils.createChannels(context)
            NotificationUtils
                .manager(context)
                .notify(NotificationUtils.NOTIFICATION_ID_ERROR, notification.build())
        }

        return Result.success()
    }

    companion object {
        private val TAG = CheckBalanceWorker::class.java.simpleName

        fun enqueueOrCancel(context: Context) {
            val enable = context.prefs().periodicCheck
            val rate = context.prefs().periodicCheckRateHours
            Log.d(TAG, "enqueue($enable, $rate)")
            rate ?: return

            if (!enable) {
                WorkManager
                    .getInstance(context)
                    .cancelAllWork()
                return
            }

            WorkManager.getInstance(context).apply {
                val request = PeriodicWorkRequest.Builder(
                    CheckBalanceWorker::class.java,
                    Duration.ofHours(rate)
                )
                    .setConstraints(Constraints.NONE)
                    .build()

                enqueueUniquePeriodicWork(
                    "work",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            }
        }

        fun checkBalance(context: Context, callback: (CheckResult, String?) -> Unit) {
            if (!context.hasPermissions(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)) {
                return callback(CheckResult.MISSING_PERMISSIONS, null)
            }

            val ussdResponseCallback = object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager?,
                    request: String?,
                    response: CharSequence?
                ) {
                    Log.d(TAG, "onReceiveUssdResponse($response)")

                    context.prefs().lastUssdResponse = response?.toString()

                    val balance = ResponseParser.getBalance(response as String?)
                        ?: return callback(CheckResult.PARSER_FAILED, response)

                    handleNewBalance(context, balance, response, callback)
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager?,
                    request: String?,
                    failureCode: Int
                ) {
                    Log.d(TAG, "onReceiveUssdResponseFailed($failureCode)")

                    val errorMessage = when (failureCode) {
                        USSD_RETURN_FAILURE -> context.getString(R.string.debug_last_ussd_response_failed_to_complete)
                        USSD_ERROR_SERVICE_UNAVAIL -> context.getString(R.string.debug_last_ussd_response_failed_telephony_service_unavailable)
                        else -> context.getString(R.string.debug_last_ussd_response_invalid, failureCode)
                    }

                    context.prefs().lastUssdResponse = errorMessage

                    return callback(CheckResult.USSD_FAILED, null)
                }
            }

            val ussdCode = context.prefs().ussdCode
            if (!ussdCode.isValidUssdCode()) {
                return callback(CheckResult.USSD_INVALID, null)
            }

            val subscriptionId = context.prefs().subscriptionId
                ?: return callback(CheckResult.SUBSCRIPTION_INVALID, null)

            Log.d(TAG, "SubscriptionId selected: $subscriptionId")
            Log.d(TAG, "Send USSD request to $ussdCode")
            context.getSystemService(TelephonyManager::class.java)
                .createForSubscriptionId(subscriptionId)
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
            val prefs = context.prefs()
            prefs.lastUpdateTimestamp = System.currentTimeMillis()

            val latestInDb = database.balanceDao().getLatest()
            if (balance == latestInDb?.balance) {
                Log.d(TAG, "New balance is equal to previous, don't insert")
            } else {
                val new = BalanceEntry(
                    timestamp = System.currentTimeMillis(),
                    balance = balance,
                    fullResponse = response
                )

                Log.d(TAG, "Insert $new")
                database
                    .balanceDao()
                    .insert(new)

                NotificationUtils.createChannels(context)
                showBalancedIncreasedIfRequired(context, prefs, latestInDb, new)
                showThresholdIfRequired(prefs, new, context)
            }
            callback(CheckResult.OK, response)
        }

        private fun showThresholdIfRequired(
            prefs: Prefs,
            new: BalanceEntry,
            context: Context
        ) {
            val threshold = prefs.notifyBalanceUnderThresholdValue
            if (
                prefs.notifyBalanceUnderThreshold && new.balance < threshold
            ) {
                Log.d(TAG, "Below threshold")
                val notification = getBaseNotification(context, CHANNEL_ID_THRESHOLD_REACHED)
                    .setContentTitle(
                        context.getString(
                            R.string.threshold_reached,
                            new.balance.formatAsCurrency()
                        )
                    )

                NotificationUtils
                    .manager(context)
                    .notify(NOTIFICATION_ID_THRESHOLD_REACHED, notification.build())
            }
        }

        private fun showBalancedIncreasedIfRequired(
            context: Context,
            prefs: Prefs,
            latestInDb: BalanceEntry?,
            new: BalanceEntry
        ) {
            if (
                prefs.notifyBalanceIncreased &&
                latestInDb != null &&
                new.balance > latestInDb.balance
            ) {
                val diff = new.balance - latestInDb.balance
                Log.d(TAG, "New balance is larger: $diff")

                val notification = getBaseNotification(context, CHANNEL_ID_BALANCE_INCREASED)
                    .setContentTitle(
                        context.getString(
                            R.string.balance_increased,
                            diff.formatAsCurrency()
                        )
                    )

                NotificationUtils
                    .manager(context)
                    .notify(NOTIFICATION_ID_BALANCE_INCREASED, notification.build())
            }
        }

        enum class CheckResult {
            OK,
            MISSING_PERMISSIONS,
            PARSER_FAILED,
            USSD_FAILED,
            SUBSCRIPTION_INVALID,
            USSD_INVALID
        }
    }
}