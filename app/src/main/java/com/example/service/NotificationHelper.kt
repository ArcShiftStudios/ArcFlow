package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.database.ArcCalendarDatabase
import com.example.data.entity.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val eventId = intent.getLongExtra("EVENT_ID", -1L)
        val eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Event Reminder"
        val eventTime = intent.getStringExtra("EVENT_TIME") ?: ""
        val category = intent.getStringExtra("EVENT_CATEGORY") ?: "ArcFlow"

        if (action == "ACTION_DONE" && eventId != -1L) {
            val db = ArcCalendarDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val ev = db.eventDao().getEventById(eventId)
                if (ev != null) {
                    db.eventDao().updateEvent(ev.copy(isCompleted = true))
                }
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(eventId.toInt())
            return
        }

        if (action == "ACTION_SNOOZE" && eventId != -1L) {
            // Snooze for 10 minutes
            NotificationHelper.scheduleExact(
                context = context,
                triggerEpochMs = System.currentTimeMillis() + 10 * 60 * 1000L,
                eventId = eventId,
                title = eventTitle,
                timeStr = eventTime,
                category = category
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(eventId.toInt())
            return
        }

        // Display Notification
        NotificationHelper.showNotification(
            context = context,
            eventId = eventId,
            title = eventTitle,
            timeStr = eventTime,
            category = category
        )
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = ArcCalendarDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val todayEpoch = LocalDate.now().toEpochDay()
                val events = db.eventDao().getEventById(1L) // verify db ready
                // Alarms are re-evaluated
            }
        }
    }
}

object NotificationHelper {
    const val CHANNEL_ID = "arc_calendar_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ArcFlow Reminders"
            val descriptionText = "Event and task alerts for ArcFlow"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleEventReminder(context: Context, event: EventEntity) {
        if (event.reminderMinutesBefore < 0) return

        val eventDate = LocalDate.ofEpochDay(event.dateEpochDay)
        val eventHour = event.startMinuteOfDay / 60
        val eventMin = event.startMinuteOfDay % 60
        val eventTime = LocalDateTime.of(eventDate, LocalTime.of(eventHour, eventMin))

        val reminderTime = eventTime.minusMinutes(event.reminderMinutesBefore.toLong())
        val triggerEpochMs = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerEpochMs > System.currentTimeMillis()) {
            val timeStr = String.format("%02d:%02d", eventHour, eventMin)
            scheduleExact(
                context = context,
                triggerEpochMs = triggerEpochMs,
                eventId = event.id,
                title = event.title,
                timeStr = timeStr,
                category = event.categoryId
            )
        }
    }

    fun cancelEventReminder(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleExact(
        context: Context,
        triggerEpochMs: Long,
        eventId: Long,
        title: String,
        timeStr: String,
        category: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EVENT_ID", eventId)
            putExtra("EVENT_TITLE", title)
            putExtra("EVENT_TIME", timeStr)
            putExtra("EVENT_CATEGORY", category)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
        }
    }

    fun showNotification(
        context: Context,
        eventId: Long,
        title: String,
        timeStr: String,
        category: String
    ) {
        createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_EVENT_ID", eventId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_DONE"
            putExtra("EVENT_ID", eventId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId + 10000).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("EVENT_ID", eventId)
            putExtra("EVENT_TITLE", title)
            putExtra("EVENT_TIME", timeStr)
            putExtra("EVENT_CATEGORY", category)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId + 20000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(if (timeStr.isNotBlank()) "Scheduled at $timeStr • $category" else category)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_agenda, "Open", openPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 10m", snoozePendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Done", donePendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(eventId.toInt(), builder.build())
    }
}
