@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.univ.quizouille.model.Question
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.utilities.navigateToRoute
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
fun handleAnswerValidation(answer: String, question: Question, gameViewModel: GameViewModel) {
    if (answer.equals(question.answer, ignoreCase = true))
        gameViewModel.successQuestion(question)
    else
        gameViewModel.failQuestion(question)
}

@Composable
fun QuestionButton(buttonText: String, fontSize: Int, onClickAction: () -> Unit) {
    Button(
        onClick = onClickAction,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(buttonText, fontSize = fontSize.sp)
    }
}

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
    val timerFlow by settingsViewModel.questionDelayFlow.collectAsState(initial = 15)

    var answer by remember { mutableStateOf("") }
    var showNextButton by remember { mutableStateOf(false) }
    var showNextQuestion by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(timerFlow) }

    LaunchedEffect(key1 = questionId, key2 = showNextButton) {
        timeLeft = timerFlow // Reset timer for each new question
        while (timeLeft > 0 && !showNextButton) {
            delay(1000)
            timeLeft -= 1
        }

    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (!showNextButton) {
            Text(
                text = timeLeft.toString(),
                fontSize = policeTitleSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        TitleWithContentRow(title = "Question", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        question?.let { question ->
            if (timeLeft == 0) {
                gameViewModel.failQuestion(question)
                showNextButton = true
            }
            ECard(text = question.content, fontSize = policeSize)
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Réponse") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !showNextButton
            )
            if (!showNextButton) {
                QuestionButton(buttonText = "Valider", fontSize = policeSize) {
                    handleAnswerValidation(answer, question, gameViewModel)
                    showNextButton = true
                }
                QuestionButton(buttonText = "Afficher réponse", fontSize = policeSize) {
                    answer = question.answer
                    showNextButton = true
                }
            }
            else {
                QuestionButton(buttonText = "Question suivante", fontSize = policeSize) {
                    showNextQuestion = true
                }
            }
        }
    }

    LaunchedEffect(showNextQuestion) {
        question?.let { currentQuestion ->
            handleNextQuestionNavigation(
                setId = currentQuestion.questionSetId,
                gameViewModel = gameViewModel,
                navController = navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun handleNextQuestionNavigation(setId: Int, gameViewModel: GameViewModel, navController: NavHostController) {
    gameViewModel.getRandomQuestionFromSet(setId).collect { randomQuestion ->
        if (randomQuestion != null)
            navigateToRoute(
                route = "question/" + randomQuestion.questionId.toString(),
                navController = navController
            )
        else
            navigateToRoute(route = "gameEnded", navController = navController)
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

@Composable
fun GameEnded(settingsViewModel: SettingsViewModel, navController: NavHostController) {
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    Column {
        TitleWithContentRow(title = "Question", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        TitleWithContentRow(title = "Aucune question restante pour ce jeu de question !", fontSize = policeSize)
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
        TitleWithContentRow(title = "Sujets du jour", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        if (questionsSet.isEmpty()) {
            TitleWithContentRow(title = "Aucun sujet restant pour aujourd'hui, revenez demain !", fontSize = policeSize)
        }
        else {
            LazyColumn {
                items(questionsSet) { questionSet ->
                    ECard(text = questionSet.name, fontSize = policeSize, modifier = Modifier.clickable {
                        selectedSetId = questionSet.setId
                    })
                }
            }
        }
    }

    LaunchedEffect(selectedSetId) {
        selectedSetId?.let { setId ->
            gameViewModel.getRandomQuestionFromSet(setId).collect { randomQuestion ->
                randomQuestion?.let { question ->
                    navigateToRoute(route = "question/" + question.questionId.toString(), navController = navController)
                    selectedSetId = null
                }
            }
        }
    }
}