package com.rank.quickwallpaper.di

import android.content.Context
import androidx.room.Room
import com.rank.quickwallpaper.data.local.FavoriteDao
import com.rank.quickwallpaper.data.local.HistoryDao
import com.rank.quickwallpaper.data.local.PaziaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PaziaDatabase =
        Room.databaseBuilder(context, PaziaDatabase::class.java, "pazia.db")
            .addMigrations(PaziaDatabase.MIGRATION_1_2, PaziaDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideFavoriteDao(db: PaziaDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideHistoryDao(db: PaziaDatabase): HistoryDao = db.historyDao()
}
