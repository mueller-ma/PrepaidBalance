package com.github.muellerma.prepaidbalance.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.muellerma.prepaidbalance.R

class NotificationUtils {
    companion object {
        private val TAG = NotificationUtils::class.java.simpleName
        const val CHANNEL_ID_BALANCE_INCREASED = "balance_increased"
        const val CHANNEL_ID_THRESHOLD_REACHED = "threshold"
        const val CHANNEL_ID_DAILY_LIMIT_REACHED = "daily_limit"
        const val CHANNEL_ID_ERROR = "error"

        const val NOTIFICATION_ID_BALANCE_INCREASED = 0
        const val NOTIFICATION_ID_THRESHOLD_REACHED = 1
        const val NOTIFICATION_ID_DAILY_LIMIT_REACHED = 2
        const val NOTIFICATION_ID_ERROR = 3

        fun getBaseNotification(context: Context, channel: String): NotificationCompat.Builder {
            return NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_circle_multiple_outline)
                .setColor(context.getColor(R.color.golden))
                .setLights(context.getColor(R.color.golden), 500, 500)
        }

        fun createChannels(context: Context) {
            Log.d(TAG, "createChannels()")
            val manager = manager(context)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_BALANCE_INCREASED,
                    context.getString(R.string.channel_name_balance_increased),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_THRESHOLD_REACHED,
                    context.getString(R.string.channel_name_threshold_reached),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_DAILY_LIMIT_REACHED,
                    context.getString(R.string.daily_limit_reached),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_ERROR,
                    context.getString(R.string.channel_name_error),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        fun manager(context: Context) =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}