@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.univ.quizouille.viewmodel.SettingsViewModel

@Composable
fun TitleWithContentRow(title: String, fontSize: Int, content: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = fontSize.sp,
            modifier = Modifier.weight(1f)
        )
        content?.invoke()
    }
}

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

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val questionDelay by settingsViewModel.questionDelayFlow.collectAsState(initial = 10)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 10)
    val notificationsMode by settingsViewModel.notificationsFlow.collectAsState(initial = true)
    val notificationsFrequency by settingsViewModel.notificationsFreqFlow.collectAsState(initial = 1)

    Column {
        TitleWithContentRow(title = "Paramètres", fontSize = 20)
        TitleWithContentRow(title = "Notifications", fontSize = 16) {
            Switch(
                checked = notificationsMode,
                onCheckedChange = { settingsViewModel.setNotifications(notificationsMode) })
        }
        TitleWithContentRow(title = "Temps de réponse aux questions", fontSize = 16) {
            SettingsTextField(
                value = questionDelay.toString(),
                label = "secondes",
                onDone = { newDelay ->
                    settingsViewModel.setQuestionDelay(newDelay.toInt())
                }
            )
        }
        TitleWithContentRow(title = "Fréquence des notifications", fontSize = 16) {
            SettingsTextField(
                value = notificationsFrequency.toString(),
                label = "jour",
                onDone = { newFreq ->
                    settingsViewModel.setNotificationsFrequency(newFreq.toInt())
                })
        }
        TitleWithContentRow(title = "Taille de la police", fontSize = 16) {
            SettingsTextField(
                value = policeSize.toString(),
                label = "",
                onDone = { newSize ->
                    settingsViewModel.setPoliceSize(newSize.toInt())
                })
        }
    }
}