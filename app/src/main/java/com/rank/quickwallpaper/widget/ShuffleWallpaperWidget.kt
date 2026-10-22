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
import androidx.glance.LocalContext
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.service.WallpaperApplier
import com.rank.quickwallpaper.service.WallpaperFit
import com.rank.quickwallpaper.service.WallpaperTarget
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun photoRepository(): PhotoRepository
    fun wallpaperApplier(): WallpaperApplier
}

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
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF0B0E14)))
            .clickable(actionRunCallback<ShuffleWallpaperAction>())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = context.getString(R.string.widget_shuffle_label),
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
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.photoRepository()
        val applier = entryPoint.wallpaperApplier()
        val favorites = repository.getFavoriteEntities()
        val (imageUrl, localPath) = if (favorites.isNotEmpty()) {
            val pick = favorites.random()
            Pair<String, String?>(pick.fullUrl, pick.localCachedPath.takeIf { it.isNotBlank() })
        } else {
            val fallback = repository.getRandomPhoto().getOrNull() ?: return
            Pair<String, String?>(fallback.fullUrl.ifBlank { fallback.regularUrl }, null)
        }
        applier.apply(
            imageUrl = imageUrl,
            target = WallpaperTarget.HOME,
            fit = WallpaperFit.FILL,
            localPath = localPath
        )
    }
}
