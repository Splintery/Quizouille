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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class GameViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dao = (application as AppApplication).database.appDao()

    var errorMessage by mutableStateOf("")
    private val questionSetsFlow = dao.getAllQuestionSets()
    var questionFlow = dao.getQuestionById(0)

    fun getQuestionsForSet(setId: Int): Flow<List<Question>> {
        return dao.getQuestionsForSet(setId)
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
        return daysSinceLastShown >= question.status
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilteredQuestionsForSet(setId: Int): Flow<List<Question>> {
        val currentDate = LocalDate.now()
        return dao.getQuestionsForSet(setId).map { questions ->
            questions.filter { question ->
                shouldShowQuestion(question, currentDate)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getQuestionSetsForToday(): Flow<List<QuestionSet>> = questionSetsFlow.map{ questionSets ->
        questionSets.filter { set ->
            // on récupère toutes les questions du set dont le status
            // est inférieur au nombre de jours de la dernière apparition
            val questions = getFilteredQuestionsForSet(set.setId).first()
            // on garde seulement les jeux de questions dont au moins une question est à répondre
            questions.isNotEmpty()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRandomQuestionFromSet(setId: Int): Flow<Question?> {
        return getFilteredQuestionsForSet(setId).map { questions ->
            if (questions.isNotEmpty())
                questions[Random.nextInt(questions.size)]
            else
                null
        }
    }

    fun fetchQuestionById(questionId: Int) {
        questionFlow = dao.getQuestionById(questionId = questionId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertQuestion(setId: Int, question: String, answer: String) {
        viewModelScope.launch {
            try {
                val currentDate = LocalDate.now().toString()
                dao.insertQuestion(Question(questionSetId = setId, content = question, answer = answer, lastShownDate = currentDate))
            }
            catch (e: Exception) {
                errorMessage = "Duplicate data: Failed to insert"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLastSeenDateQuestion(question: Question) {
        viewModelScope.launch {
            question.lastShownDate = LocalDate.now().toString()
            dao.updateQuestion(question)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun successQuestion(question: Question) {
        viewModelScope.launch {
            question.status += 1
            question.lastShownDate = LocalDate.now().toString()
            dao.updateQuestion(question)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun failQuestion(question: Question) {
        viewModelScope.launch {
            question.status = 1
            question.lastShownDate = LocalDate.now().toString()
            dao.updateQuestion(question)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertSampleData() = viewModelScope.launch {
        try {
            val currentDate = LocalDate.of(2023,12,28).toString()
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
                    questionSetId = 1,
                    content = "What is 1 + 1",
                    answer = "2",
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