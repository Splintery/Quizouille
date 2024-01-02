package com.univ.quizouille.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

    Column {
        TitleWithContentRow(title = "Statistiques", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        if (questionsSet.isEmpty()) {
            TitleWithContentRow(title = "Aucun jeu de questions disponible actuellement", fontSize = policeSize)
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
            navigateToRoute(route = "statistics/$setId", navController = navController)
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
    }

    val questionSetStatistics by gameViewModel.setStatisticsFlow.collectAsState(initial = null)
    val currentDate = LocalDate.now()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        questionSetStatistics?.let { stats ->
            TitleWithContentRow(title = "Statistiques", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
            Text("Bonnes réponses / Total: ${stats.correctCount}/${stats.totalAsked}", fontSize = policeSize.sp)
            if (stats.lastTrainedDate.isNotEmpty()) {
                val daysSinceLastShown = ChronoUnit.DAYS.between(stringToLocalDate(stats.lastTrainedDate), currentDate)
                Text("Jours depuis le dernier entrainement: $daysSinceLastShown", fontSize = policeSize.sp)
            }
        } ?: Text("Aucune statistique disponible pour ce jeu.", fontSize = policeSize.sp)
    }
}
