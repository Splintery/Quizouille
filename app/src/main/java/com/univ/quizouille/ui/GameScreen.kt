@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.univ.quizouille.model.Answer
import com.univ.quizouille.model.Question
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.utilities.navigateToRoute
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
fun handleSingleAnswerValidation(userAnswer: String, answer: Answer, question: Question, gameViewModel: GameViewModel): Boolean {
    return if (userAnswer.equals(answer.answer, ignoreCase = true)) {
        gameViewModel.successQuestion(question)
        true
    } else {
        gameViewModel.failQuestion(question)
        false
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun handleMultipleAnswerValidation(answersSelectedId: List<Int>, answers: List<Answer>, question: Question, gameViewModel: GameViewModel): Boolean {
    var res = true
    for (element in answers) {
        if ((element.correct && !answersSelectedId.contains(element.answerId)) || (!element.correct && answersSelectedId.contains(element.answerId))) {
            res = false
        }
    }
    return if (res) {
        gameViewModel.successQuestion(question)
        true
    } else {
        gameViewModel.failQuestion(question)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun handleTimeout(question: Question, gameViewModel: GameViewModel) {
    gameViewModel.failQuestion(question)
}

@RequiresApi(Build.VERSION_CODES.O)
fun handleReveal(question: Question, gameViewModel: GameViewModel) {
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

fun getFontWeight(answersSelectedId: List<Int>, id: Int): FontWeight {
    return if (answersSelectedId.contains(id)) FontWeight.Bold else FontWeight.Normal
}
fun getElevation(answersSelectedId: List<Int>, id: Int): Int {
    return if (answersSelectedId.contains(id)) 10 else 0
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuestionScreen(
    questionId: Int,
    navController: NavHostController,
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    // on met à jour la question actuelle via une coroutine
    LaunchedEffect(questionId) {
        gameViewModel.fetchQuestionById(questionId = questionId)
    }
    val question by gameViewModel.questionFlow.collectAsState(initial = null)
    val answers: List<Answer> by gameViewModel.answersFlow.collectAsState(initial = mutableListOf())
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val timerFlow by settingsViewModel.questionDelayFlow.collectAsState(initial = 15)

    var answer by remember { mutableStateOf("") }
    var answersSelectedId: MutableList<Int> = remember { mutableListOf() }
    var showNextButton by remember { mutableStateOf(false) }
    var showNextQuestion by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(timerFlow) }

    // Déclencheurs Snackbar
    var successQuestion by remember { mutableStateOf(false) }
    var failQuestion by remember { mutableStateOf(false) }
    var revealQuestion by remember { mutableStateOf(false) }

    // Le timer continue tant qu'une réponse n'a pas été donnée
    LaunchedEffect(questionId, showNextButton) {
        timeLeft = timerFlow
        while (timeLeft > 0 && !showNextButton) {
            delay(1000)
            timeLeft -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                handleTimeout(question = question, gameViewModel = gameViewModel)
                showNextButton = true
            }
            ECard(text = question.content, fontSize = policeSize)
            if (answers.size == 1) {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Réponse") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = !showNextButton
                )
            } else {

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 5.dp)) {
                        for (index in answers.indices) {
                            if (index % 2 == 0) {
                                Text(
                                    text = answers[index].answer,
                                    fontSize = policeSize.sp,
                                    fontWeight = getFontWeight(answersSelectedId, answers[index].answerId),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .clickable {
                                            answer = answers[index].answer
                                            if (answersSelectedId.contains(answers[index].answerId)) {
                                                answersSelectedId.remove(answers[index].answerId)
                                            } else {
                                                answersSelectedId.add(answers[index].answerId)
                                            }
                                        }
                                        .padding(horizontal = 5.dp)
                                        .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(30))
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 5.dp)) {
                        for (index in 0..<answers.size) {
                            if (index % 2 == 1) {
                                Text(
                                    text = answers[index].answer,
                                    fontSize = policeSize.sp,
                                    fontWeight = getFontWeight(answersSelectedId, answers[index].answerId),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .clickable {
                                            answer = answers[index].answer
                                            if (answersSelectedId.contains(answers[index].answerId)) {
                                                answersSelectedId.remove(answers[index].answerId)
                                            } else {
                                                answersSelectedId.add(answers[index].answerId)
                                            }
                                        }
                                        .padding(horizontal = 5.dp)
                                        .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(30))
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }

                }

            }

            if (!showNextButton) {
                QuestionButton(buttonText = "Valider", fontSize = policeSize) {
                    if (answers.size == 1) {
                        if (handleSingleAnswerValidation(userAnswer = answer, answer = answers[0], question = question, gameViewModel = gameViewModel))
                            successQuestion = true
                        else
                            failQuestion = true
                        showNextButton = true
                    } else {
                        if (handleMultipleAnswerValidation(answersSelectedId = answersSelectedId, answers = answers, question = question, gameViewModel = gameViewModel))
                            successQuestion = true
                        else
                            failQuestion = true
                        showNextButton = true
                    }

                }
                QuestionButton(buttonText = "Afficher réponse", fontSize = policeSize) {
                    handleReveal(question = question, gameViewModel = gameViewModel)
                    if (answers.size == 1) {
                        answer = answers[0].answer
                    } else {
                        answersSelectedId.clear()
                        for (element in answers) {
                            if (element.correct) {
                                answersSelectedId.add(element.answerId)
                            }
                        }
                    }
                    showNextButton = true
                    revealQuestion = true
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

    LaunchedEffect(successQuestion, failQuestion, revealQuestion) {
        if (successQuestion || failQuestion || revealQuestion) {
            val message =
                if (successQuestion) "Bonne réponse !"
                else if (failQuestion)
                    "Mauvaise réponse..."
                else
                    "Réponse affichée, réessayez une prochaine fois !"
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
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
    fontWeight: FontWeight = FontWeight.Normal,
    elevation: Int = 6,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    val commonModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)

    val combinedModifier = commonModifier.then(modifier)

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        modifier = combinedModifier
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = textAlign,
            fontSize = fontSize.sp,
            fontWeight = fontWeight
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
fun GameScreen(
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController
) {
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