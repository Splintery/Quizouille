package com.univ.quizouille.services

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.univ.quizouille.viewmodel.SettingsViewModel
import com.univ.quizouille.viewmodel.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val dataStore: DataStore<Preferences> = applicationContext.dataStore

        val shouldSendNotification = dataStore.data.map { preferences ->
            preferences[SettingsViewModel.PreferencesKeys.NOTIFICATIONS] ?: false
        }.first()

        val notificationFrequency = dataStore.data.map { preferences ->
            preferences[SettingsViewModel.PreferencesKeys.NOTIFICATIONS_FREQUENCY] ?: 10
        }.first()

        if (shouldSendNotification) {
            val notificationManager = AppNotificationManager(applicationContext)
            notificationManager.createNotification()
        }

        scheduleNextWork(applicationContext, notificationFrequency.toLong())

        return Result.success()
    }

    companion object {
        fun scheduleNextWork(context: Context, delay: Long) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "notification_work",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("notif", "c'est parti!")
        }
    }
}