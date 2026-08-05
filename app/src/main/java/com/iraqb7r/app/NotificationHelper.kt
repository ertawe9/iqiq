package com.iraqb7r.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_WIDGET = "widget_channel"
    const val CHANNEL_ALERTS = "alerts_channel"
    const val WIDGET_NOTIF_ID = 1001

    fun ensureChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (nm.getNotificationChannel(CHANNEL_WIDGET) == null) {
            val ch = NotificationChannel(
                CHANNEL_WIDGET,
                ctx.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_MIN
            )
            ch.description = ctx.getString(R.string.notif_channel_desc)
            nm.createNotificationChannel(ch)
        }
        if (nm.getNotificationChannel(CHANNEL_ALERTS) == null) {
            val ch2 = NotificationChannel(
                CHANNEL_ALERTS,
                "تنبيهات العربات",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(ch2)
        }
    }

    fun buildForegroundNotification(ctx: Context, contentText: String) =
        NotificationCompat.Builder(ctx, CHANNEL_WIDGET)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(ctx.getString(R.string.app_name))
            .setContentText(contentText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    fun showAlert(ctx: Context, title: String, body: String) {
        ensureChannels(ctx)
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(System.currentTimeMillis().toInt(), notif)
        } catch (e: SecurityException) { /* صلاحية الإشعارات غير ممنوحة، تجاهل */ }
    }
}
