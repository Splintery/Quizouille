@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.univ.quizouille.services.AppNotificationManager
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.viewmodel.SettingsViewModel

@Composable
fun SettingsTextField(value: String, label: String, onDone: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (text.isNotEmpty() && text.isDigitsOnly() && text.toInt() > 0)
                    onDone(text)
            }
        ),
        modifier = Modifier.padding(start = 10.dp)
    )
}

fun handleNotificationsMode(
    mode: Boolean,
    settingsViewModel: SettingsViewModel,
    notificationManager: AppNotificationManager,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
    if (mode) {
        if(notificationManager.hasNotificationPermission())
            settingsViewModel.setNotifications(true)
        else
            notificationManager.requestNotificationPermission(permissionLauncher)
    }
    else
        settingsViewModel.setNotifications(false)
}

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    notificationManager: AppNotificationManager,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    val questionDelay by settingsViewModel.questionDelayFlow.collectAsState(initial = 10)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val notificationsMode by settingsViewModel.notificationsFlow.collectAsState(initial = false)
    val notificationsFrequency by settingsViewModel.notificationsFreqFlow.collectAsState(initial = 24)

    Column {
        TitleWithContentRow(title = "Paramètres", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        TitleWithContentRow(title = "Notifications", fontSize = policeSize) {
            Switch(
                checked = notificationsMode,
                onCheckedChange = {
                    handleNotificationsMode(
                        mode = it,
                        settingsViewModel = settingsViewModel,
                        notificationManager = notificationManager,
                        permissionLauncher = permissionLauncher
                    )
                })
        }
        if (notificationsMode) {
            // TODO snackbar pour dire que c'est entre 1 et 24
            TitleWithContentRow(title = "Fréquence des notifications", fontSize = policeSize) {
                SettingsTextField(
                    value = notificationsFrequency.toString(),
                    label = "heure",
                    onDone = { settingsViewModel.setNotificationsFrequency(it.toInt()) })
            }
        }
        TitleWithContentRow(title = "Temps de réponse aux questions", fontSize = policeSize) {
            SettingsTextField(
                value = questionDelay.toString(),
                label = "secondes",
                onDone = { settingsViewModel.setQuestionDelay(it.toInt()) }
            )
        }
        TitleWithContentRow(title = "Taille de la police", fontSize = policeSize) {
            // TODO mettre un minimun et un maximum
            SettingsTextField(
                value = policeSize.toString(),
                label = "",
                onDone = { settingsViewModel.setPoliceSize(it.toInt()) })
        }
    }
}