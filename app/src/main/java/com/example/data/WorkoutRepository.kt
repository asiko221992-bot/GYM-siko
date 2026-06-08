package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorkoutRepository(private val dao: WorkoutDao) {

    val allExercises: Flow<List<Exercise>> = dao.getAllExercises()
    val allSessions: Flow<List<WorkoutSession>> = dao.getAllSessions()
    val allSetLogs: Flow<List<SetLog>> = dao.getAllSetLogs()

    fun getSetLogsForSession(sessionId: Int): Flow<List<SetLog>> {
        return dao.getSetLogsForSession(sessionId)
    }

    suspend fun insertExercise(exercise: Exercise): Long {
        return dao.insertExercise(exercise)
    }

    suspend fun deleteExercise(exercise: Exercise) {
        dao.deleteExercise(exercise)
    }

    suspend fun insertSession(session: WorkoutSession): Long {
        return dao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: Int) {
        dao.deleteSetLogsForSession(sessionId)
        dao.deleteSessionById(sessionId)
    }

    suspend fun saveSessionWithSets(notes: String, sets: List<TempSetLog>) {
        val session = WorkoutSession(notes = notes, dateMillis = System.currentTimeMillis())
        val sessionId = dao.insertSession(session).toInt()
        
        sets.forEachIndexed { index, tempSet ->
            val setLog = SetLog(
                sessionId = sessionId,
                exerciseName = tempSet.exerciseName,
                setNumber = index + 1,
                reps = tempSet.reps,
                weight = tempSet.weight
            )
            dao.insertSetLog(setLog)
        }
    }

    suspend fun seedDatabaseIfEmpty() {
        // Collect once safely on dispatcher thread
        val exercises = dao.getAllExercises().first()
        if (exercises.isEmpty()) {
            val initialExercises = listOf(
                Exercise(name = "ضغط الصدر بالبار / Bench Press", category = "صدر / Chest"),
                Exercise(name = "تفتيح الصدر بالدمبل / Chest Fly", category = "صدر / Chest"),
                Exercise(name = "سحب علوي للظهر / Lat Pulldown", category = "ظهر / Back"),
                Exercise(name = "تجديف بالبار / Barbell Row", category = "ظهر / Back"),
                Exercise(name = "ضغط عسكري للأكتاف / Overhead Press", category = "أكتاف / Shoulders"),
                Exercise(name = "رفرفة جانبية بالدمبل / Lateral Raise", category = "أكتاف / Shoulders"),
                Exercise(name = "قرفصاء بالبار / Barbell Squat", category = "أرجل / Legs"),
                Exercise(name = "رفعة مميتة / Deadlift", category = "ظهر / Back"),
                Exercise(name = "مد الأرجل بالجهاز / Leg Extension", category = "أرجل / Legs"),
                Exercise(name = "تبادل بايسبس بالدمبل / Bicep Curl", category = "أذرع / Arms"),
                Exercise(name = "مد ترايسبس بالحبل / Tricep Pushdown", category = "أذرع / Arms"),
                Exercise(name = "بلانك / Plank", category = "بطن / Core"),
                Exercise(name = "معده طحن / Crunches", category = "بطن / Core"),
                Exercise(name = "ركض حر على السير / Treadmill Run", category = "كارديو / Cardio")
            )
            initialExercises.forEach { dao.insertExercise(it) }
        }
    }
}

// Helper data class for temporary sets in UI state machine before hitting Room DB
data class TempSetLog(
    val exerciseName: String,
    val reps: Int,
    val weight: Double
)
