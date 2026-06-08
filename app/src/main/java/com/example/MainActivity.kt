package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Exercise
import com.example.data.SetLog
import com.example.data.TempSetLog
import com.example.data.WorkoutSession
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkoutTrackerApp()
                }
            }
        }
    }
}

@Composable
fun WorkoutTrackerApp(
    viewModel: WorkoutViewModel = viewModel()
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    // Enforce RTL Layout Direction for natural Arabic rendering
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                WorkoutBottomNavBar(
                    activeTab = activeTab,
                    onTabSelected = { tab -> viewModel.activeTab.value = tab }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                Color(0xFF121216)
                            )
                        )
                    )
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    "DASHBOARD" -> DashboardScreen(viewModel)
                    "NEW_WORKOUT" -> NewWorkoutScreen(viewModel)
                    "EXERCISES_LIST" -> ExercisesScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WorkoutBottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = activeTab == "DASHBOARD",
            onClick = { onTabSelected("DASHBOARD") },
            icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
            label = { Text("الرئيسية", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                unselectedIconColor = SportGrayText,
                unselectedTextColor = SportGrayText
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )
        NavigationBarItem(
            selected = activeTab == "NEW_WORKOUT",
            onClick = { onTabSelected("NEW_WORKOUT") },
            icon = { Icon(Icons.Default.Add, contentDescription = "تمرين جديد") },
            label = { Text("تمرين جديد", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                unselectedIconColor = SportGrayText,
                unselectedTextColor = SportGrayText
            ),
            modifier = Modifier.testTag("nav_new_workout")
        )
        NavigationBarItem(
            selected = activeTab == "EXERCISES_LIST",
            onClick = { onTabSelected("EXERCISES_LIST") },
            icon = { Icon(Icons.Default.List, contentDescription = "التمارين") },
            label = { Text("دليل التمارين", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                unselectedIconColor = SportGrayText,
                unselectedTextColor = SportGrayText
            ),
            modifier = Modifier.testTag("nav_exercises")
        )
    }
}

@Composable
fun DashboardScreen(viewModel: WorkoutViewModel) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val setLogs by viewModel.allSetLogs.collectAsStateWithLifecycle()

    // Compute dynamic dashboard stats
    val totalSessions = sessions.size
    val totalSets = setLogs.size
    val maxWeight = setLogs.maxOfOrNull { it.weight } ?: 0.0
    val totalReps = setLogs.sumOf { it.reps }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
    ) {
        // App Header Unit
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "متابع التمرين",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "أهلاً بك! تتبع نشاطك البدني وحقق أرقاماً جديدة اليوم.",
                    fontSize = 14.sp,
                    color = SportGrayText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Quick Stats Panel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "إحصائيات الأداء / Performance Stats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "الحصص",
                        value = totalSessions.toString(),
                        subtitle = "Sessions",
                        icon = Icons.Default.Check,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "أقصى وزن",
                        value = "${if (maxWeight % 1.0 == 0.0) maxWeight.toInt() else maxWeight} كجم",
                        subtitle = "Max Weight",
                        icon = Icons.Default.Star,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "إجمالي المجموعات",
                        value = totalSets.toString(),
                        subtitle = "Total Sets",
                        icon = Icons.Default.PlayArrow,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "إجمالي التكرار",
                        value = totalReps.toString(),
                        subtitle = "Total Reps",
                        icon = Icons.Default.Refresh,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title: Workout History
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل التمارين السابقة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { viewModel.activeTab.value = "NEW_WORKOUT" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Workout",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("سجل تمرين", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List or Empty Showcase
        if (sessions.isEmpty()) {
            item {
                EmptyStateCard(onNewWorkoutClick = { viewModel.activeTab.value = "NEW_WORKOUT" })
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                // Filter set logs relating to this session dynamically
                val sessionSets = setLogs.filter { it.sessionId == session.id }
                SessionHistoryCard(
                    session = session,
                    sets = sessionSets,
                    onDeleteSession = { viewModel.deleteSession(session.id) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .border(1.dp, Color(0xFF232329), RoundedCornerShape(20.dp))
            .testTag("stat_card_$title")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportGrayText
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = SportGrayText.copy(alpha = 0.7f),
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(onNewWorkoutClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF232329), RoundedCornerShape(24.dp))
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SportOrange.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = SportOrange,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "لا توجد تمارين مسجلة بعد",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ابدأ رحلتك الرياضية وقم بتسجيل حصتك التدريبية الأولى الآن لتتبع تقدمك المذهل!",
                fontSize = 13.sp,
                color = SportGrayText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNewWorkoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("start_first_workout_btn")
            ) {
                Text("سجل تمرينك الأول الآن", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SessionHistoryCard(
    session: WorkoutSession,
    sets: List<SetLog>,
    onDeleteSession: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Group sets by exercise name to show structured views
    val groupedSets = sets.groupBy { it.exerciseName }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF232329), RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded }
            .testTag("session_card_${session.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.notes,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatMillis(session.dateMillis),
                        fontSize = 12.sp,
                        color = SportGrayText,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteSession,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFF453A)),
                        modifier = Modifier.testTag("delete_session_btn_${session.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف التمرين",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "توسيع",
                        tint = SportGrayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "💪 ${groupedSets.size} تمارين",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportOrangeLight
                )
                Text(
                    text = "🏋️ ${sets.size} مجموعات",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportYellow
                )
            }

            // Expanded Workout Detail Panel
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = Color(0xFF232329), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    groupedSets.forEach { (exerciseName, exerciseSets) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = exerciseName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // List sets horizontally or vertically
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                exerciseSets.forEach { set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1C1C22), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "المجموعة ${set.setNumber}",
                                            fontSize = 12.sp,
                                            color = SportGrayText
                                        )
                                        Text(
                                            text = "${set.weight} كجم",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${set.reps} تكرار",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkoutScreen(viewModel: WorkoutViewModel) {
    val activeNotes by viewModel.activeSessionNotes.collectAsStateWithLifecycle()
    val activeSets by viewModel.activeTempSets.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    var showExercisePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
    ) {
        // Title banner
        item {
            Column {
                Text(
                    text = "تسجيل تمرين جديد",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "سجل التمارين والمجموعات التي تقوم بها حالياً لمراقبة فورية لأدائك.",
                    fontSize = 13.sp,
                    color = SportGrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Input notes
        item {
            OutlinedTextField(
                value = activeNotes,
                onValueChange = { viewModel.activeSessionNotes.value = it },
                label = { Text("اسم التمرين / ملاحظات (مثال: تمرين دفع - صدر)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workout_notes_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF232329),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = SportGrayText,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Controls action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة التمارين المضافة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { showExercisePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add exercise",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "إضافة تمرين +",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Display current active sets
        if (activeSets.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF232329), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "لا توجد تمارين مضافة في هذه الحصة",
                            fontSize = 14.sp,
                            color = SportGrayText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showExercisePicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("اختر تمرين من القائمة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Group activeSets by exercise name to show structured views
            val groupedActive = activeSets.onEachIndexed { index, _ -> }.withIndex().groupBy { it.value.exerciseName }

            items(groupedActive.keys.toList()) { exerciseName ->
                val setsForExercise = groupedActive[exerciseName] ?: emptyList()

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF232329), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exerciseName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            TextButton(
                                onClick = {
                                    viewModel.addSetToActiveWorkout(
                                        exerciseName = exerciseName,
                                        reps = 10,
                                        weight = if (setsForExercise.isNotEmpty()) setsForExercise.last().value.weight else 50.0
                                    )
                                }
                            ) {
                                Text("+ إضافة مجموعة", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic set listings
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            setsForExercise.forEachIndexed { num, indexedSet ->
                                val index = indexedSet.index
                                val setLog = indexedSet.value

                                ActiveSetItemRow(
                                    setNum = num + 1,
                                    setLog = setLog,
                                    onRepsChanged = { newReps ->
                                        viewModel.updateSetInActiveWorkout(index, newReps, setLog.weight)
                                    },
                                    onWeightChanged = { newWeight ->
                                        viewModel.updateSetInActiveWorkout(index, setLog.reps, newWeight)
                                    },
                                    onRemove = {
                                        viewModel.removeSetFromActiveWorkout(index)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Actions: Discard or Complete Workout
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.discardActiveWorkout() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(16.dp))
                        .testTag("discard_workout_btn")
                ) {
                    Text("إلغاء التغييرات", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.saveActiveWorkout() },
                    enabled = activeSets.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFF1F1F24)
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("save_workout_btn")
                ) {
                    Text("حفظ الحصة التدريبية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (activeSets.isNotEmpty()) Color.White else SportGrayText)
                }
            }
        }
    }

    // Modal dialogue: Picker of exercises
    if (showExercisePicker) {
        val searchQuery by viewModel.exerciseSearchQuery.collectAsStateWithLifecycle()
        val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

        // Filter search results
        val filteredExercises = exercises.filter {
            val matchesSearch = it.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "الكل / All" || it.category.contains(selectedCategory.split(" / ")[0])
            matchesSearch && matchesCategory
        }

        AlertDialog(
            onDismissRequest = { showExercisePicker = false },
            title = {
                Text(
                    text = "اختر التمرين الرياضي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.exerciseSearchQuery.value = it },
                        placeholder = { Text("ابحث في التمارين...", color = SportGrayText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_picker_search"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF232329),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Quick category buttons row
                    val categories = listOf("الكل / All", "صدر", "ظهر", "أرجل", "أكتاف", "أذرع", "بطن", "كارديو")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat || (cat != "الكل / All" && selectedCategory.startsWith(cat))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E1E24),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.selectedCategoryFilter.value = cat
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SportGrayText
                                )
                            }
                        }
                    }

                    // Exercises Result list
                    if (filteredExercises.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم يتم العثور على تمارين مخصصة. يمكنك إضافتها من صفحة دليل التمارين.",
                                fontSize = 12.sp,
                                color = SportGrayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredExercises) { exercise ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1C1C22), RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.addSetToActiveWorkout(exercise.name, 10, 50.0)
                                            showExercisePicker = false
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = exercise.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = exercise.category,
                                            fontSize = 11.sp,
                                            color = SportGrayText,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "أضف التمارين",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExercisePicker = false }) {
                    Text("إغلاق", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF16161A),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }
}

@Composable
fun ActiveSetItemRow(
    setNum: Int,
    setLog: TempSetLog,
    onRepsChanged: (Int) -> Unit,
    onWeightChanged: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F24), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set label and remove
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(70.dp)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "حذف مجموعة",
                    tint = Color(0xFFFF453A),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "م ${setNum}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Weight adjustments
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1.3f)
        ) {
            TextButton(
                onClick = { onWeightChanged((setLog.weight - 2.5).coerceAtLeast(0.0)) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Text("-", color = SportOrangeLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${if (setLog.weight % 1.0 == 0.0) setLog.weight.toInt() else setLog.weight}كجم",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text("وزن", fontSize = 9.sp, color = SportGrayText)
            }

            TextButton(
                onClick = { onWeightChanged(setLog.weight + 2.5) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", color = SportOrangeLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Reps adjustments
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1.2f)
        ) {
            TextButton(
                onClick = { onRepsChanged((setLog.reps - 1).coerceAtLeast(1)) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Text("-", color = SportYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${setLog.reps}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text("تكرار", fontSize = 9.sp, color = SportGrayText)
            }

            TextButton(
                onClick = { onRepsChanged(setLog.reps + 1) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", color = SportYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExercisesScreen(viewModel: WorkoutViewModel) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    var customExerciseName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("صدر / Chest") }

    val categories = listOf(
        "صدر / Chest",
        "ظهر / Back",
        "أرجل / Legs",
        "أكتاف / Shoulders",
        "أذرع / Arms",
        "بطن / Core",
        "كارديو / Cardio"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "دليل التمارين الرياضية",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "تصفح التمارين المتاحة، أو قم بإنشاء تمارينك الرياضية المخصصة للاستخدام الفوري.",
                    fontSize = 13.sp,
                    color = SportGrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Add custom exercise card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF232329), RoundedCornerShape(20.dp))
                    .testTag("add_custom_exercise_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إضافة تمرين جديد مخصص",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customExerciseName,
                        onValueChange = { customExerciseName = it },
                        placeholder = { Text("مثال: ضغط ترايسبس خلفي", color = SportGrayText) },
                        label = { Text("اسم التمرين المخصص") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_exercise_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF232329),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "العضلة المستهدفة / القسم",
                        fontSize = 12.sp,
                        color = SportGrayText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Easy category horizontal lists
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSel) MaterialTheme.colorScheme.primary else Color(0xFF1F1F24),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat.split(" / ")[0],
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else SportGrayText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (customExerciseName.isNotBlank()) {
                                viewModel.addNewExercise(customExerciseName, selectedCategory)
                                customExerciseName = ""
                            }
                        },
                        enabled = customExerciseName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFF1F1F24)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_custom_exercise_btn")
                    ) {
                        Text("إدراج التمرين لقائمتي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section Title: Directory List
        item {
            Text(
                text = "قائمة التمارين المعرفة",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // List exercises grouped nicely by muscle group
        val groupedExercises = exercises.groupBy { it.category }

        if (exercises.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جاري تهيئة التمارين الرئيسية...",
                        color = SportGrayText
                    )
                }
            }
        } else {
            groupedExercises.forEach { (cat, catExercises) ->
                item {
                    Text(
                        text = cat,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(catExercises, key = { it.id }) { exercise ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1F1F24), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (exercise.isCustom) "مخصص / Custom" else "أساسي / Default",
                                        fontSize = 11.sp,
                                        color = if (exercise.isCustom) SportYellow else SportGrayText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (exercise.isCustom) {
                                IconButton(
                                    onClick = { viewModel.deleteCustomExercise(exercise) },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFF453A)),
                                    modifier = Modifier.testTag("delete_exercise_btn_${exercise.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف تمرين مخصص",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Global Help Format Millisecond
fun formatMillis(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale("ar"))
    return sdf.format(Date(millis))
}
