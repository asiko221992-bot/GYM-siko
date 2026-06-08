package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM exercises ORDER BY category ASC, name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(setLog: SetLog): Long

    @Query("DELETE FROM set_logs WHERE sessionId = :sessionId")
    suspend fun deleteSetLogsForSession(sessionId: Int)

    @Query("SELECT * FROM workout_sessions ORDER BY dateMillis DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetLogsForSession(sessionId: Int): Flow<List<SetLog>>

    @Query("SELECT * FROM set_logs ORDER BY id DESC")
    fun getAllSetLogs(): Flow<List<SetLog>>
}
