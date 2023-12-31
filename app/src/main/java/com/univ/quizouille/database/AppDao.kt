package com.univ.quizouille.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import kotlinx.coroutines.flow.Flow
@Dao
interface AppDao {
    @Insert
    suspend fun insertQuestionSet(questionSet: QuestionSet)

    @Insert
    suspend fun insertQuestion(question: Question)

    @Query("SELECT * FROM question_sets")
    fun getAllQuestionSets(): Flow<List<QuestionSet>>

    @Query("SELECT * FROM questions WHERE questionSetId = :setId")
    fun getQuestionsForSet(setId: Int): Flow<List<Question>>

    @Update
    suspend fun updateQuestion(question: Question)
}