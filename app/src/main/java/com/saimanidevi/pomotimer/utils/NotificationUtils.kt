package com.saimanidevi.pomotimer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.saimanidevi.pomotimer.MainActivity
import com.saimanidevi.pomotimer.R
import com.saimanidevi.pomotimer.State
import com.saimanidevi.pomotimer.utils.Constants.CHANNEL_ID
import com.saimanidevi.pomotimer.utils.Constants.NOTIFICATION_CHANNEL_DESCRIPTION
import com.saimanidevi.pomotimer.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.saimanidevi.pomotimer.utils.Constants.NOTIFICATION_ID
import com.saimanidevi.pomotimer.utils.Constants.NOTIF_PENDING_INTENT_REQ_CODE
import com.saimanidevi.pomotimer.utils.Constants.VIBRATE_PATTERN

class NotificationUtils {
    fun displayNotification(context: Context, nextPomodoroState: State, pomoSession: Int) {
        val notificationTitle: String = context.getString(R.string.notification_title)
        val notificationContent: String = when (nextPomodoroState) {
            State.POMODORO -> context.getString(R.string.notification_content_pomodoro)
            State.SHORT_BREAK -> context.getString(R.string.notification_content_short_break)
            State.LONG_BREAK -> context.getString(R.string.notification_content_long_break)
        }
        val colorForLed = when (nextPomodoroState) {
            State.POMODORO -> Color.RED
            State.SHORT_BREAK -> Color.BLUE
            State.LONG_BREAK -> Color.GREEN
        }
        // Create sound uri
        val sound: Uri =
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/raw/japanese_bell")
        // Create the NotificationChannel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText = NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = colorForLed
                vibrationPattern = VIBRATE_PATTERN
                setSound(sound, Notification.AUDIO_ATTRIBUTES_DEFAULT)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tomato)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(VIBRATE_PATTERN)
            .setLights(colorForLed, 1000, 1000)
            .setAutoCancel(true)
        // Set-up flag for the notification
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        // Setup the pending intent to ensure MainActivity opens
        // when notification is clicked
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                NOTIF_PENDING_INTENT_REQ_CODE,
                flag
            )
        }
        builder.setContentIntent(pendingIntent)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }
}