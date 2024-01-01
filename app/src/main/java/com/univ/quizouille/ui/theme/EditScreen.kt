@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun EditScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    var setId by remember { mutableIntStateOf(1) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) } ) {
        Column {
            Row {
                OutlinedTextField(
                    value = setId.toString(),
                    onValueChange = { setId = it.toInt() },
                    label = { Text(text = "setId") })
            }
            Row {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(text = "question") })
            }
            Row {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text(text = "answer") })
            }
            Row {
                Button(onClick = {
                    gameViewModel.insertQuestion(setId, question, answer)
                }) {
                    Text(text = "insérer", Modifier.padding(1.dp))
                }
            }
        }
    }

    // envoie des snackbars quand une entrée dans la BD n'a pas marché
    LaunchedEffect(gameViewModel.errorMessage) {
        if (gameViewModel.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(gameViewModel.errorMessage)
            gameViewModel.errorMessage = ""
        }
    }
}