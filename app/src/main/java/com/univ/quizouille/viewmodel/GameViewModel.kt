package com.univ.quizouille.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univ.quizouille.database.AppApplication
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GameViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = (application as AppApplication).database.appDao()

    private var errorMessage by mutableStateOf("")
    val questionSetsFlow = dao.getAllQuestionSets()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getQuestionSetsForToday(): Flow<List<QuestionSet>> = flow {
        val questionSets = questionSetsFlow.first()
        val currentDate = LocalDate.now()

        val setsWithQuestions = questionSets.filter { set ->
            val questions = dao.getQuestionsForSet(set.setId).first() // Get questions for the set
            questions.any { question -> shouldShowQuestion(question, currentDate) }
        }

        emit(setsWithQuestions)
    }

    fun insertQuestion(setId: Int, question: String, answer: String) = viewModelScope.launch {
        try {
            dao.insertQuestion(Question(questionSetId = setId, content = question, answer = answer))
        }
        catch (e: Exception) {
            errorMessage = "Duplicate data: Failed to insert"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stringToLocalDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shouldShowQuestion(question: Question, currentDate: LocalDate) : Boolean {
        val daysSinceLastShown = ChronoUnit.DAYS.between(stringToLocalDate(question.lastShownDate), currentDate)
        Log.d("days", daysSinceLastShown.toString())
        return daysSinceLastShown >= question.status
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertSampleData() = viewModelScope.launch {
        try {
            val currentDate = LocalDate.now().toString()
            val set1 = QuestionSet(name = "Math Formulas")
            val set2 = QuestionSet(name = "French Vocabulary")

            dao.insertQuestionSet(set1)
            dao.insertQuestionSet(set2)

            dao.insertQuestion(
                Question(
                    questionSetId = 1,
                    content = "What is Pi?",
                    answer = "3.14",
                    lastShownDate = currentDate
                )
            )
            dao.insertQuestion(
                Question(
                    questionSetId = 2,
                    content = "What is 'apple' in French?",
                    answer = "pomme",
                    lastShownDate = currentDate
                )
            )
        }
        catch (e: Exception) {
            errorMessage = "Failed to insert sample data: ${e.message}"
        }
    }
}