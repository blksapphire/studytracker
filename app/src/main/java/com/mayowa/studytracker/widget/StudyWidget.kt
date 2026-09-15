package com.mayowa.studytracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

/**
 * Home-screen widget: today's study time + current streak at a glance.
 * Data source: DailyStatsDao via a small repository read, refreshed by
 * the same WorkManager rollup job that updates daily_stats.
 */
class StudyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // TODO: pull real DailyStats + StreakInfo once wired to repository
            Text(text = "Today: 0m studied · 🔥 0 day streak")
        }
    }
}

class StudyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StudyWidget()
}
