package com.github.muellerma.prepaidbalance.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.muellerma.prepaidbalance.utils.NotificationUtils

class RetryBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive()")

        NotificationUtils
            .manager(context)
            .cancel(NotificationUtils.NOTIFICATION_ID_ERROR)

        CheckBalanceWorker.enqueueOrCancel(context)
    }

    companion object {
        private val TAG = RetryBroadcastReceiver::class.java.simpleName
    }
}