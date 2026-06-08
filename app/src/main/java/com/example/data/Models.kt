package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g. "صدر / Chest", "ظهر / Back", "أرجل / Legs", "أكتاف / Shoulders", "أذرع / Arms", "بطن / Core", "كارديو / Cardio"
    val isCustom: Boolean = false
)

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "set_logs")
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double // in kg
)
