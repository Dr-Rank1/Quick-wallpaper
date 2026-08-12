package com.rank.quickwallpaper.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.rank.quickwallpaper.data.local.PaziaDatabase
import com.rank.quickwallpaper.service.WallpaperApplier
import com.rank.quickwallpaper.service.WallpaperFit
import com.rank.quickwallpaper.service.WallpaperTarget

class ShuffleWallpaperWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }
}

@Composable
private fun WidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF0B0E14)))
            .clickable(actionRunCallback<ShuffleWallpaperAction>())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Shuffle wallpaper",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

class ShuffleWallpaperWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShuffleWallpaperWidget()
}

class ShuffleWallpaperAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val db = Room.databaseBuilder(context, PaziaDatabase::class.java, "pazia.db")
            .addMigrations(PaziaDatabase.MIGRATION_1_2)
            .build()
        try {
            val favorites = db.favoriteDao().getAll()
            if (favorites.isEmpty()) return
            val pick = favorites.random()
            WallpaperApplier(context).apply(
                imageUrl = pick.fullUrl,
                target = WallpaperTarget.HOME,
                fit = WallpaperFit.FILL,
                localPath = pick.localCachedPath.takeIf { it.isNotBlank() }
            )
        } finally {
            db.close()
        }
    }
}
