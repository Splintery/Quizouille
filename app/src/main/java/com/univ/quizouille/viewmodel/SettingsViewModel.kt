package com.univ.quizouille.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name ="settings")

class SettingsViewModel (application: Application) : AndroidViewModel(application) {
    private val dataStore: DataStore<Preferences> = application.applicationContext.dataStore;

    val questionDelayFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUESTION_DELAY] ?: 10
    }

    val policeSizeFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POLICE_SIZE] ?: 10
    }

    val notificationsFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS] ?: true
    }

    val notificationsFreqFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_FREQUENCY] ?: 1
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
            }
        }
    }

    fun setNotifications(mode: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATIONS] = mode
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

    private object PreferencesKeys {
        val QUESTION_DELAY = intPreferencesKey("question_delay")
        val POLICE_SIZE = intPreferencesKey("police_size")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val NOTIFICATIONS_FREQUENCY = intPreferencesKey("notifications_freq")
    }
}