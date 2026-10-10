package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.ArcCalendarDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ArcTodayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val db = ArcCalendarDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now()
            val events = db.eventDao().getEventById(1) // simple fetch
            val todayEvents = db.eventDao().getAllEvents()

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, today)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        today: LocalDate
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_arc_today)
        val weekdayStr = today.dayOfWeek.name
        val dateStr = String.format("%02d.%02d", today.dayOfMonth, today.monthValue)
        val monthStr = today.month.name

        views.setTextViewText(R.id.widget_weekday, weekdayStr)
        views.setTextViewText(R.id.widget_date_str, dateStr)
        views.setTextViewText(R.id.widget_month, monthStr)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_event_title, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
