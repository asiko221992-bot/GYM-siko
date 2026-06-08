package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoutRepository

    // Database flows
    val exercises: StateFlow<List<Exercise>>
    val sessions: StateFlow<List<WorkoutSession>>
    val allSetLogs: StateFlow<List<SetLog>>

    // Active session builder state
    val activeSessionNotes = MutableStateFlow("")
    val activeTempSets = MutableStateFlow<List<TempSetLog>>(emptyList())

    // UI Navigation State: "DASHBOARD", "NEW_WORKOUT", "EXERCISES_LIST"
    val activeTab = MutableStateFlow("DASHBOARD")

    // Filter/Search states
    val exerciseSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("الكل / All")

    init {
        val database = WorkoutDatabase.getDatabase(application)
        repository = WorkoutRepository(database.workoutDao())

        // Seed data in background
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        // Initialize flows
        exercises = repository.allExercises
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        sessions = repository.allSessions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allSetLogs = repository.allSetLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Custom Exercise Addition
    fun addNewExercise(name: String, category: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val newExercise = Exercise(
                    name = name.trim(),
                    category = category,
                    isCustom = true
                )
                repository.insertExercise(newExercise)
            }
        }
    }

    // Delete Custom Exercise
    fun deleteCustomExercise(exercise: Exercise) {
        viewModelScope.launch {
            if (exercise.isCustom) {
                repository.deleteExercise(exercise)
            }
        }
    }

    // Active Workout Builder Operations
    fun addSetToActiveWorkout(exerciseName: String, reps: Int, weight: Double) {
        val currentList = activeTempSets.value.toMutableList()
        currentList.add(TempSetLog(exerciseName = exerciseName, reps = reps, weight = weight))
        activeTempSets.value = currentList
    }

    fun removeSetFromActiveWorkout(index: Int) {
        val currentList = activeTempSets.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            activeTempSets.value = currentList
        }
    }

    fun updateSetInActiveWorkout(index: Int, reps: Int, weight: Double) {
        val currentList = activeTempSets.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(reps = reps, weight = weight)
            activeTempSets.value = currentList
        }
    }

    fun saveActiveWorkout() {
        val notes = activeSessionNotes.value.trim()
        val sets = activeTempSets.value

        if (sets.isNotEmpty()) {
            viewModelScope.launch {
                val finalNotes = if (notes.isEmpty()) "تمرين جديد / New Workout" else notes
                repository.saveSessionWithSets(finalNotes, sets)
                
                // Clear state
                activeSessionNotes.value = ""
                activeTempSets.value = emptyList()
                activeTab.value = "DASHBOARD" // Go back to dashboard!
            }
        }
    }

    fun discardActiveWorkout() {
        activeSessionNotes.value = ""
        activeTempSets.value = emptyList()
        activeTab.value = "DASHBOARD"
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }
}
