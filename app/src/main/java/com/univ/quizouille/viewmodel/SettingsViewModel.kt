package com.univ.quizouille.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.univ.quizouille.services.AppNotificationManager
import com.univ.quizouille.services.NotificationWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name ="settings")

class SettingsViewModel (application: Application) : AndroidViewModel(application) {
    private val dataStore: DataStore<Preferences> = application.applicationContext.dataStore;

    val questionDelayFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUESTION_DELAY] ?: 15
    }

    val policeSizeFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POLICE_SIZE] ?: 16
    }

    val policeTitleSizeFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POLICE_TITLE_SIZE] ?: 20
    }

    val notificationsFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS] ?: false
    }

    val notificationsFreqFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_FREQUENCY] ?: 10
    }

    fun setQuestionDelay(delay: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.QUESTION_DELAY] = delay
                Log.d("datastore", preferences[PreferencesKeys.QUESTION_DELAY].toString())
            }
        }
    }

    fun setPoliceSize(size: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.POLICE_SIZE] = size
                preferences[PreferencesKeys.POLICE_TITLE_SIZE] = (size * 1.25).toInt()
            }
        }
    }

    fun setNotifications(
        mode: Boolean,
        notificationManager: AppNotificationManager,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        viewModelScope.launch {
            if (mode) {
                if(notificationManager.hasNotificationPermission()) {
                    dataStore.edit { preferences ->
                        preferences[PreferencesKeys.NOTIFICATIONS] = true
                        Log.d("notif", "true")
                        NotificationWorker.scheduleNextWork(getApplication(), notificationsFreqFlow.first().toLong())
                    }
                }
                else {
                    notificationManager.requestNotificationPermission(permissionLauncher)
                }
            }
            else {
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.NOTIFICATIONS] = false
                    Log.d("notif", "stoppé...")
                }
                WorkManager.getInstance(getApplication()).cancelUniqueWork("notification_work")
            }
        }
    }

    fun setNotificationsFrequency(freq: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATIONS_FREQUENCY] = freq
            }
        }
    }

    object PreferencesKeys {
        val QUESTION_DELAY = intPreferencesKey("question_delay")
        val POLICE_SIZE = intPreferencesKey("police_size")
        val POLICE_TITLE_SIZE = intPreferencesKey("police_title_size")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val NOTIFICATIONS_FREQUENCY = intPreferencesKey("notifications_freq")
    }
}