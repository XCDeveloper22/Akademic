package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Course
import com.example.data.ScheduleItem
import com.example.data.Semester
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = remember { context.getSharedPreferences("akademic_theme_prefs", android.content.Context.MODE_PRIVATE) }
            val savedThemeStr = prefs.getString("selected_theme", com.example.ui.theme.AppTheme.DARK_MODE.name) ?: com.example.ui.theme.AppTheme.DARK_MODE.name
            var activeTheme by remember { 
                mutableStateOf(
                    try { com.example.ui.theme.AppTheme.valueOf(savedThemeStr) } catch(e: Exception) { com.example.ui.theme.AppTheme.DARK_MODE }
                )
            }
            
            MyApplicationTheme(appTheme = activeTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRootContent(
                        activeTheme = activeTheme,
                        onThemeChange = { newTheme ->
                            activeTheme = newTheme
                            prefs.edit().putString("selected_theme", newTheme.name).apply()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppRootContent(
    activeTheme: com.example.ui.theme.AppTheme,
    onThemeChange: (com.example.ui.theme.AppTheme) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val scope = rememberCoroutineScope()

    // Automatic transition from SPLASH to MAIN screen
    LaunchedEffect(currentScreen) {
        if (currentScreen == "SPLASH") {
            delay(5000) // Delay of 5 seconds for branding splash visibility as requested
            viewModel.currentScreen.value = "MAIN"
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(600, easing = EaseInOutCubic)) with
                    fadeOut(animationSpec = tween(500, easing = EaseInOutCubic))
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            "SPLASH" -> SplashScreen(onProceed = { viewModel.currentScreen.value = "MAIN" })
            else -> DashboardScreen(
                activeTheme = activeTheme,
                onThemeChange = onThemeChange,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun SplashScreen(onProceed: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraPulse")
    
    // Smooth pulse scaling for the logo
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Smooth rotational aura accent
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuraRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF380811), // Midnight Crimson
                        Color(0xFF130608)  // Velvet Charcoal Dark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative rich luxury radial background patterns
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Golden background rotating flare glow
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .rotate(auraRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xDFD4AF37), // Academic Gold
                                    Color(0x009B1B1B), // Clear
                                    Color(0x7FD4AF37)  // Gold
                                )
                            )
                        )
                )

                // Render the real custom generated metallic logo drawable
                Image(
                    painter = painterResource(id = R.drawable.img_app_logo_1782037411146),
                    contentDescription = "Akademic Circular Logo Medal",
                    modifier = Modifier
                        .size(190.dp)
                        .scale(scaleFactor)
                        .clip(RoundedCornerShape(32.dp))
                        .border(3.dp, Color(0xFFD4AF37), RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "AKADEMIC",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp
                ),
                color = Color(0xFFD4AF37), // Rich Gold
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OFFLINE ACADEMIC PORTAL & GWA PLANNER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color(0xFFAFAFAF),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Instantly bypass splash
            Button(
                onClick = onProceed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9B1B1B), // MSU Crimson
                    contentColor = Color(0xFFFFFDF6)   // Ivory
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFFD4AF37)), // Gold Lining
                modifier = Modifier
                    .widthIn(min = 180.dp)
                    .testTag("skip_splash_button")
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Continue Icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ENTER PORTAL",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ONE VISION. ONE VOICE. ONE PORTAL.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                ),
                color = Color(0x60FFFDF0)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    activeTheme: com.example.ui.theme.AppTheme,
    onThemeChange: (com.example.ui.theme.AppTheme) -> Unit,
    viewModel: MainViewModel
) {
    val semesters by viewModel.semesters.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val scheduleItems by viewModel.scheduleItems.collectAsState()
    val selectedSemId by viewModel.selectedSemesterId.collectAsState()
    val context = LocalContext.current

    var selectedSection by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf("Monday") }
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showSharePreviewDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isStudyModeActive by remember { mutableStateOf(false) }

    var semesterToEdit by remember { mutableStateOf<Semester?>(null) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var scheduleItemToEdit by remember { mutableStateOf<ScheduleItem?>(null) }

    // Screen width check for adaptive layouts
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    if (isStudyModeActive) {
        // Keep screen awake
        DisposableEffect(Unit) {
            val activity = context as? android.app.Activity
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        StudyModeFullScreen(
            activeTheme = activeTheme,
            onExit = { isStudyModeActive = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_logo_1782037411146),
                                contentDescription = "Logo Thumbnail",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, secondaryColor, RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                when (selectedSection) {
                                    0 -> "AKADEMIC"
                                    1 -> "CLASS SCHEDULE"
                                    2 -> "AKADEMIC REPORT"
                                    else -> "TASKS & REMINDERS"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Serif,
                                    letterSpacing = 2.sp
                                ),
                                color = secondaryColor
                            )
                        }
                    },
                    actions = {
                        // Study Mode quick toggle
                        IconButton(
                            onClick = { isStudyModeActive = true },
                            modifier = Modifier.testTag("study_mode_quick_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Start Study Stay Awake Mode",
                                tint = secondaryColor
                            )
                        }
                        IconButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.testTag("about_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "About, Terms and Privacy",
                                tint = secondaryColor
                            )
                        }
                        // Theme toggler
                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (activeTheme.isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Theme Toggle Selection Icon",
                                tint = secondaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!isTablet) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedSection == 0,
                            onClick = { selectedSection = 0 },
                            icon = { Icon(Icons.Default.School, contentDescription = "Academics Tab") },
                            label = { Text("AKADEMIC") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedSection == 1,
                            onClick = { selectedSection = 1 },
                            icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule Tab") },
                            label = { Text("Schedule") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedSection == 3,
                            onClick = { selectedSection = 3 },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tasks Tab") },
                            label = { Text("Tasks") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedSection == 2,
                            onClick = { selectedSection = 2 },
                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Report Tab") },
                            label = { Text("Report") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTablet) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight().testTag("tablet_nav_rail")
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        NavigationRailItem(
                            selected = selectedSection == 0,
                            onClick = { selectedSection = 0 },
                            icon = { Icon(Icons.Default.School, contentDescription = "Academics Tab") },
                            label = { Text("AKADEMIC") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = selectedSection == 1,
                            onClick = { selectedSection = 1 },
                            icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule Tab") },
                            label = { Text("Schedule") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = selectedSection == 3,
                            onClick = { selectedSection = 3 },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tasks Tab") },
                            label = { Text("Tasks") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = selectedSection == 2,
                            onClick = { selectedSection = 2 },
                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Report Tab") },
                            label = { Text("Report") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = secondaryColor,
                                selectedTextColor = secondaryColor,
                                indicatorColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ActiveSectionContent(
                    sectionIndex = selectedSection,
                    semesters = semesters,
                    courses = courses,
                    scheduleItems = scheduleItems,
                    selectedSemId = selectedSemId,
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it },
                    onAddSemesterClick = { showAddSemesterDialog = true },
                    onEditSemesterClick = { semesterToEdit = it },
                    onDeleteSemesterClick = { viewModel.deleteSemester(it) },
                    onAddCourseClick = { showAddCourseDialog = true },
                    onEditCourseClick = { courseToEdit = it },
                    onDeleteCourseClick = { viewModel.deleteCourse(it) },
                    onAddScheduleClick = { showAddScheduleDialog = true },
                    onEditScheduleItem = { scheduleItemToEdit = it },
                    onDeleteScheduleItem = { viewModel.deleteScheduleItem(it) },
                    activeTheme = activeTheme,
                    viewModel = viewModel,
                    context = context
                )
            }
        }
    }
}

    // Modal dialog handling
    if (showAddSemesterDialog) {
        AddSemesterModal(
            onDismiss = { showAddSemesterDialog = false },
            onConfirm = { name ->
                viewModel.addSemester(name)
                showAddSemesterDialog = false
            }
        )
    }

    // Edit Semester Dialog
    semesterToEdit?.let { semester ->
        AddSemesterModal(
            initialName = semester.name,
            onDismiss = { semesterToEdit = null },
            onConfirm = { name ->
                viewModel.updateSemester(semester.id, name)
                semesterToEdit = null
            }
        )
    }

    if (showAddCourseDialog && selectedSemId != null) {
        AddCourseModal(
            gradeList = viewModel.gradeScaleOptions,
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { code, name, credits, grade ->
                viewModel.addCourse(selectedSemId!!, code, name, credits, grade)
                showAddCourseDialog = false
            }
        )
    }

    // Edit Course Dialog
    courseToEdit?.let { course ->
        AddCourseModal(
            gradeList = viewModel.gradeScaleOptions,
            initialCode = course.code,
            initialName = course.name,
            initialCredits = course.credits,
            initialGrade = course.gradeString,
            onDismiss = { courseToEdit = null },
            onConfirm = { code, name, credits, grade ->
                viewModel.updateCourse(course.id, course.semesterId, code, name, credits, grade)
                courseToEdit = null
            }
        )
    }

    if (showAddScheduleDialog) {
        AddScheduleModal(
            onDismiss = { showAddScheduleDialog = false },
            onConfirm = { title, code, day, start, end, room, color ->
                viewModel.addScheduleItem(title, code, day, start, end, room, color)
                showAddScheduleDialog = false
            }
        )
    }

    // Edit Schedule Item Dialog
    scheduleItemToEdit?.let { item ->
        AddScheduleModal(
            initialTitle = item.title,
            initialCode = item.code,
            initialDay = item.dayOfWeek,
            initialStartTime = item.startTime,
            initialEndTime = item.endTime,
            initialRoom = item.room,
            initialColorHex = item.colorHex,
            onDismiss = { scheduleItemToEdit = null },
            onConfirm = { title, code, day, start, end, room, color ->
                viewModel.updateScheduleItem(item.id, title, code, day, start, end, room, color)
                scheduleItemToEdit = null
            }
        )
    }

    if (showSharePreviewDialog) {
        SharePreviewSheet(
            semesters = semesters,
            courses = courses,
            scheduleItems = scheduleItems,
            activeTheme = activeTheme,
            onDismiss = { showSharePreviewDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutAppModal(
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Text(
                        text = "CHOOSE APP THEME",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.ui.theme.AppTheme.values().forEach { themeItem ->
                            val isSelected = themeItem == activeTheme
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onThemeChange(themeItem)
                                        showThemeDialog = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (themeItem) {
                                                    com.example.ui.theme.AppTheme.CLASSIC_LIGHT -> Color(0xFF9B1B1B)
                                                    com.example.ui.theme.AppTheme.DARK_MODE -> Color(0xFFD4AF37)
                                                    com.example.ui.theme.AppTheme.AKADEMIC_BLUE -> Color(0xFF3B82F6)
                                                    com.example.ui.theme.AppTheme.FOREST_GREEN -> Color(0xFF4CAF50)
                                                    com.example.ui.theme.AppTheme.SUNSET_ORANGE -> Color(0xFFF97316)
                                                    com.example.ui.theme.AppTheme.PURPLE_SCHOLAR -> Color(0xFF8B5CF6)
                                                    com.example.ui.theme.AppTheme.PINK_BLOSSOM -> Color(0xFFEC4899)
                                                    com.example.ui.theme.AppTheme.PINK_SCHOLAR -> Color(0xFFDB2777)
                                                    com.example.ui.theme.AppTheme.MIDNIGHT_BLACK -> Color.White
                                                }
                                            )
                                    )
                                    Text(
                                        text = themeItem.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { showThemeDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// DYNAMIC NAVIGATION PANELS ACTIVE CONTENT
// ----------------------------------------------------
@Composable
fun ActiveSectionContent(
    sectionIndex: Int,
    semesters: List<Semester>,
    courses: List<Course>,
    scheduleItems: List<ScheduleItem>,
    selectedSemId: Int?,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    onAddSemesterClick: () -> Unit,
    onEditSemesterClick: (Semester) -> Unit,
    onDeleteSemesterClick: (Int) -> Unit,
    onAddCourseClick: () -> Unit,
    onEditCourseClick: (Course) -> Unit,
    onDeleteCourseClick: (Int) -> Unit,
    onAddScheduleClick: () -> Unit,
    onEditScheduleItem: (ScheduleItem) -> Unit,
    onDeleteScheduleItem: (Int) -> Unit,
    activeTheme: com.example.ui.theme.AppTheme,
    viewModel: MainViewModel,
    context: Context
) {
    val javaLocale = java.util.Locale.US
    when (sectionIndex) {
        0 -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CGPAMiniWidget(semesters = semesters, courses = courses)
                
                SemesterCatalogWidget(
                    semesters = semesters,
                    selectedSemId = selectedSemId,
                    courses = courses,
                    onSemesterSelect = { viewModel.selectSemester(it) },
                    onAddSemesterClick = onAddSemesterClick,
                    onEditSemesterClick = onEditSemesterClick,
                    onDeleteSemesterClick = onDeleteSemesterClick,
                    onAddCourseClick = onAddCourseClick,
                    onEditCourseClick = onEditCourseClick,
                    onDeleteCourseClick = onDeleteCourseClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        1 -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NotificationSettingsCard(context = context, scheduleItems = scheduleItems)
                TodayScheduleWidget(
                    scheduleItems = scheduleItems,
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                    onAddScheduleClick = onAddScheduleClick,
                    onEditScheduleItem = onEditScheduleItem,
                    onDeleteScheduleItem = onDeleteScheduleItem,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        2 -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "OFFICIAL REPORT CARD PREVIEW",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = Color(0xFFD4AF37)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B0609),
                        contentColor = Color(0xFFFFFCEE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo_1782037411146),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "AKADEMIC REPORT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFD4AF37)
                        )

                        Text(
                            text = "OFFLINE ACADEMY SYNCED CARD",
                            fontSize = 8.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFFA6A6A6)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0x20D4AF37))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            var totalCredits = 0.0
                            var totalWeighted = 0.0
                            courses.forEach { course ->
                                if (course.gradePoints >= 1.00 && course.gradePoints <= 5.00) {
                                    totalCredits += course.credits
                                    totalWeighted += (course.gradePoints * course.credits)
                                }
                            }
                            val overallGpa = if (totalCredits > 0.0) totalWeighted / totalCredits else 1.00

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CUMULATIVE GWA", fontSize = 8.sp, color = Color(0xFFD4AF37))
                                Text(
                                    text = String.format(javaLocale, "%.2f", overallGpa),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFFDF6)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL CREDITS", fontSize = 8.sp, color = Color(0xFFD4AF37))
                                Text(
                                    text = "${totalCredits.toInt()} cr",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFFDF6)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0x20D4AF37))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (semesters.isEmpty() || courses.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No recorded subject grades found", fontSize = 11.sp, color = Color(0x7FFFFFFF))
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                semesters.forEach { sem ->
                                    val semCourses = courses.filter { it.semesterId == sem.id }
                                    if (semCourses.isNotEmpty()) {
                                        Text(
                                            text = sem.name.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD4AF37),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        semCourses.forEach { crs ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(crs.code, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFDF6))
                                                    Text(crs.name, fontSize = 9.sp, color = Color(0x9FFFFFFF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("${crs.credits.toInt()} cr", fontSize = 10.sp, color = Color(0x7FFFFFFF))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFD4AF37).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(crs.gradeString, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
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

                // Organized separate Section: Class Schedule Report Card
                Text(
                    text = "OFFICIAL CLASS PLANNER REPORT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = Color(0xFFD4AF37),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B0609),
                        contentColor = Color(0xFFFFFCEE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "CLASS SCHEDULE REPORT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFD4AF37)
                        )

                        Text(
                            text = "WEEKLY SCHEDULE BREAKDOWN",
                            fontSize = 8.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFFA6A6A6)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0x20D4AF37))
                        Spacer(modifier = Modifier.height(12.dp))

                        val daysOfWeekList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                        var hasAnySchedules = false
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            daysOfWeekList.forEach { dayName ->
                                val daySchedules = scheduleItems.filter { it.dayOfWeek.equals(dayName, ignoreCase = true) }
                                if (daySchedules.isNotEmpty()) {
                                    hasAnySchedules = true
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = dayName.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD4AF37),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            daySchedules.forEach { item ->
                                                val parsedColor = try {
                                                    Color(android.graphics.Color.parseColor(item.colorHex))
                                                } catch (e: Exception) {
                                                    Color(0xFF9E1B32)
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .background(parsedColor, CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = "${item.code}: ${item.title}",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFFFFDF6)
                                                            )
                                                            if (item.room.isNotEmpty()) {
                                                                Text(
                                                                    text = "Room: ${item.room}",
                                                                    fontSize = 9.sp,
                                                                    color = Color(0x9FFFFFFF)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "${item.startTime} - ${item.endTime}",
                                                        fontSize = 10.sp,
                                                        color = Color(0x7FFFFFFF),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider(color = Color(0x10D4AF37))
                                    }
                                }
                            }
                            
                            if (!hasAnySchedules) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recorded class schedules found",
                                        fontSize = 11.sp,
                                        color = Color(0x7FFFFFFF)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            shareAcademicRecordAsImage(
                                context = context,
                                semesters = semesters,
                                courses = courses,
                                scheduleItems = scheduleItems,
                                shareIntent = false,
                                activeTheme = activeTheme
                            ) {}
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("save_to_gallery_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color(0xFF130608)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to Gallery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            shareAcademicRecordAsImage(
                                context = context,
                                semesters = semesters,
                                courses = courses,
                                scheduleItems = scheduleItems,
                                shareIntent = true,
                                activeTheme = activeTheme
                            ) {}
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("share_report_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B1B1B), contentColor = Color(0xFFFFFDF6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        3 -> {
            TasksScreen(viewModel = viewModel, context = context)
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Section not found", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// ----------------------------------------------------
// BENTO WIDGET 1: Cumulative Academic Grade Tracker Status (MSU GWA System)
// ----------------------------------------------------
@Composable
fun CGPAMiniWidget(
    semesters: List<Semester>,
    courses: List<Course>,
    modifier: Modifier = Modifier
) {
    // Math to compute Cumulative General Weighted Average (GWA)
    var totalCredits = 0.0
    var weightedPoints = 0.0
    
    courses.forEach { course ->
        // Only count valid numerical grades (1.00 to 5.00) in GWA
        if (course.gradePoints >= 1.00 && course.gradePoints <= 5.00) {
            totalCredits += course.credits
            weightedPoints += (course.gradePoints * course.credits)
        }
    }

    val cumulativeGwa = if (totalCredits > 0.0) {
        weightedPoints / totalCredits
    } else {
        1.00 // Default to highest starting point
    }

    // Since MSU Scale goes from 1.00 (highest) to 5.00 (failure), 
    // we calculate Academic Excellence score as: (5.00 - GWA) / 4.00
    // e.g. GWA of 1.00 -> 100% excellence score, GWA of 3.00 -> 50%, GWA of 5.00 -> 0%
    val progressRatio = ((5.00 - cumulativeGwa) / 4.00).toFloat().coerceIn(0f, 1f)

    // Determine verbal academic standing for the user's information
    val ratingDescription = when {
        totalCredits == 0.0 -> "No Grades Yet"
        cumulativeGwa <= 1.25 -> "Excellent"
        cumulativeGwa <= 1.75 -> "Very Good"
        cumulativeGwa <= 2.25 -> "Good"
        cumulativeGwa <= 2.75 -> "Satisfactory"
        cumulativeGwa <= 3.00 -> "Passing"
        else -> "Failure"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gpa_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9B1B1B), // MSU Red
            contentColor = Color(0xFFFFFDF6)
        ),
        border = BorderStroke(1.5.dp, Color(0xFFD4AF37)) // Beautiful gold border
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x40D4AF37), // Gold central aura
                            Color.Transparent
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left text details
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "MSU ACADEMIC RECORD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFFD4AF37) // Gold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cumulative GWA",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFFDF6)
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", cumulativeGwa),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif
                            ),
                            color = Color(0xFFFFFDF6)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GWA",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD4AF37),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Verbal rating standing
                    Box(
                        modifier = Modifier
                            .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Status: $ratingDescription",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFFDF6)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                "TOTAL CREDITS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                "${totalCredits.toInt()} Credits",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFDF6)
                            )
                        }
                        
                        Column {
                            Text(
                                "SEMESTERS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                "${semesters.size} Enrolled",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFDF6)
                            )
                        }
                    }
                }

                // Right visual circular radial status dial
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(0.7f)
                ) {
                    CircularProgressIndicator(
                        progress = progressRatio,
                        modifier = Modifier.size(90.dp),
                        color = Color(0xFFD4AF37), // Golden Accent
                        strokeWidth = 10.dp,
                        trackColor = Color(0xFF5F1111), // Dark MSU red tracker
                        strokeCap = StrokeCap.Round
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", progressRatio * 100),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFFDF6)
                        )
                        Text(
                            text = "EXCELLENCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// BENTO WIDGET 2: Daily Classes Schedule Planner
// ----------------------------------------------------
@Composable
fun TodayScheduleWidget(
    scheduleItems: List<ScheduleItem>,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    onAddScheduleClick: () -> Unit,
    onEditScheduleItem: (ScheduleItem) -> Unit,
    onDeleteScheduleItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val filteredSchedules = scheduleItems.filter { it.dayOfWeek.lowercase() == selectedDay.lowercase() }

    Card(
        modifier = modifier.testTag("today_schedule_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Schedule Icon",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SCHEDULE PLANNER",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFD4AF37)
                    )
                }

                IconButton(
                    onClick = onAddScheduleClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF9B1B1B), CircleShape)
                        .testTag("add_schedule_item_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Class Schedule",
                        tint = Color(0xFFFFFDF6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day selector strip (mon-sat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { dayName ->
                    val isSelected = dayName.lowercase() == selectedDay.lowercase()
                    val indicatorColor = if (isSelected) Color(0xFF9B1B1B) else MaterialTheme.colorScheme.surfaceVariant
                    val textColor = if (isSelected) Color(0xFFFFFDF6) else MaterialTheme.colorScheme.onSurfaceVariant
                    val borderStroke = if (isSelected) BorderStroke(1.dp, Color(0xFFD4AF37)) else null

                    Card(
                        onClick = { onDaySelected(dayName) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = indicatorColor,
                            contentColor = textColor
                        ),
                        border = borderStroke,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = dayName.take(3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = "No classes",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Classes Scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Enjoy your free day!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSchedules) { scheduleItem ->
                        ScheduleItemRow(
                            item = scheduleItem,
                            onEdit = { onEditScheduleItem(scheduleItem) },
                            onDelete = { onDeleteScheduleItem(scheduleItem.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItemRow(
    item: ScheduleItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored left accent bar
                Box(
                    modifier = Modifier
                        .size(4.dp, 40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(android.graphics.Color.parseColor(item.colorHex)))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.code,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD4AF37)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF9B1B1B).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.room,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9B1B1B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.startTime} - ${item.endTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit schedule item",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

enum class CourseSortOption(val displayName: String) {
    BY_CODE_ASC("Code A-Z"),
    BY_CODE_DESC("Code Z-A"),
    BY_NAME_ASC("Name A-Z"),
    BY_GRADE_ASC("Grade (Highest first)"),
    BY_GRADE_DESC("Grade (Lowest first)"),
    BY_CREDITS_DESC("Credits (High-Low)")
}

// ----------------------------------------------------
// BENTO WIDGET 3: Academic Semesters and Grades Portal List
// ----------------------------------------------------
@Composable
fun SemesterCatalogWidget(
    semesters: List<Semester>,
    selectedSemId: Int?,
    courses: List<Course>,
    onSemesterSelect: (Int) -> Unit,
    onAddSemesterClick: () -> Unit,
    onEditSemesterClick: (Semester) -> Unit,
    onDeleteSemesterClick: (Int) -> Unit,
    onAddCourseClick: () -> Unit,
    onEditCourseClick: (Course) -> Unit,
    onDeleteCourseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSemester = semesters.find { it.id == selectedSemId }
    val semesterCourses = courses.filter { it.semesterId == selectedSemId }

    var currentSortOption by remember { mutableStateOf(CourseSortOption.BY_CODE_ASC) }

    val sortedSemesterCourses = remember(semesterCourses, currentSortOption) {
        when (currentSortOption) {
            CourseSortOption.BY_CODE_ASC -> semesterCourses.sortedBy { it.code }
            CourseSortOption.BY_CODE_DESC -> semesterCourses.sortedByDescending { it.code }
            CourseSortOption.BY_NAME_ASC -> semesterCourses.sortedBy { it.name }
            CourseSortOption.BY_GRADE_ASC -> semesterCourses.sortedWith { c1, c2 ->
                val p1 = c1.gradePoints
                val p2 = c2.gradePoints
                if (p1 >= 1.0 && p2 >= 1.0) {
                    p1.compareTo(p2) // 1.00 < 5.00 ascending (highest first in MSU)
                } else if (p1 >= 1.0) {
                    -1
                } else if (p2 >= 1.0) {
                    1
                } else {
                    c1.gradeString.compareTo(c2.gradeString)
                }
            }
            CourseSortOption.BY_GRADE_DESC -> semesterCourses.sortedWith { c1, c2 ->
                val p1 = c1.gradePoints
                val p2 = c2.gradePoints
                if (p1 >= 1.0 && p2 >= 1.0) {
                    p2.compareTo(p1) // 5.00 > 1.00 descending (lowest first in MSU)
                } else if (p1 >= 1.0) {
                    -1
                } else if (p2 >= 1.0) {
                    1
                } else {
                    c1.gradeString.compareTo(c2.gradeString)
                }
            }
            CourseSortOption.BY_CREDITS_DESC -> semesterCourses.sortedByDescending { it.credits }
        }
    }

    // Calc GWA for current active semester
    var semCredits = 0.0
    var semWeightedPoints = 0.0
    semesterCourses.forEach {
        // Only include numerical passes/fails in weighted calculation
        if (it.gradePoints >= 1.00 && it.gradePoints <= 5.00) {
            semCredits += it.credits
            semWeightedPoints += (it.gradePoints * it.credits)
        }
    }
    val semGpa = if (semCredits > 0.0) semWeightedPoints / semCredits else 1.00

    Card(
        modifier = modifier.testTag("academic_portal_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title & quick buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = "Academic Catalog",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACADEMIC PORTAL",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFD4AF37)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add new semester action
                    TextButton(
                        onClick = onAddSemesterClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9B1B1B)),
                        modifier = Modifier.testTag("add_semester_button")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sem", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horiz chip list of semesters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                semesters.forEach { sem ->
                    val isSelected = sem.id == selectedSemId
                    val backColor = if (isSelected) Color(0xFFD4AF37).copy(alpha = 0.15f) else Color.Transparent
                    val border = if (isSelected) BorderStroke(1.5.dp, Color(0xFFD4AF37)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    val textColor = if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                    Surface(
                        onClick = { onSemesterSelect(sem.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = backColor,
                        border = border,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sem.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeSemester == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add a semester to get started with grading tracker!", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // Focus statistics bar of the chosen Semester
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeSemester.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Term GWA: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f", semGpa),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }
                            Text(
                                text = "Credits counted: ${semCredits.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onEditSemesterClick(activeSemester) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Semester", tint = Color(0xFFD4AF37))
                            }
                            IconButton(onClick = onAddCourseClick) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add course", tint = Color(0xFF9B1B1B))
                            }
                            IconButton(onClick = { onDeleteSemesterClick(activeSemester.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Sem", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Course Item Row lists
                if (semesterCourses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No courses calculated yet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            TextButton(onClick = onAddCourseClick, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD4AF37))) {
                                Text("Add Your First Subject")
                            }
                        }
                    }
                } else {
                    // Sort Subjects Selector Component
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subjects (${semesterCourses.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box {
                            var sortMenuExpanded by remember { mutableStateOf(false) }
                            TextButton(
                                onClick = { sortMenuExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD4AF37)),
                                modifier = Modifier.testTag("sort_subjects_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Subjects",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentSortOption.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                CourseSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.displayName) },
                                        onClick = {
                                            currentSortOption = option
                                            sortMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            if (currentSortOption == option) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD4AF37),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedSemesterCourses.forEach { course ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Grade round pill
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF9B1B1B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            course.gradeString,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFFDF6),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            course.code,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD4AF37)
                                        )
                                        Text(
                                            course.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        
                                        val subInfoText = if (course.gradePoints >= 1.00) {
                                            "Credits: ${course.credits.toInt()} | GWA grade: ${String.format(Locale.US, "%.2f", course.gradePoints)}"
                                        } else {
                                            "Credits: ${course.credits.toInt()} | Standby: ${course.gradeString}"
                                        }
                                        Text(
                                            subInfoText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { onEditCourseClick(course) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit course", tint = Color(0xFFD4AF37), modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onDeleteCourseClick(course.id) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
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

// ----------------------------------------------------
// BENTO WIDGET 4: Quick Dynamic Share Capabilities
// ----------------------------------------------------
@Composable
fun ExportActionWidget(
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onShareClick() }
            .testTag("export_promo_widget"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD4AF37).copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.5.dp, Color(0xFFD4AF37))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFD4AF37).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "EXPORT SCHEDULE & GWA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD4AF37),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Share high-fidelity report cards and weekly schedules instantly with peers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ----------------------------------------------------
// SHARING DIALOG PREVIEW AND NATIVE EXPORTER
// ----------------------------------------------------
@Composable
fun SharePreviewSheet(
    semesters: List<Semester>,
    courses: List<Course>,
    scheduleItems: List<ScheduleItem>,
    activeTheme: com.example.ui.theme.AppTheme,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("share_preview_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF130608), // Crimson Charcoal
                contentColor = Color(0xFFFFFDF6)
            ),
            border = BorderStroke(2.dp, Color(0xFFD4AF37))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header of preview
                Text(
                    text = "EXPORT PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color(0xFFD4AF37)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The virtual layout representing what will be generated into the PNG image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF251013), // Deep wine card
                        contentColor = Color(0xFFFFFCEE)
                    ),
                    border = BorderStroke(1.dp, Color(0x30D4AF37))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo_1782037411146),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "AKADEMIC REPORT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFD4AF37)
                        )

                        Text(
                            text = "OFFLINE ACADEMY SYNCED CARD",
                            fontSize = 8.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFFA6A6A6)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = Color(0x20D4AF37))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            var totalCredits = 0.0
                            var totalWeighted = 0.0
                            courses.forEach { course ->
                                if (course.gradePoints >= 1.00 && course.gradePoints <= 5.00) {
                                    totalCredits += course.credits
                                    totalWeighted += (course.gradePoints * course.credits)
                                }
                            }
                            val overallGpa = if (totalCredits > 0.0) totalWeighted / totalCredits else 1.00

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("OVERALL GWA", fontSize = 8.sp, color = Color(0xFFD4AF37))
                                Text(
                                    text = String.format(Locale.US, "%.2f", overallGpa),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFFDF6)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL CREDITS", fontSize = 8.sp, color = Color(0xFFD4AF37))
                                Text(
                                    text = "${totalCredits.toInt()} cr",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFFDF6)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0x15D4AF37))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Briefing class listings in preview
                        Text(
                            "WEEKLY SCHEDULE SUMMARY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (scheduleItems.isEmpty()) {
                            Text("No classes configured", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else {
                            scheduleItems.take(5).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${item.dayOfWeek.take(3)} ${item.startTime} - ${item.code}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${item.title} (${item.room})",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD4AF37),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            if (scheduleItems.size > 5) {
                                Text("+ ${scheduleItems.size - 5} more classes scheduled", fontSize = 9.sp, color = Color.LightGray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share file (preview -> share/download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFFDF6)),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            isSaving = true
                            // Execute share & generation
                            shareAcademicRecordAsImage(context, semesters, courses, scheduleItems, activeTheme = activeTheme) {
                                isSaving = false
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_download_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9B1B1B),
                            contentColor = Color(0xFFFFFDF6)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFFFFDF6), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHARE / SAVE")
                        }
                    }
                }
            }
        }
    }
}

// Generates an offscreen bitmap canvas, writes PNG to cache dir, and sends Action Share Intent
fun shareAcademicRecordAsImage(
    context: Context,
    semesters: List<Semester>,
    courses: List<Course>,
    scheduleItems: List<ScheduleItem>,
    shareIntent: Boolean = true,
    activeTheme: com.example.ui.theme.AppTheme = com.example.ui.theme.AppTheme.DARK_MODE,
    onComplete: () -> Unit
) {
    try {
        var totalCredits = 0.0
        var totalPoints = 0.0
        courses.forEach { course ->
            if (course.gradePoints >= 1.00 && course.gradePoints <= 5.00) {
                totalCredits += course.credits
                totalPoints += (course.gradePoints * course.credits)
            }
        }
        val overallGpa = if (totalCredits > 0.0) totalPoints / totalCredits else 1.00

        // Theme dynamic colors
        val backgroundHex = when (activeTheme) {
            com.example.ui.theme.AppTheme.CLASSIC_LIGHT -> "#FAFAFA"
            com.example.ui.theme.AppTheme.DARK_MODE -> "#130608"
            com.example.ui.theme.AppTheme.AKADEMIC_BLUE -> "#0B132B"
            com.example.ui.theme.AppTheme.FOREST_GREEN -> "#0D1F10"
            com.example.ui.theme.AppTheme.SUNSET_ORANGE -> "#1A0E0B"
            com.example.ui.theme.AppTheme.PURPLE_SCHOLAR -> "#110A1C"
            com.example.ui.theme.AppTheme.PINK_BLOSSOM -> "#FAF5F7"
            com.example.ui.theme.AppTheme.PINK_SCHOLAR -> "#1F1116"
            com.example.ui.theme.AppTheme.MIDNIGHT_BLACK -> "#000000"
        }

        val primaryHex = when (activeTheme) {
            com.example.ui.theme.AppTheme.CLASSIC_LIGHT -> "#9B1B1B"
            com.example.ui.theme.AppTheme.DARK_MODE -> "#9B1B1B"
            com.example.ui.theme.AppTheme.AKADEMIC_BLUE -> "#1E3A8A"
            com.example.ui.theme.AppTheme.FOREST_GREEN -> "#1B5E20"
            com.example.ui.theme.AppTheme.SUNSET_ORANGE -> "#D84315"
            com.example.ui.theme.AppTheme.PURPLE_SCHOLAR -> "#6D28D9"
            com.example.ui.theme.AppTheme.PINK_BLOSSOM -> "#DB2777"
            com.example.ui.theme.AppTheme.PINK_SCHOLAR -> "#9D174D"
            com.example.ui.theme.AppTheme.MIDNIGHT_BLACK -> "#222222"
        }

        val accentHex = when (activeTheme) {
            com.example.ui.theme.AppTheme.CLASSIC_LIGHT -> "#D4AF37"
            com.example.ui.theme.AppTheme.DARK_MODE -> "#D4AF37"
            com.example.ui.theme.AppTheme.AKADEMIC_BLUE -> "#64D2FF"
            com.example.ui.theme.AppTheme.FOREST_GREEN -> "#81C784"
            com.example.ui.theme.AppTheme.SUNSET_ORANGE -> "#FFB74D"
            com.example.ui.theme.AppTheme.PURPLE_SCHOLAR -> "#D6BCFA"
            com.example.ui.theme.AppTheme.PINK_BLOSSOM -> "#EC4899"
            com.example.ui.theme.AppTheme.PINK_SCHOLAR -> "#F472B6"
            com.example.ui.theme.AppTheme.MIDNIGHT_BLACK -> "#FFFFFF"
        }

        val textColorPrimaryHex = when (activeTheme) {
            com.example.ui.theme.AppTheme.CLASSIC_LIGHT -> "#1A1A1A"
            com.example.ui.theme.AppTheme.PINK_BLOSSOM -> "#371B26"
            else -> "#FFFDF6"
        }

        // Create a gorgeous high resolution custom card
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Backdrop paints
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor(backgroundHex)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Radial glow effect
        val glowPaint = Paint().apply {
            color = android.graphics.Color.parseColor(primaryHex)
            alpha = when (activeTheme) {
                com.example.ui.theme.AppTheme.CLASSIC_LIGHT, com.example.ui.theme.AppTheme.PINK_BLOSSOM -> 20
                else -> 42
            }
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, 480f, 450f, glowPaint)

        // Outer Border
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor(accentHex)
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawRoundRect(25f, 25f, width - 25f, height - 25f, 50f, 50f, borderPaint)

        // Internal accent border line
        val thinBorderPaint = Paint().apply {
            color = android.graphics.Color.parseColor(accentHex)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 100
        }
        canvas.drawRoundRect(40f, 40f, width - 40f, height - 40f, 40f, 40f, thinBorderPaint)

        // Content Paint Setup
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Title text
        textPaint.color = android.graphics.Color.parseColor(accentHex)
        textPaint.textSize = 64f
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("AKADEMIC REPORT", width / 2f, 150f, textPaint)

        // Subtitle slogan
        textPaint.color = android.graphics.Color.parseColor(textColorPrimaryHex)
        textPaint.textSize = 28f
        canvas.drawText("OFFLINE ACADEMY GWA TRANSCRIPT", width / 2f, 205f, textPaint)

        // Sub slogan Developer Brand
        textPaint.color = android.graphics.Color.parseColor(accentHex)
        textPaint.textSize = 22f
        canvas.drawText("Developed by XCDeveloper", width / 2f, 245f, textPaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = android.graphics.Color.parseColor(accentHex)
            strokeWidth = 2f
            alpha = 120
        }
        canvas.drawLine(100f, 290f, width - 100f, 290f, dividerPaint)

        // GPA circle container
        val gpaCirclePaint = Paint().apply {
            color = android.graphics.Color.parseColor(primaryHex)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, 470f, 130f, gpaCirclePaint)
        canvas.drawCircle(width / 2f, 470f, 130f, borderPaint)

        // GPA Value text
        val valuePaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor(if (activeTheme == com.example.ui.theme.AppTheme.CLASSIC_LIGHT || activeTheme == com.example.ui.theme.AppTheme.PINK_BLOSSOM) "#FFFFFF" else textColorPrimaryHex)
            textAlign = Paint.Align.CENTER
            textSize = 90f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText(String.format(Locale.US, "%.2f", overallGpa), width / 2f, 495f, valuePaint)

        val gpaLabelPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor(accentHex)
            textAlign = Paint.Align.CENTER
            textSize = 24f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("OVERALL GWA (MSU SCALE)", width / 2f, 540f, gpaLabelPaint)

        // Stats Labels Left & Right
        textPaint.textSize = 30f
        textPaint.color = android.graphics.Color.parseColor(textColorPrimaryHex)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Total Credits :  ${totalCredits.toInt()} cr", 130f, 710f, textPaint)
        canvas.drawText("Semesters Count:  ${semesters.size}", 130f, 765f, textPaint)

        canvas.drawText("Active Classes :  ${scheduleItems.size}", width - 420f, 710f, textPaint)
        canvas.drawText("Overall GWA Scale:  2.00", width - 420f, 765f, textPaint)

        // Middle Divider
        canvas.drawLine(100f, 840f, width - 100f, 840f, dividerPaint)

        // COURSES TRACKED Section
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = android.graphics.Color.parseColor(accentHex)
        textPaint.textSize = 38f
        canvas.drawText("ACADEMIC PROGRESS SUMMARY", width / 2f, 900f, textPaint)

        var courseY = 965f
        val topCourses = courses.take(6)
        if (topCourses.isEmpty()) {
            textPaint.color = android.graphics.Color.parseColor("#8E8E8E")
            textPaint.textSize = 26f
            canvas.drawText("No courses tracked yet. Use grading tracker to log.", width / 2f, courseY + 40f, textPaint)
        } else {
            topCourses.forEach { item ->
                textPaint.textAlign = Paint.Align.LEFT
                // Course Code
                textPaint.color = android.graphics.Color.parseColor(accentHex)
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textPaint.textSize = 26f
                canvas.drawText(item.code, 120f, courseY, textPaint)

                // Course Title
                textPaint.color = android.graphics.Color.parseColor(textColorPrimaryHex)
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                val cTitle = if (item.name.length > 28) item.name.take(25) + "..." else item.name
                canvas.drawText(cTitle, 310f, courseY, textPaint)

                // Course Grade & Credits
                textPaint.color = android.graphics.Color.parseColor(accentHex)
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                canvas.drawText("Credits: ${item.credits} cr", width - 360f, courseY, textPaint)
                canvas.drawText("GWA: ${item.gradePoints}", width - 210f, courseY, textPaint)

                courseY += 48f
            }
        }

        // Section separator
        canvas.drawLine(100f, 1310f, width - 100f, 1310f, dividerPaint)

        // WEEKLY CLASSES SCHEDULE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = android.graphics.Color.parseColor(accentHex)
        textPaint.textSize = 38f
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("WEEKLY CLASSES SCHEDULE", width / 2f, 1370f, textPaint)

        // Draw schedule rows
        var yPos = 1435f
        if (scheduleItems.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = android.graphics.Color.parseColor("#8E8E8E")
            textPaint.textSize = 26f
            textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            canvas.drawText("No offline classes scheduled. Use Acads Planner to add.", width / 2f, yPos + 40f, textPaint)
        } else {
            scheduleItems.take(7).forEach { item ->
                textPaint.textAlign = Paint.Align.LEFT
                // Draw Day/Time
                textPaint.color = android.graphics.Color.parseColor(accentHex)
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textPaint.textSize = 26f
                canvas.drawText("${item.dayOfWeek.take(3).uppercase()} ${item.startTime}", 120f, yPos, textPaint)

                // Draw Subject Name
                textPaint.color = android.graphics.Color.parseColor(textColorPrimaryHex)
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                val infoText = "${item.code}: ${item.title} [${item.room}]"
                val truncated = if (infoText.length > 42) infoText.take(39) + "..." else infoText
                canvas.drawText(truncated, 340f, yPos, textPaint)

                yPos += 52f
            }
        }

        // Footer Brand slogan
        val footerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor(accentHex)
            textAlign = Paint.Align.CENTER
            textSize = 22f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("ONE VISION • ONE VOICE • ONE MSUPORTAL", width / 2f, 1845f, footerPaint)

        // Save bitmap to cache directory
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "akademic_record_export.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        // Save directly to the phone's national system gallery
        try {
            val contentResolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "akademic_report_${System.currentTimeMillis()}.png")
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Akademic")
                    put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val imageUri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                contentResolver.openOutputStream(imageUri)?.use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(imageUri, contentValues, null, null)
                }
                // Notify system media databases to index this image instantly so the user sees the latest design in their gallery apps
                try {
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        arrayOf(imageUri.toString()),
                        arrayOf("image/png")
                    ) { _, _ -> }
                } catch (scanEx: Exception) {
                    scanEx.printStackTrace()
                }
                Toast.makeText(context, "Saved report to phone gallery successfully!", Toast.LENGTH_LONG).show()
            }
        } catch (mediaEx: Exception) {
            mediaEx.printStackTrace()
            Toast.makeText(context, "Gallery save failed: ${mediaEx.localizedMessage}", Toast.LENGTH_SHORT).show()
        }

        if (shareIntent) {
            // Get file Uri secure provider
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            // Issue intent Action Send share
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_TITLE, "My Akademic PDF/Image Record")
                putExtra(Intent.EXTRA_SUBJECT, "Check out my schedule and GWA report on Akademic!")
                putExtra(Intent.EXTRA_TEXT, "Shared from Akademic Offline Academic Portal. My Cumulative GWA: ${String.format(Locale.US, "%.2f", overallGpa)}")
            }

            context.startActivity(Intent.createChooser(intent, "Export schedule & report as PDF/Image"))
        }
        onComplete()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        onComplete()
    }
}

// ----------------------------------------------------
// UI DIALOGS FOR INPUT DETAILS
// ----------------------------------------------------
@Composable
fun AddSemesterModal(
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    
    val isEdit = initialName.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Semester Name" else "Create New Semester", fontWeight = FontWeight.ExtraBold, color = Color(0xFFCF9E26)) },
        text = {
            Column {
                Text("Enter label for the educational semester term.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Semester Name") },
                    placeholder = { Text("e.g. 2nd Year - 1st Semester") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_semester_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCF9E26),
                        focusedLabelColor = Color(0xFFCF9E26)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E1B32)),
                modifier = Modifier.testTag("submit_semester_button")
            ) {
                Text(if (isEdit) "Update Term" else "Save Term", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddCourseModal(
    gradeList: List<String>,
    initialCode: String = "",
    initialName: String = "",
    initialCredits: Double = 3.0,
    initialGrade: String = "1.00",
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit
) {
    var code by remember { mutableStateOf(initialCode) }
    var name by remember { mutableStateOf(initialName) }
    var creditsStr by remember { mutableStateOf(if (initialCredits % 1.0 == 0.0) initialCredits.toInt().toString() else initialCredits.toString()) }
    var selectedGrade by remember { mutableStateOf(initialGrade) }
    var expandedDropdown by remember { mutableStateOf(false) }
    
    val isEdit = initialCode.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Subject Details" else "Record Course Grade", fontWeight = FontWeight.ExtraBold, color = Color(0xFFCF9E26)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code") },
                    placeholder = { Text("e.g. COSC 311") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("course_code_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Title") },
                    placeholder = { Text("e.g. Database Systems") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("course_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                )

                OutlinedTextField(
                    value = creditsStr,
                    onValueChange = { creditsStr = it },
                    label = { Text("Credits Hours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("course_credits_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("course_grade_dropdown_trigger"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.dp, Color(0xFFCF9E26))
                    ) {
                        Text("Expected Grade: $selectedGrade", fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        gradeList.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedGrade = grade
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cr = creditsStr.toDoubleOrNull() ?: 3.0
                    if (code.isNotBlank() && name.isNotBlank()) {
                        onConfirm(code, name, cr, selectedGrade)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E1B32)),
                modifier = Modifier.testTag("submit_course_button")
            ) {
                Text(if (isEdit) "Update Subject" else "Confirm Grade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddScheduleModal(
    initialTitle: String = "",
    initialCode: String = "",
    initialDay: String = "Monday",
    initialStartTime: String = "09:00",
    initialEndTime: String = "10:30",
    initialRoom: String = "",
    initialColorHex: String = "#9E1B32",
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var code by remember { mutableStateOf(initialCode) }
    var selectedDay by remember { mutableStateOf(initialDay) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTime) }
    var room by remember { mutableStateOf(initialRoom) }
    var colorHex by remember { mutableStateOf(initialColorHex) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val colors = listOf("#9E1B32", "#CF9E26", "#2E7D32", "#1565C0", "#D81B60", "#F4511E")

    var dayExpanded by remember { mutableStateOf(false) }
    
    val isEdit = initialTitle.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Class Lecture" else "Schedule Class Lecture", fontWeight = FontWeight.ExtraBold, color = Color(0xFFCF9E26)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Subject Code") },
                    placeholder = { Text("e.g. COSC 311") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("schedule_code_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subject Name") },
                    placeholder = { Text("e.g. Software Engineering") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("schedule_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Starts") },
                        placeholder = { Text("e.g. 09:00") },
                        modifier = Modifier.weight(1f).testTag("schedule_start_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Ends") },
                        placeholder = { Text("e.g. 10:30") },
                        modifier = Modifier.weight(1f).testTag("schedule_end_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                    )
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Lecture Hall / Classroom") },
                    placeholder = { Text("e.g. Room 311 / Lab 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("schedule_room_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFCF9E26), focusedLabelColor = Color(0xFFCF9E26))
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dayExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("schedule_day_dropdown_trigger"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.dp, Color(0xFFCF9E26))
                    ) {
                        Text("Day of Week: $selectedDay", fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        days.forEach { dayName ->
                            DropdownMenuItem(
                                text = { Text(dayName, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedDay = dayName
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Theme color indicator:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { hex ->
                        val isSelected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    if (isSelected) 2.5.dp else 0.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && title.isNotBlank() && room.isNotBlank()) {
                        onConfirm(title, code, selectedDay, startTime, endTime, room, colorHex)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E1B32)),
                modifier = Modifier.testTag("submit_schedule_button")
            ) {
                Text(if (isEdit) "Update Schedule" else "Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AboutAppModal(
    onDismiss: () -> Unit
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var expandedSection by remember { mutableStateOf(0) } // 0: AboutUs, 1: Terms, 2: Privacy, 3: Contact

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Application Information",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFCF9E26),
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Banner / Logo header inside
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF220509))
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo_1782037411146),
                            contentDescription = "Akademic Shield Logo Large",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AKADEMIC",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFD4AF37),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Developed by XCDeveloper",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section 1: About Us
                InfoSectionCard(
                    title = "About Us",
                    icon = Icons.Default.School,
                    expanded = expandedSection == 0,
                    onToggle = { expandedSection = if (expandedSection == 0) -1 else 0 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Akademic is an offline academic management application designed to help students organize, monitor, and manage their academic records in one place.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "The application provides tools for subject management, grade tracking, GPA/GWA computation, semester organization, and academic record storage. Since Akademic works offline, students can access their information anytime without an internet connection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "Our mission is to provide a simple, accessible, and reliable academic companion for students throughout their educational journey.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Developed by XCDeveloper.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }

                // Section 2: Terms & Conditions
                InfoSectionCard(
                    title = "Terms and Conditions",
                    icon = Icons.Default.Assignment,
                    expanded = expandedSection == 1,
                    onToggle = { expandedSection = if (expandedSection == 1) -1 else 1 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "By using Akademic, you agree to the following terms:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        val terms = listOf(
                            "Akademic is intended for educational and personal academic management purposes only.",
                            "Users are responsible for the accuracy of the information they enter into the application.",
                            "All data is stored locally on the user's device unless exported by the user.",
                            "XCDeveloper is not responsible for data loss caused by device failure, accidental deletion, or lack of backups.",
                            "Users may not copy, modify, distribute, or resell any part of the application without prior permission.",
                            "Features and functionality may be updated, improved, or modified in future releases.",
                            "Continued use of the application constitutes acceptance of these Terms and Conditions."
                        )
                        terms.forEachIndexed { index, term ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 3: Privacy Policy
                InfoSectionCard(
                    title = "Privacy Policy",
                    icon = Icons.Default.Lock,
                    expanded = expandedSection == 2,
                    onToggle = { expandedSection = if (expandedSection == 2) -1 else 2 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Your privacy is important to us.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        val points = listOf(
                            "Akademic is primarily an offline application.",
                            "Academic records, grades, subjects, and related information are stored locally on your device.",
                            "We do not sell, share, or distribute your personal information to third parties.",
                            "Any files exported by users (PDF, Word, PowerPoint, or printed documents) are managed solely by the user.",
                            "Limited technical information may be collected to improve application performance and stability when applicable.",
                            "Users are responsible for protecting their devices and maintaining backups of their data.",
                            "By using Akademic, you agree to this Privacy Policy."
                        )
                        points.forEachIndexed { index, point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 4: Contact Us
                InfoSectionCard(
                    title = "Contact Us",
                    icon = Icons.Default.Share,
                    expanded = expandedSection == 3,
                    onToggle = { expandedSection = if (expandedSection == 3) -1 else 3 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "For inquiries, feedback, suggestions, or support, please contact us through:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                        
                        Text(
                            text = "Facebook Page:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        uriHandler.openUri("https://www.facebook.com/Akademic1?utm_source=chatgpt.com")
                                    } catch (e: Exception) {
                                        // safety ignore
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Akademic Official Page",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = "Developer: XCDeveloper\nWe appreciate your feedback and are committed to continuously improving Akademic for students.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E1B32))
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun InfoSectionCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (expanded) 90f else 0f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B0609),
            contentColor = Color(0xFFFFFDF0)
        ),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFDF3)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFFD4AF37).copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp).rotate(rotationState)
                )
            }
            if (expanded) {
                HorizontalDivider(color = Color(0x10D4AF37))
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsCard(
    context: Context,
    scheduleItems: List<ScheduleItem>
) {
    val prefs = remember { context.getSharedPreferences("akademic_notifications", Context.MODE_PRIVATE) }
    var notify30Mins by remember { mutableStateOf(prefs.getBoolean("notify_30_mins", true)) }
    var notify1Hour by remember { mutableStateOf(prefs.getBoolean("notify_1_hour", false)) }

    // Check for Post Notifications permission dynamic state
    var hasPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val requestPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Reminders enabled successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Alarms won't show popups.", Toast.LENGTH_LONG).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B0609),
            contentColor = Color(0xFFFFFDF0)
        ),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "CLASS SCHEDULE ALERTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFFD4AF37)
                )
            }

            Text(
                text = "Keep track of your lectures with automated wakeup popup notification reminders.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFFDF0).copy(alpha = 0.7f),
                lineHeight = 15.sp
            )

            HorizontalDivider(color = Color(0xFFD4AF37).copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 30 mins toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val newValue = !notify30Mins
                            notify30Mins = newValue
                            prefs.edit().putBoolean("notify_30_mins", newValue).apply()
                            com.example.notification.AlarmScheduler.rescheduleAllAlarms(context, scheduleItems)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = notify30Mins,
                        onCheckedChange = { newValue ->
                            notify30Mins = newValue
                            prefs.edit().putBoolean("notify_30_mins", newValue).apply()
                            com.example.notification.AlarmScheduler.rescheduleAllAlarms(context, scheduleItems)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF9E1B32),
                            uncheckedColor = Color(0xFFD4AF37).copy(alpha = 0.5f),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "30m Before",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFFDF0)
                    )
                }

                // 1 hour toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val newValue = !notify1Hour
                            notify1Hour = newValue
                            prefs.edit().putBoolean("notify_1_hour", newValue).apply()
                            com.example.notification.AlarmScheduler.rescheduleAllAlarms(context, scheduleItems)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = notify1Hour,
                        onCheckedChange = { newValue ->
                            notify1Hour = newValue
                            prefs.edit().putBoolean("notify_1_hour", newValue).apply()
                            com.example.notification.AlarmScheduler.rescheduleAllAlarms(context, scheduleItems)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF9E1B32),
                            uncheckedColor = Color(0xFFD4AF37).copy(alpha = 0.5f),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "1 Hour Before",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFFDF0)
                    )
                }
            }

            if (!hasPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E1B32),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Authorize Popup Alerts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StudyModeFullScreen(
    activeTheme: com.example.ui.theme.AppTheme,
    onExit: () -> Unit
) {
    var secondsElapsed by remember { mutableStateOf(0L) }
    
    // Increment timer every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val hours = secondsElapsed / 3600
    val minutes = (secondsElapsed % 3600) / 60
    val secs = secondsElapsed % 60
    val timerString = String.format("%02d:%02d:%02d", hours, minutes, secs)

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(1.1f),
                tint = primaryColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "STUDY MODE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp
                ),
                color = secondaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Screen will remain awake indefinitely",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Pulse Timer Card
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = secondaryColor.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.5.dp, secondaryColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timerString,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = secondaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FOCUS DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceColor.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .height(52.dp)
                    .testTag("exit_study_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "BACK TO NORMAL",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    context: Context
) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<com.example.data.Task?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val incompleteTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tasks header with action count & Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TO-DO & TASKS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = secondaryColor
                )
                Text(
                    text = "${incompleteTasks.size} pending reminders",
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }

            Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("add_task_fab_trigger")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Task Icon",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("ADD TASK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = onSurfaceColor.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "All Caught Up!",
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor.copy(alpha = 0.4f)
                    )
                    Text(
                        "Add offline tasks & custom reminders to keep on track",
                        fontSize = 12.sp,
                        color = onSurfaceColor.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (incompleteTasks.isNotEmpty()) {
                    item {
                        Text(
                            "PENDING TASKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(incompleteTasks.size) { index ->
                        val task = incompleteTasks[index]
                        TaskItemRow(
                            task = task,
                            onToggleComplete = {
                                viewModel.updateTask(task.copy(isCompleted = !task.isCompleted), context)
                            },
                            onEdit = { taskToEdit = task },
                            onDelete = { viewModel.deleteTask(task, context) },
                            secondaryColor = secondaryColor,
                            onSurfaceColor = onSurfaceColor
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "COMPLETED TASKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor.copy(alpha = 0.4f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(completedTasks.size) { index ->
                        val task = completedTasks[index]
                        TaskItemRow(
                            task = task,
                            onToggleComplete = {
                                viewModel.updateTask(task.copy(isCompleted = !task.isCompleted), context)
                            },
                            onEdit = { taskToEdit = task },
                            onDelete = { viewModel.deleteTask(task, context) },
                            secondaryColor = secondaryColor,
                            onSurfaceColor = onSurfaceColor
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddOrEditTaskModal(
            task = null,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, time, day, hasRem ->
                val newTask = com.example.data.Task(
                    id = 0,
                    title = title,
                    description = desc,
                    reminderTime = time,
                    reminderDayOfWeek = day,
                    isReminderEnabled = hasRem,
                    isCompleted = false
                )
                viewModel.addTask(newTask, context)
                showAddTaskDialog = false
            },
            secondaryColor = secondaryColor
        )
    }

    taskToEdit?.let { task ->
        AddOrEditTaskModal(
            task = task,
            onDismiss = { taskToEdit = null },
            onConfirm = { title, desc, time, day, hasRem ->
                val updated = task.copy(
                    title = title,
                    description = desc,
                    reminderTime = time,
                    reminderDayOfWeek = day,
                    isReminderEnabled = hasRem
                )
                viewModel.updateTask(updated, context)
                taskToEdit = null
            },
            secondaryColor = secondaryColor
        )
    }
}

@Composable
fun TaskItemRow(
    task: com.example.data.Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    secondaryColor: Color,
    onSurfaceColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted) secondaryColor.copy(alpha = 0.15f) else secondaryColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completed Checkbox
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Toggle Completeness",
                        tint = if (task.isCompleted) secondaryColor else onSurfaceColor.copy(alpha = 0.4f)
                    )
                }

                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (task.isCompleted) onSurfaceColor.copy(alpha = 0.5f) else onSurfaceColor
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceColor.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (task.isReminderEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = secondaryColor
                            )
                            Text(
                                text = "${task.reminderDayOfWeek} | ${task.reminderTime}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = secondaryColor
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = onSurfaceColor.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "Reminder Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceColor.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Edit & Delete actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task Det",
                        tint = secondaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task Det",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditTaskModal(
    task: com.example.data.Task?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, time: String, day: String, isReminderEnabled: Boolean) -> Unit,
    secondaryColor: Color
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var reminderTime by remember { mutableStateOf(task?.reminderTime ?: "08:00") }
    var reminderDay by remember { mutableStateOf(task?.reminderDayOfWeek ?: "Daily") }
    var isReminderEnabled by remember { mutableStateOf(task?.isReminderEnabled ?: true) }

    val daysOptions = listOf("Daily", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.dp, secondaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (task == null) "CREATE SYSTEM TASK" else "UPDATE SYSTEM TASK",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    ),
                    color = secondaryColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Details / Optional Note") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                // Reminder Enable switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = secondaryColor
                        )
                        Text(
                            text = "Trigger System Alarm", 
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = secondaryColor,
                            checkedTrackColor = secondaryColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("task_reminder_switch")
                    )
                }

                if (isReminderEnabled) {
                    HorizontalDivider(color = secondaryColor.copy(alpha = 0.15f))

                    // Row for Day and Timepicker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Day choice drop list
                        var showDayDropdown by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier.weight(1.1f)
                        ) {
                            OutlinedButton(
                                onClick = { showDayDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(reminderDay, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            DropdownMenu(
                                expanded = showDayDropdown,
                                onDismissRequest = { showDayDropdown = false }
                            ) {
                                daysOptions.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d) },
                                        onClick = {
                                            reminderDay = d
                                            showDayDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Time input (HH:mm formatting simple text)
                        OutlinedTextField(
                            value = reminderTime,
                            onValueChange = { reminderTime = it },
                            label = { Text("Alarm Time") },
                            singleLine = true,
                            placeholder = { Text("08:00") },
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("task_time_input")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, description, reminderTime, reminderDay, isReminderEnabled)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text("SAVE TASK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


