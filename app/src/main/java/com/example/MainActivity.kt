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
            var darkThemeState by remember { mutableStateOf(true) }
            
            MyApplicationTheme(darkTheme = darkThemeState) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRootContent(
                        darkTheme = darkThemeState,
                        onThemeToggle = { darkThemeState = !darkThemeState }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppRootContent(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
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
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle,
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
                    painter = painterResource(id = R.drawable.img_msu_logo_1782014499885),
                    contentDescription = "Akademic Circular Logo Medal",
                    modifier = Modifier
                        .size(190.dp)
                        .scale(scaleFactor)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFFD4AF37), CircleShape),
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
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
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

    // Screen width check for adaptive layouts
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_msu_logo_1782014499885),
                            contentDescription = "Logo Thumbnail",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFD4AF37), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            when (selectedSection) {
                                0 -> "AKADEMIC"
                                1 -> "CLASS SCHEDULE"
                                else -> "AKADEMIC REPORT"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFFD4AF37)
                        )
                    }
                },
                actions = {
                    // Theme toggler
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Theme Toggle Selection Icon",
                            tint = Color(0xFFD4AF37)
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
                            selectedIconColor = Color(0xFFD4AF37),
                            selectedTextColor = Color(0xFFD4AF37),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule Tab") },
                        label = { Text("Schedule") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFD4AF37),
                            selectedTextColor = Color(0xFFD4AF37),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Report Tab") },
                        label = { Text("Report") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFD4AF37),
                            selectedTextColor = Color(0xFFD4AF37),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.15f)
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
                            selectedIconColor = Color(0xFFD4AF37),
                            selectedTextColor = Color(0xFFD4AF37),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.15f)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationRailItem(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule Tab") },
                        label = { Text("Schedule") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFFD4AF37),
                            selectedTextColor = Color(0xFFD4AF37),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.15f)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationRailItem(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Report Tab") },
                        label = { Text("Report") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFFD4AF37),
                            selectedTextColor = Color(0xFFD4AF37),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.15f)
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
                    onDeleteSemesterClick = { viewModel.deleteSemester(it) },
                    onAddCourseClick = { showAddCourseDialog = true },
                    onDeleteCourseClick = { viewModel.deleteCourse(it) },
                    onAddScheduleClick = { showAddScheduleDialog = true },
                    onDeleteScheduleItem = { viewModel.deleteScheduleItem(it) },
                    viewModel = viewModel,
                    context = context
                )
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

    if (showAddScheduleDialog) {
        AddScheduleModal(
            onDismiss = { showAddScheduleDialog = false },
            onConfirm = { title, code, day, start, end, room, color ->
                viewModel.addScheduleItem(title, code, day, start, end, room, color)
                showAddScheduleDialog = false
            }
        )
    }

    if (showSharePreviewDialog) {
        SharePreviewSheet(
            semesters = semesters,
            courses = courses,
            scheduleItems = scheduleItems,
            onDismiss = { showSharePreviewDialog = false }
        )
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
    onDeleteSemesterClick: (Int) -> Unit,
    onAddCourseClick: () -> Unit,
    onDeleteCourseClick: (Int) -> Unit,
    onAddScheduleClick: () -> Unit,
    onDeleteScheduleItem: (Int) -> Unit,
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
                    onDeleteSemesterClick = onDeleteSemesterClick,
                    onAddCourseClick = onAddCourseClick,
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TodayScheduleWidget(
                    scheduleItems = scheduleItems,
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                    onAddScheduleClick = onAddScheduleClick,
                    onDeleteScheduleItem = onDeleteScheduleItem,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        else -> {
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
                            painter = painterResource(id = R.drawable.img_msu_logo_1782014499885),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFFD4AF37), CircleShape),
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
                                shareIntent = false
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
                                shareIntent = true
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
    onDeleteSemesterClick: (Int) -> Unit,
    onAddCourseClick: () -> Unit,
    onDeleteCourseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSemester = semesters.find { it.id == selectedSemId }
    val semesterCourses = courses.filter { it.semesterId == selectedSemId }

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

                        Row {
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        semesterCourses.forEach { course ->
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
                            painter = painterResource(id = R.drawable.img_msu_logo_1782014499885),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFFD4AF37), CircleShape),
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
                            shareAcademicRecordAsImage(context, semesters, courses, scheduleItems) {
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

        // Create a gorgeous high resolution custom card
        val width = 1000
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Backdrop paints
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#15080A") // Dark Crimson Charcoal
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Radial gold glow effect
        val glowPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#2A0E212D") // Deep gold dust
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, 250f, 350f, glowPaint)

        // Golden Outer Border
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D4AF37") // Academic Gold
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawRoundRect(20f, 20f, width - 20f, height - 20f, 40f, 40f, borderPaint)

        // Internal gold accent border line
        val thinBorderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 100
        }
        canvas.drawRoundRect(35f, 35f, width - 35f, height - 35f, 30f, 30f, thinBorderPaint)

        // Content Paint Setup
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Title text
        textPaint.color = android.graphics.Color.parseColor("#D4AF37")
        textPaint.textSize = 52f
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("AKADEMIC REPORT", width / 2f, 130f, textPaint)

        // Subtitle slogan
        textPaint.color = android.graphics.Color.parseColor("#FFFDF0")
        textPaint.textSize = 24f
        canvas.drawText("OFFLINE ACADEMY GWA TRANSCRIPT", width / 2f, 175f, textPaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D4AF37")
            strokeWidth = 2f
            alpha = 120
        }
        canvas.drawLine(100f, 220f, width - 100f, 220f, dividerPaint)

        // GPA circle container
        val gpaCirclePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#9B1B1B") // MSU Crimson
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, 380f, 110f, gpaCirclePaint)
        canvas.drawCircle(width / 2f, 380f, 110f, borderPaint)

        // GPA Value text
        val valuePaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#FFFDF6")
            textAlign = Paint.Align.CENTER
            textSize = 72f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText(String.format(Locale.US, "%.2f", overallGpa), width / 2f, 395f, valuePaint)

        val gpaLabelPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
            textSize = 22f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("OVERALL GWA (MSU SCALE)", width / 2f, 440f, gpaLabelPaint)

        // Stats Labels
        textPaint.textSize = 28f
        textPaint.color = android.graphics.Color.parseColor("#FFFDF0")
        canvas.drawText("Total Credits: ${totalCredits.toInt()} cr", 180f, 370f, textPaint)
        canvas.drawText("Semesters: ${semesters.size}", 180f, 410f, textPaint)

        canvas.drawText("Active Classes: ${scheduleItems.size}", width - 180f, 390f, textPaint)

        // Lower schedules section header
        canvas.drawLine(100f, 530f, width - 100f, 530f, dividerPaint)
        textPaint.color = android.graphics.Color.parseColor("#D4AF37")
        textPaint.textSize = 36f
        canvas.drawText("WEEKLY CLASSES SCHEDULE", width / 2f, 580f, textPaint)

        // Draw schedule rows
        textPaint.color = android.graphics.Color.parseColor("#FFFDF6")
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.LEFT

        var yPos = 640f
        if (scheduleItems.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = android.graphics.Color.parseColor("#8E8E8E")
            canvas.drawText("No offline classes scheduled. Use Acads Planner to add.", width / 2f, yPos + 60f, textPaint)
        } else {
            scheduleItems.take(6).forEach { item ->
                // Draw Day/Time
                textPaint.color = android.graphics.Color.parseColor("#D4AF37")
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                canvas.drawText("${item.dayOfWeek.take(3).uppercase()} ${item.startTime}", 100f, yPos, textPaint)

                // Draw Subject Name
                textPaint.color = android.graphics.Color.parseColor("#FFFDF6")
                textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                val infoText = "${item.code}: ${item.title} [${item.room}]"
                val truncated = if (infoText.length > 40) infoText.take(38) + "..." else infoText
                canvas.drawText(truncated, 320f, yPos, textPaint)

                yPos += 540f / 11f // Even grid rows
            }
        }

        // Footer Brand slogan
        val footerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
            textSize = 20f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("ONE VISION • ONE VOICE • ONE MSUPORTAL", width / 2f, 940f, footerPaint)

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
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Semester", fontWeight = FontWeight.ExtraBold, color = Color(0xFFCF9E26)) },
        text = {
            Column {
                Text("Enter label for the new educational semester term.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                Text("Save Term", fontWeight = FontWeight.Bold)
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
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var creditsStr by remember { mutableStateOf("3") }
    var selectedGrade by remember { mutableStateOf("1.00") }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Course Grade", fontWeight = FontWeight.ExtraBold, color = Color(0xFFCF9E26)) },
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
                Text("Confirm Grade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddScheduleModal(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("Monday") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var room by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#9E1B32") }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val colors = listOf("#9E1B32", "#CF9E26", "#2E7D32", "#1565C0", "#D81B60", "#F4511E")

    var dayExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Class Lecture", fontWeight = FontWeight.ExtraBold, color = Color(0xFFCF9E26)) },
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
                Text("Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
