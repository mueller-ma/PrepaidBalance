package com.github.muellerma.prepaidbalance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.ui.MainActivity
import com.github.muellerma.prepaidbalance.utils.formatAsCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class Widget : AppWidgetProvider(), CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + Job()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        appWidgetIds.forEach { appWidgetId ->
            Log.d(TAG, "Update widget with ID $appWidgetId")
            launch {
                setupWidget(context, appWidgetId, appWidgetManager)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        Log.d(TAG, "onAppWidgetOptionsChanged()")
        launch {
            setupWidget(context, appWidgetId, appWidgetManager)
        }
    }

    private fun setupWidget(context: Context, widgetId: Int, widgetManager: AppWidgetManager) {
        val database = AppDatabase.get(context)
        val balance = database.balanceDao().getLatest()?.balance ?: 0.0
        val textForWidget = balance.formatAsCurrency()
        Log.d(TAG, "textForWidget = $textForWidget")

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        RemoteViews(context.packageName, R.layout.widget).apply {
            setTextViewText(R.id.balance, textForWidget)
            setOnClickPendingIntent(android.R.id.background, pendingIntent)
            widgetManager.updateAppWidget(widgetId, this)
        }
    }

    companion object {
        private val TAG = Widget::class.java.simpleName
    }
}