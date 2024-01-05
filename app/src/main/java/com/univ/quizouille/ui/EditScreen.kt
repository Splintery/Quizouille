@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel


fun getFontWeight(setId: Int, itemId: Int): FontWeight {
    if (setId == itemId) {
        return FontWeight.Bold
    } else {
        return FontWeight.Normal
    }
}

fun areAnswersValid(answer: List<String>, answerCorrect: List<Boolean>): Boolean {
    for (i in 0..<4) {
        if (!answer[i].equals("") && answerCorrect[i]) {
            return true
        }
    }
    return false
}
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun EditScreen(
    gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel, snackbarHostState: SnackbarHostState) {
    var setId by remember { mutableIntStateOf(-1) }
    var newSetName by remember { mutableStateOf("")}
    var question by remember { mutableStateOf("") }
    var answer = remember { mutableStateListOf("", "", "", "") }
    var answerCorrect = remember {mutableStateListOf(true, false, false, false)}
    var newSet by remember { mutableStateOf(false) }

    val lastQuestionSet by gameViewModel.lastSetInsertedIdFlow.collectAsState(initial = -1)
    val lastQuestion by gameViewModel.lastQuestionInsertedIdFlow.collectAsState(initial = -1)

    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val setNames: List<QuestionSet> by gameViewModel.questionSetsFlow.collectAsState(initial = mutableListOf())

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) } ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ){
            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxHeight(0.3f)
            ) {
                if (newSet) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = newSetName,
                            onValueChange = { newSetName = it },
                            label = {
                                Text(text = "new set")
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(setNames) {
                                Text(
                                    text = it.name,
                                    fontSize = policeSize.sp,
                                    fontWeight = getFontWeight(setId, it.setId),
                                    modifier = Modifier
                                        .clickable {
                                            if (setId != it.setId) {
                                                setId = it.setId
                                            } else {
                                                setId = -1
                                            }
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = Color.Black,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        newSet = !newSet
                        newSetName = ""
                        setId = -1
                    }) {
                        if (newSet) {
                            Image(Icons.Outlined.Clear, contentDescription = "cancel create set")
                        } else {
                            Image(Icons.Outlined.Add, contentDescription = "create set")
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(text = "question") }
                )
            }
            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 10.dp)) {
                Text(
                    text = "Toggle the switch if the answer is correct",
                    fontSize = policeTitleSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            for (index in 0 ..< 4) {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    OutlinedTextField(
                        value = answer[index],
                        onValueChange = { answer[index] = it },
                        label = {
                            if (index > 0) {
                                Text(text = "answer (Optinal)")
                            } else {
                                Text(text = "answer")
                            }
                        }
                    )
                    Switch(
                        checked = answerCorrect[index],
                        onCheckedChange = {
                            if (!it) {
                                for (i in 0..<4) {
                                    if (i != index && answerCorrect[i]) {
                                        answerCorrect[index] = false
                                        break;
                                    }
                                }
                            } else {
                                answerCorrect[index] = true
                            }
                        }
                    )
                }
            }
            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 10.dp)) {
                Button(onClick = {
                    if (areAnswersValid(answer, answerCorrect)) {
                        if (setId == -1) {
                            Log.d("test", setId.toString())
                            gameViewModel.insertQuestionSet(newSetName)
                            if (lastQuestionSet != -1) {
                                setId = lastQuestionSet + 1
                            }
                        }
                        Log.d("test", "setId: " + setId.toString())
                        gameViewModel.insertQuestion(setId = setId, question = question)
                        var questionId = lastQuestion + 1
                        Log.d("test", "questionId: " + questionId.toString())
                        for (i in 0..<4) {
                            if (!answer[i].equals("")) {
                                gameViewModel.insertAnswer(questionId = questionId, answer = answer[i], correct = answerCorrect[i])
                            }
                        }
                        question = ""
                        setId = -1
                        newSetName = ""
                        newSet = false
                        for (i in 0..<4) {
                            answer[i] = ""
                            answerCorrect[i] = false
                        }
                        answerCorrect[0] = true
                    }
                }) {
                    Text(text = "insérer", Modifier.padding(1.dp))
                }
            }
        }
    }

    // Détecte le changement de valeur de `gameViewModel.snackBarMessage` et lance un SnackBar si non vide
    LaunchedEffect(gameViewModel.snackBarMessage) {
        if (gameViewModel.snackBarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(gameViewModel.snackBarMessage, duration = SnackbarDuration.Short)
            gameViewModel.resetSnackbarMessage()
        }
    }
}

