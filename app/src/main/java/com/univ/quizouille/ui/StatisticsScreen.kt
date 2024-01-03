package com.univ.quizouille.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.univ.quizouille.model.QuestionSetStatistics
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.utilities.navigateToRoute
import com.univ.quizouille.utilities.navigateToRouteNoPopUp
import com.univ.quizouille.utilities.stringToLocalDate
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel, navController: NavHostController) {
    val questionsSet by gameViewModel.questionSetsFlow.collectAsState(listOf())
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    var selectedSetId by remember { mutableStateOf<Int?>(null) }
    var showNextButton by remember { mutableStateOf<Boolean?>(null) }

    Column {
        TitleWithContentRow(title = "Statistiques", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        if (questionsSet.isEmpty()) {
            TitleWithContentRow(title = "Aucun jeu de questions disponible actuellement", fontSize = policeSize)
        }
        else {
            Row {
                ECard(text = "Tous les jeux", fontSize = policeSize, modifier = Modifier.clickable {
                    showNextButton = true
                })
            }
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
            navigateToRouteNoPopUp(route = "statistics/$setId", navController = navController)
        }
    }

    LaunchedEffect(showNextButton) {
        showNextButton?.let { _ ->
            navigateToRouteNoPopUp(route = "statistics/all", navController = navController)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowStatisticsScreen(setId: Int, gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    LaunchedEffect(setId) {
        gameViewModel.fetchSetStatisticsById(setId = setId)
        gameViewModel.fetchQuestionSet(setId = setId)
    }

    val questionSetStatistics by gameViewModel.setStatisticsFlow.collectAsState(initial = null)
    val questionSet by gameViewModel.questionSetFlow.collectAsState(initial = null)
    val currentDate = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        questionSetStatistics?.let { stats ->
            val setName = questionSet?.name
            TitleWithContentRow(title = "Statistiques: $setName", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
            TitleWithContentRow(title = "Bonnes réponses: ${stats.correctCount}", fontSize = policeSize)
            TitleWithContentRow(title = "Total répondu: ${stats.totalAsked}", fontSize = policeSize)
            if (stats.lastTrainedDate.isNotEmpty()) {
                val daysSinceLastShown = ChronoUnit.DAYS.between(stringToLocalDate(stats.lastTrainedDate), currentDate)
                TitleWithContentRow(title = "Jours depuis le dernier entrainement: $daysSinceLastShown", fontSize = policeSize)
            }
            else
                TitleWithContentRow(title = "Aucun entraînement effectué", fontSize = policeSize)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowAllStatisticsScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    LaunchedEffect(true) {
        gameViewModel.fetchAllSetsStatistics()
    }

    val totalCorrectCount = gameViewModel.totalCorrectCount
    val totalAskedCount = gameViewModel.totalAskedCount
    val daysSinceTraining = gameViewModel.daysSinceTraining

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleWithContentRow(title = "Statistiques: Tous les jeux", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        TitleWithContentRow(title = "Bonnes réponses: $totalCorrectCount", fontSize = policeSize)
        TitleWithContentRow(title = "Total répondu: $totalAskedCount", fontSize = policeSize)
        if (daysSinceTraining >= 0)
            TitleWithContentRow(title = "Jours depuis le dernier entrainement: $daysSinceTraining", fontSize = policeSize)
        else
            TitleWithContentRow(title = "Aucun entraînement effectué", fontSize = policeSize)
    }
}
