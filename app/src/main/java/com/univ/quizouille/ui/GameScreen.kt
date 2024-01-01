@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.univ.quizouille.model.Question
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuestionScreen(questionId: Int, navController: NavHostController, gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    // on met à jour la question actuelle via une coroutine
    LaunchedEffect(questionId) {
        gameViewModel.fetchQuestionById(questionId = questionId)
    }
    val question by gameViewModel.questionFlow.collectAsState(initial = null)
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    var answer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TitleWithContentRow(title = "Question", fontSize = policeTitleSize)
        question?.let { question ->
            ECard(text = question.content, fontSize = policeSize)
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Réponse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Button(
                onClick = {
                    if (answer.equals(question.answer, ignoreCase = true)) {
                        gameViewModel.incrementQuestionStatus(question)
                    }
                    else {
                        gameViewModel.resetQuestionStatus(question)
                    }
                    gameViewModel.updateQuestionSeenDate(question)
                    // navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valider", fontSize = policeSize.sp)
            }
        }
    }
}

@Composable
fun ECard(
    text: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    val commonModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)

    val combinedModifier = commonModifier.then(modifier)

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = combinedModifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            textAlign = textAlign,
            fontSize = fontSize.sp
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel, navController: NavHostController) {
    val questionsSet by gameViewModel.getQuestionSetsForToday().collectAsState(listOf())
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    var selectedSetId by remember { mutableStateOf<Int?>(null) }

    Column {
        TitleWithContentRow(title = "Sujets du jour", fontSize = policeTitleSize)
        LazyColumn {
            items(questionsSet) { questionSet ->
                ECard(text = questionSet.name, fontSize = policeSize, modifier = Modifier.clickable {
                    selectedSetId = questionSet.setId
                })
            }
        }
    }

    LaunchedEffect(selectedSetId) {
        selectedSetId?.let { setId ->
            gameViewModel.getQuestionsForSet(setId).collect { questions ->
                val randomQuestion = questions.randomOrNull()
                randomQuestion?.let { question ->
                    navController.navigate("question/${question.questionId}")
                    selectedSetId = null
                }
            }
        }
    }
}