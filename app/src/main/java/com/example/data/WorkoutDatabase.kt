package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Exercise::class, WorkoutSession::class, SetLog::class], version = 1, exportSchema = false)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate on creation
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialExercises(database.workoutDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                optionsInit(context) // Ensure instance is held or ready (can just return instance)
                INSTANCE = instance
                instance
            }
        }

        private fun optionsInit(context: Context) {
            // Setup helper
        }

        private suspend fun populateInitialExercises(dao: WorkoutDao) {
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
            for (exercise in initialExercises) {
                dao.insertExercise(exercise)
            }
        }
    }
}
