package com.rank.quickwallpaper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rank.quickwallpaper.MainActivity
import com.rank.quickwallpaper.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper that creates and posts a rich Android notification whenever
 * the user successfully sets a wallpaper.
 *
 * Call [notify] from [WallpaperApplier] or the ViewModel after a
 * successful [WallpaperApplier.apply] call.
 */
@Singleton
class WallpaperNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_WALLPAPER_SET = "wallpaper_set"
        const val CHANNEL_AUTO_WALLPAPER = "auto_wallpaper"
        private const val NOTIFICATION_ID_WALLPAPER_SET = 1001

        /** Register all app notification channels. Call once from [Application.onCreate]. */
        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = context.getSystemService(NotificationManager::class.java) ?: return

            nm.createNotificationChannels(
                listOf(
                    NotificationChannel(
                        CHANNEL_WALLPAPER_SET,
                        context.getString(R.string.notif_channel_wallpaper_set),
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = context.getString(R.string.notif_channel_wallpaper_set_desc)
                        enableVibration(false)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_AUTO_WALLPAPER,
                        context.getString(R.string.notif_channel_auto_wallpaper),
                        NotificationManager.IMPORTANCE_MIN
                    ).apply {
                        description = context.getString(R.string.notif_channel_auto_wallpaper_desc)
                        setShowBadge(false)
                    }
                )
            )
        }
    }

    /**
     * Post a "Wallpaper set ✓" notification.
     *
     * @param target Which screen was set (HOME / LOCK / BOTH).
     * @param photographer Photographer's name, shown in the notification body.
     * @param thumbnail Optional wallpaper bitmap shown as a large icon in the notification.
     * @param photoId Deep-link parameter so the notification taps back to that photo's detail screen.
     */
    fun notify(
        target: WallpaperTarget,
        photographer: String?,
        thumbnail: Bitmap? = null,
        photoId: String? = null
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val title = context.getString(
            when (target) {
                WallpaperTarget.HOME -> R.string.notif_wallpaper_set_home
                WallpaperTarget.LOCK -> R.string.notif_wallpaper_set_lock
                WallpaperTarget.BOTH -> R.string.notif_wallpaper_set_both
            }
        )
        val body = if (photographer != null) {
            context.getString(R.string.notif_wallpaper_set_body, photographer)
        } else {
            context.getString(R.string.notif_wallpaper_set_body_generic)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            photoId?.let { putExtra("photoId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WALLPAPER_SET,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_WALLPAPER_SET)
            .setSmallIcon(R.drawable.ic_notification_wallpaper)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)

        thumbnail?.let { bmp ->
            builder
                .setLargeIcon(bmp)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bmp)
                        .bigLargeIcon(null as Bitmap?) // collapse large icon when expanded
                        .setBigContentTitle(title)
                        .setSummaryText(body)
                )
        }

        runCatching {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_WALLPAPER_SET, builder.build())
        }
    }
}
