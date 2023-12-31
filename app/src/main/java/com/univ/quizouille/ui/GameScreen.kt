package com.univ.quizouille.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel


@Composable
fun ShowQuestionsSet(set: List<QuestionSet>, fontSize: Int, onQuestionSetClick: (QuestionSet) -> Unit) {
    LazyColumn {
        items(set) { questionSet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQuestionSetClick(questionSet) }
                    .padding(16.dp)
                    .background(MaterialTheme.colors.surface)
            ) {
                Text(
                    text = questionSet.name,
                    modifier = Modifier.weight(1f),
                    fontSize = fontSize.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    val questionsSet by gameViewModel.getQuestionSetsForToday().collectAsState(listOf())
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    Column {
        TitleWithContentRow(title = "Sujets du jour", fontSize = policeTitleSize)
        ShowQuestionsSet(set = questionsSet, fontSize = policeSize) {}
    }
}