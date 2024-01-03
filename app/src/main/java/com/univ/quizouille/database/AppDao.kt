package com.univ.quizouille.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.model.QuestionSetStatistics
import kotlinx.coroutines.flow.Flow
@Dao
interface AppDao {
    @Insert
    suspend fun insertQuestionSetStats(questionSetStatistics: QuestionSetStatistics)

    @Insert
    suspend fun insertQuestionSet(questionSet: QuestionSet)

    @Insert
    suspend fun insertQuestion(question: Question)

    @Query("SELECT * FROM question_sets")
    fun getAllQuestionSets(): Flow<List<QuestionSet>>

    @Query("SELECT * FROM questions WHERE questionSetId = :setId")
    fun getQuestionsForSet(setId: Int): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE questionId = :questionId")
    fun getQuestionById(questionId: Int): Flow<Question>

    @Query("SELECT * FROM question_set_statistics WHERE questionSetId= :setId")
    fun getSetStatisticsById(setId: Int): Flow<QuestionSetStatistics>

    @Query("SELECT * FROM question_set_statistics")
    fun getSetsStatistics(): Flow<List<QuestionSetStatistics>>

    @Query("SELECT * FROM question_sets WHERE setId= :setId")
    fun getQuestionSetById(setId: Int): Flow<QuestionSet>

    @Update
    suspend fun updateQuestion(question: Question)

    @Update
    suspend fun updateQuestionSetStats(questionSetStatistics: QuestionSetStatistics)
}