package com.univ.quizouille.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "question_sets",
        indices = [Index(value = ["name"], unique = true)])
data class QuestionSet(
    @PrimaryKey(autoGenerate = true)
    val setId: Int = 0,
    val name: String
)
