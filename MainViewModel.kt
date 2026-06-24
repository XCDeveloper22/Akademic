package com.example

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FloatingService : Service() {

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "mochi_assistive_touch_channel"

    private var notificationAnimationRunnable: Runnable? = null
    private var animationFrame = 0
    private val animationFrames = listOf(
        "(＾▽＾) Mochi is smiling! ✨",
        "(・ω・) Mochi is waving! 👋",
        "(・_・ ) Mochi is looking around... 👀",
        "(・∀・) Mochi is cheering you on! 🌟",
        "( -_•) Mochi is winking! 😉",
        "(＾▽＾) Mochi is happy! 🎉",
        "(*＾3＾) Mochi blows a kiss! 💖",
        "(●'▽'●) Mochi is excited! 🍀",
        "(๑•̀ㅂ•́) Mochi says: Keep it up! 💪"
    )

    private val screenReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    mochiView?.currentMannerism = "sleeping"
                    updateMochiNotification()
                }
                Intent.ACTION_SCREEN_ON -> {
                    mochiView?.currentMannerism = "yawning"
                    mochiView?.triggerJump()
                    updateMochiNotification()
                }
                Intent.ACTION_USER_PRESENT -> {
                    mochiView?.currentMannerism = "joyful"
                    mochiView?.triggerJump()
                    updateMochiNotification()
                }
            }
        }
    }

    private lateinit var windowManager: WindowManager
    private var floatingView: FrameLayout? = null
    private var menuView: FrameLayout? = null
    private var mochiView: MochiTouchView? = null
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var menuParams: WindowManager.LayoutParams

    private val handler = Handler(Looper.getMainLooper())
    private var clockRunnable: Runnable? = null
    private var timerRunnable: Runnable? = null

    // State
    private var isTimerRunning = false
    private var secondsElapsed = 0L

    // Prefs
    private val timezonePrefs by lazy { getSharedPreferences("akademic_timezone_prefs", Context.MODE_PRIVATE) }
    private val studyPrefs by lazy { getSharedPreferences("akademic_study_prefs", Context.MODE_PRIVATE) }

    // Dynamic UI elements inside menu overlay
    private var tvClockTime: TextView? = null
    private var tvClockZone: TextView? = null
    private var tvTimerDisplay: TextView? = null
    private var tvTimerStatus: TextView? = null
    private var btnPlayPause: ImageView? = null
    private var tvPlayPauseIcon: TextView? = null
    private var tvPlayPauseLabel: TextView? = null

    // Theme changes observer
    private val themeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "selected_theme") {
            handler.post {
                rebuildFloatingUi()
            }
        }
    }

    private fun rebuildFloatingUi() {
        val fView = floatingView ?: return
        val currentX = params.x
        val currentY = params.y
        val isMenuVisible = menuView?.visibility == View.VISIBLE

        // Remove old views
        floatingView?.let { 
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() } 
        }
        menuView?.let { 
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() } 
        }

        // Recreate them with fresh themes
        createFloatingButton()
        createFloatingMenu()

        // Restore position
        params.x = currentX
        params.y = currentY
        try {
            windowManager.updateViewLayout(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isMenuVisible) {
            menuView?.visibility = View.VISIBLE
            floatingView?.visibility = View.GONE
        } else {
            menuView?.visibility = View.GONE
            floatingView?.visibility = View.VISIBLE
        }
    }

    data class ThemeColors(
        val backgroundStart: String,
        val backgroundEnd: String,
        val primary: String,
        val accent: String,
        val dimBackdrop: String
    )

    private fun getThemeColors(): ThemeColors {
        val themePrefs = getSharedPreferences("akademic_theme_prefs", Context.MODE_PRIVATE)
        val savedThemeStr = themePrefs.getString("selected_theme", "DARK_MODE") ?: "DARK_MODE"
        val theme = try { com.example.ui.theme.AppTheme.valueOf(savedThemeStr) } catch(e: Exception) { com.example.ui.theme.AppTheme.DARK_MODE }
        
        return when (theme) {
            com.example.ui.theme.AppTheme.DARK_MODE -> ThemeColors(
                backgroundStart = "#380811", // Velvet Crimson Dark
                backgroundEnd = "#130608",
                primary = "#9B1B1B",
                accent = "#D4AF37",
                dimBackdrop = "#9D0F0204"
            )
            com.example.ui.theme.AppTheme.AKADEMIC_BLUE -> ThemeColors(
                backgroundStart = "#1E3A8A", // Celestial Sapphire
                backgroundEnd = "#0B132B",
                primary = "#3B82F6",
                accent = "#64D2FF",
                dimBackdrop = "#9D030A1F"
            )
            com.example.ui.theme.AppTheme.FOREST_GREEN -> ThemeColors(
                backgroundStart = "#1B5E20", // Ethereal Emerald
                backgroundEnd = "#0D1F10",
                primary = "#4CAF50",
                accent = "#81C784",
                dimBackdrop = "#9D041206"
            )
            com.example.ui.theme.AppTheme.SUNSET_ORANGE -> ThemeColors(
                backgroundStart = "#D84315", // Saffron Aurora
                backgroundEnd = "#1A0E0B",
                primary = "#F97316",
                accent = "#FFB74D",
                dimBackdrop = "#9D150B08"
            )
            com.example.ui.theme.AppTheme.PURPLE_SCHOLAR -> ThemeColors(
                backgroundStart = "#6D28D9", // Mystic Amethyst
                backgroundEnd = "#110A1C",
                primary = "#8B5CF6",
                accent = "#D6BCFA",
                dimBackdrop = "#9D0B0515"
            )
            com.example.ui.theme.AppTheme.PINK_SCHOLAR -> ThemeColors(
                backgroundStart = "#9D174D", // Plum Velvet
                backgroundEnd = "#1F1116",
                primary = "#DB2777",
                accent = "#F472B6",
                dimBackdrop = "#9D1A0A10"
            )
            com.example.ui.theme.AppTheme.MIDNIGHT_BLACK -> ThemeColors(
                backgroundStart = "#222222", // Stark Obsidian
                backgroundEnd = "#000000",
                primary = "#8E8E93",
                accent = "#FFFFFF",
                dimBackdrop = "#9D050505"
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Check overlay permission first to prevent SYSTEM_ALERT_WINDOW AppOps failures of adding window
        if (!android.provider.Settings.canDrawOverlays(this)) {
            val prefs = getSharedPreferences("akademic_assistive_touch_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_service_running", false).apply()
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Mark service as running
        val prefs = getSharedPreferences("akademic_assistive_touch_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_service_running", true).apply()

        // Register theme changes listener
        val themePrefs = getSharedPreferences("akademic_theme_prefs", Context.MODE_PRIVATE)
        themePrefs.registerOnSharedPreferenceChangeListener(themeListener)

        createFloatingButton()
        createFloatingMenu()

        startClockUpdates()
        startTimerUpdates()

        // Start Foreground Service and Notification animations
        startForegroundWithMochi()
        startNotificationAnimation()

        // Register screen state broadcast receiver
        val screenFilter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, screenFilter)
    }

    private fun startForegroundWithMochi() {
        val channelName = "Mochi Stay-Awake Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = android.app.NotificationChannel(
                CHANNEL_ID,
                channelName,
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification = buildMochiNotification(getAnimationText())
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startNotificationAnimation() {
        notificationAnimationRunnable = object : Runnable {
            override fun run() {
                updateMochiNotification()
                handler.postDelayed(this, 2000)
            }
        }
        handler.post(notificationAnimationRunnable!!)
    }

    private fun updateMochiNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notification = buildMochiNotification(getAnimationText())
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun getAnimationText(): String {
        if (isTimerRunning) {
            val hours = secondsElapsed / 3600
            val minutes = (secondsElapsed % 3600) / 60
            val secs = secondsElapsed % 60
            val timerString = String.format("%02d:%02d:%02d", hours, minutes, secs)
            return "⏳ Focus Mode Active: $timerString 🔥"
        }
        val frame = animationFrames[animationFrame % animationFrames.size]
        animationFrame++
        return frame
    }

    private fun buildMochiNotification(text: String): android.app.Notification {
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }

        return builder
            .setContentTitle("Akademic • Mochi Assistant")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
            .setCategory(android.app.Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun createFloatingButton() {
        val themeColors = getThemeColors()
        floatingView = FrameLayout(this)

        // Circle Container
        val container = FrameLayout(this)
        val containerBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            val baseColor = Color.parseColor(themeColors.backgroundEnd)
            val translucentColor = (baseColor and 0x00FFFFFF) or (0xE0 shl 24)
            setColor(translucentColor) // Dynamic Translucent Background
            setStroke(dpToPx(2.5f), Color.parseColor(themeColors.accent)) // Dynamic Accent Stroke
        }
        container.background = containerBg
        container.clipToOutline = true

        // Mochi's model face instead of old logo
        val mTouchView = MochiTouchView(this).apply {
            currentMannerism = "smiling"
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }
        mochiView = mTouchView

        val designPadding = dpToPx(6f) // slightly more padding so Mochi fits beautifully
        container.setPadding(designPadding, designPadding, designPadding, designPadding)

        val imageParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(mTouchView, imageParams)

        // Wrapper params
        val buttonSizePx = dpToPx(60f)
        val wrapperParams = FrameLayout.LayoutParams(buttonSizePx, buttonSizePx)
        floatingView?.addView(container, wrapperParams)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val screenWidth = resources.displayMetrics.widthPixels
        val defaultXPx = screenWidth - buttonSizePx - dpToPx(16f) // Position overlay in the right margin by default

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or 
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = defaultXPx
            y = resources.displayMetrics.heightPixels / 3 // Placed around upper middle part vertically
        }

        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            // Gracefully handle app ops restriction
            Toast.makeText(this, "System blocked overlay rendering.", Toast.LENGTH_SHORT).show()
        }

        // Draggable touch mechanism
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var clickTime = 0L

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        clickTime = System.currentTimeMillis()
                        container.alpha = 0.7f
                        
                        // Trigger Mochi gets called state (ice cold color change) and jump!
                        mochiView?.triggerCalled()
                        
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager.updateViewLayout(floatingView, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        container.alpha = 1.0f
                        val duration = System.currentTimeMillis() - clickTime
                        val distance = Math.hypot(
                            (event.rawX - initialTouchX).toDouble(),
                            (event.rawY - initialTouchY).toDouble()
                        )
                        if (duration < 250 && distance < 15) {
                            toggleMenu()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun createFloatingMenu() {
        val themeColors = getThemeColors()
        
        // Full screen Dim Backdrop for Tap-To-Dismiss functionality like iOS Assistive Touch
        menuView = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor(themeColors.dimBackdrop)) // Dynamic luxury semi-transparent backdrop
            setOnClickListener {
                visibility = View.GONE
                floatingView?.visibility = View.VISIBLE
            }
        }
        menuView?.visibility = View.GONE

        val menuSizeDp = 300f
        val menuSizePx = dpToPx(menuSizeDp)

        // Cosmic Dark Circular Card Layout
        val circleLayout = FrameLayout(this).apply {
            val bg = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor(themeColors.backgroundStart), Color.parseColor(themeColors.backgroundEnd))
            ).apply {
                shape = GradientDrawable.OVAL
                setStroke(dpToPx(2f), Color.parseColor(themeColors.accent)) // Dynamic Accent Stroke
            }
            background = bg
            setOnClickListener {
                // Intercept clicks on the container itself to prevent dismissing the menu
            }
        }

        // Concentric decorative ring
        val ringSize = dpToPx(200f)
        val ringView = View(this).apply {
            val ringBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.TRANSPARENT)
                val baseAccent = Color.parseColor(themeColors.accent)
                val subtleAccent = (baseAccent and 0x00FFFFFF) or (0x35 shl 24)
                setStroke(dpToPx(1f), subtleAccent) // Dynamic subtle ring
            }
            background = ringBg
        }
        circleLayout.addView(ringView, FrameLayout.LayoutParams(ringSize, ringSize).apply {
            gravity = Gravity.CENTER
        })

        // Central Close trigger button (styled beautifully)
        val centerSizePx = dpToPx(60f)
        val centerBtn = FrameLayout(this).apply {
            val centerBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(themeColors.primary)) // Dynamic primary color
                setStroke(dpToPx(1.5f), Color.parseColor(themeColors.accent)) // Dynamic accent stroke
            }
            background = centerBg
            setOnClickListener {
                menuView?.visibility = View.GONE
                floatingView?.visibility = View.VISIBLE
            }
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> view.alpha = 0.6f
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1.0f
                }
                false
            }
        }
        val closeIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
        }
        centerBtn.addView(closeIcon, FrameLayout.LayoutParams(dpToPx(24f), dpToPx(24f)).apply {
            gravity = Gravity.CENTER
        })
        circleLayout.addView(centerBtn, FrameLayout.LayoutParams(centerSizePx, centerSizePx).apply {
            gravity = Gravity.CENTER
        })

        // Realtime Manila Clock Display Widget (positioned inside circular bounds above center)
        val clockLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        tvClockTime = TextView(this).apply {
            text = "--:--:--"
            setTextColor(Color.parseColor("#FFFDF0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        tvClockZone = TextView(this).apply {
            text = "Manila Time"
            setTextColor(Color.parseColor(themeColors.accent)) // Dynamic accent text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER
        }
        clockLayout.addView(tvClockTime)
        clockLayout.addView(tvClockZone)
        circleLayout.addView(clockLayout, FrameLayout.LayoutParams(dpToPx(140f), FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = dpToPx(80f)
        })

        // Stay-Awake Study Timer Display Widget (positioned inside circular bounds below center)
        val timerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        tvTimerDisplay = TextView(this).apply {
            text = "00:00:00"
            setTextColor(Color.parseColor("#FFFDF0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        tvTimerStatus = TextView(this).apply {
            text = "PAUSED"
            setTextColor(Color.parseColor("#E53935"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER
        }
        timerLayout.addView(tvTimerDisplay)
        timerLayout.addView(tvTimerStatus)
        circleLayout.addView(timerLayout, FrameLayout.LayoutParams(dpToPx(140f), FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = dpToPx(80f)
        })

        // Add 4 Cardinal orbital quick-access buttons
        val rCardDp = 104f
        val cardinalBg = themeColors.backgroundStart
        val cardinalBorder = themeColors.accent

        // 1. Akademic Board: Left (180 degrees)
        addOrbitalButton(circleLayout, "🎓", "BOARD", 180.0, rCardDp, 56f, cardinalBg, "#FFFDF0", cardinalBorder) {
            launchMainActivity(false, 0)
        }

        // 2. Class Schedule: Top (270 degrees)
        addOrbitalButton(circleLayout, "📅", "SCHEDULE", 270.0, rCardDp, 56f, cardinalBg, "#FFFDF0", cardinalBorder) {
            launchMainActivity(false, 1)
        }

        // 3. Tasks & Reminders: Right (0 degrees)
        addOrbitalButton(circleLayout, "📝", "TASKS", 0.0, rCardDp, 56f, cardinalBg, "#FFFDF0", cardinalBorder) {
            launchMainActivity(false, 3)
        }

        // 4. Export Report: Bottom (90 degrees)
        addOrbitalButton(circleLayout, "📤", "EXPORT", 90.0, rCardDp, 56f, cardinalBg, "#FFFDF0", cardinalBorder) {
            launchMainActivity(false, 2)
        }

        // Add 4 diagonal supplementary actions inside
        val rDiagDp = 96f
        val diagBg = "#30" + themeColors.accent.substring(1)
        val diagBorder = "#40" + themeColors.accent.substring(1)

        // 5. Settings (Bottom-Left / 135 degrees)
        addOrbitalButton(circleLayout, "⚙️", "SETUP", 135.0, rDiagDp, 48f, diagBg, "#B0FFFDF0", diagBorder) {
            launchMainActivity(false, 4)
        }

        // 6. Shutdown (Top-Left / 225 degrees)
        val shutdownBg = "#FF" + themeColors.primary.substring(1)
        addOrbitalButton(circleLayout, "🔌", "SHUT", 225.0, rDiagDp, 48f, shutdownBg, "#FFFDF0", "#FFE53935") {
            stopSelf()
            Toast.makeText(this@FloatingService, "Assistive Touch closed.", Toast.LENGTH_SHORT).show()
        }

        // 7. Study Stay-Awake (Top-Right / 315 degrees)
        val studyBg = "#FF" + themeColors.backgroundStart.substring(1)
        addOrbitalButton(circleLayout, "⏳", "STUDY", 315.0, rDiagDp, 48f, studyBg, "#FFFDF0", themeColors.accent) {
            launchMainActivity(true)
        }

        // 8. Live play/pause focus toggle (Bottom-Right / 45 degrees)
        addOrbitalButton(circleLayout, "▶️", "PLAY", 45.0, rDiagDp, 48f, diagBg, "#B0FFFDF0", diagBorder, isPlayPause = true) {
            toggleTimerOnOff()
        }

        val mainCardParams = FrameLayout.LayoutParams(menuSizePx, menuSizePx).apply {
            gravity = Gravity.CENTER
        }
        menuView?.addView(circleLayout, mainCardParams)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or 
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager.addView(menuView, menuParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addOrbitalButton(
        parent: FrameLayout,
        emoji: String,
        label: String,
        angleDegrees: Double,
        radiusDp: Float,
        buttonSizeDp: Float,
        bgHex: String,
        textHex: String,
        accentHex: String,
        isPlayPause: Boolean = false,
        onClick: () -> Unit
    ) {
        val buttonSize = dpToPx(buttonSizeDp)
        val radiusPx = dpToPx(radiusDp)
        val parentSize = dpToPx(300f)
        val centerX = parentSize / 2
        val centerY = parentSize / 2

        val angleRad = Math.toRadians(angleDegrees)

        // Determine target center coordinates on screen layout
        val btnCenterX = centerX + radiusPx * Math.cos(angleRad)
        val btnCenterY = centerY + radiusPx * Math.sin(angleRad)

        val leftMargin = (btnCenterX - buttonSize / 2).toInt()
        val topMargin = (btnCenterY - buttonSize / 2).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(bgHex))
                setStroke(dpToPx(1.2f), Color.parseColor(accentHex))
            }
            background = shape
            setPadding(dpToPx(2f), dpToPx(2f), dpToPx(2f), dpToPx(2f))
            setOnClickListener {
                onClick()
            }
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> view.alpha = 0.5f
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1.0f
                }
                false
            }
        }

        val tvIcon = TextView(this).apply {
            text = emoji
            textSize = if (buttonSizeDp < 48f) 11f else 14f
            gravity = Gravity.CENTER
        }
        container.addView(tvIcon)

        val tvLabel = TextView(this).apply {
            text = label
            setTextColor(Color.parseColor(textHex))
            textSize = if (buttonSizeDp < 48f) 6.5f else 8f
            typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        val labelParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        labelParams.topMargin = dpToPx(1f)
        container.addView(tvLabel, labelParams)

        if (isPlayPause) {
            tvPlayPauseIcon = tvIcon
            tvPlayPauseLabel = tvLabel
        }

        val layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize).apply {
            this.leftMargin = leftMargin
            this.topMargin = topMargin
            this.gravity = Gravity.TOP or Gravity.START
        }
        parent.addView(container, layoutParams)
    }

    private fun toggleMenu() {
        val menu = menuView ?: return
        if (menu.visibility == View.VISIBLE) {
            menu.visibility = View.GONE
            floatingView?.visibility = View.VISIBLE
        } else {
            // Relocate standard button to center as anchor or simply render expanded overlay
            menu.visibility = View.VISIBLE
            floatingView?.visibility = View.GONE
        }
    }

    private fun launchMainActivity(enterStudyMode: Boolean, selectedSection: Int? = null) {
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            if (enterStudyMode) {
                putExtra("launch_study_mode", true)
            }
            if (selectedSection != null) {
                putExtra("selected_section", selectedSection)
            }
        }
        
        // Hide overlay menu so screen isn't obstructed
        menuView?.visibility = View.GONE
        floatingView?.visibility = View.VISIBLE

        startActivity(launchIntent)
    }

    // ----------------------------------------------------
    // REALTIME TIMEZONE CLOCK UPDATER
    // ----------------------------------------------------
    private fun startClockUpdates() {
        clockRunnable = object : Runnable {
            override fun run() {
                val selectedZoneIdStr = timezonePrefs.getString("selected_zone", "Asia/Manila") ?: "Asia/Manila"
                val is24Hour = timezonePrefs.getBoolean("is_24_hour", false)

                val zoneId = try {
                    ZoneId.of(selectedZoneIdStr)
                } catch (e: Exception) {
                    ZoneId.of("Asia/Manila")
                }

                val zonedDateTime = ZonedDateTime.now(zoneId)
                val formatPattern = if (is24Hour) "HH:mm:ss" else "hh:mm:ss a"
                val timeStr = zonedDateTime.format(DateTimeFormatter.ofPattern(formatPattern, java.util.Locale.US))

                tvClockTime?.text = timeStr
                tvClockZone?.text = "Timezone: $selectedZoneIdStr"

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(clockRunnable!!)
    }

    // ----------------------------------------------------
    // LIVE FOCUS TIMER SYNCHRONIZER
    // ----------------------------------------------------
    private fun startTimerUpdates() {
        // Load study mode preferences
        isTimerRunning = studyPrefs.getBoolean("is_running", false)
        secondsElapsed = studyPrefs.getLong("seconds_elapsed", 0L)
        val lastActive = studyPrefs.getLong("last_active_time", 0L)

        if (isTimerRunning && lastActive > 0L) {
            val diffSecs = (System.currentTimeMillis() - lastActive) / 1000L
            if (diffSecs > 0) {
                secondsElapsed += diffSecs
            }
        }

        timerRunnable = object : Runnable {
            override fun run() {
                // Read fresh values to keep in sync with main app if changed
                isTimerRunning = studyPrefs.getBoolean("is_running", false)
                val appSeconds = studyPrefs.getLong("seconds_elapsed", secondsElapsed)
                
                if (isTimerRunning) {
                    secondsElapsed++
                    // Regularly save progress
                    studyPrefs.edit()
                        .putLong("seconds_elapsed", secondsElapsed)
                        .putBoolean("is_running", true)
                        .putLong("last_active_time", System.currentTimeMillis())
                        .apply()
                } else {
                    secondsElapsed = appSeconds
                }

                // Render Timer String format
                val hours = secondsElapsed / 3600
                val minutes = (secondsElapsed % 3600) / 60
                val secs = secondsElapsed % 60
                val timerString = String.format("%02d:%02d:%02d", hours, minutes, secs)

                tvTimerDisplay?.text = timerString
                tvTimerStatus?.text = if (isTimerRunning) "RUNNING • STUDY MODE" else "PAUSED"
                btnPlayPause?.setImageResource(
                    if (isTimerRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
                tvPlayPauseIcon?.text = if (isTimerRunning) "⏸️" else "▶️"
                tvPlayPauseLabel?.text = if (isTimerRunning) "PAUSE" else "PLAY"

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun toggleTimerOnOff() {
        isTimerRunning = !isTimerRunning
        
        // Also ensure study mode is activated officially so active study state triggers inside main
        studyPrefs.edit()
            .putBoolean("is_running", isTimerRunning)
            .putBoolean("study_mode_active", true) // Ensure study mode persists
            .putLong("seconds_elapsed", secondsElapsed)
            .putLong("last_active_time", System.currentTimeMillis())
            .apply()

        btnPlayPause?.setImageResource(
            if (isTimerRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        tvTimerStatus?.text = if (isTimerRunning) "RUNNING • STUDY MODE" else "PAUSED"
        tvPlayPauseIcon?.text = if (isTimerRunning) "⏸️" else "▶️"
        tvPlayPauseLabel?.text = if (isTimerRunning) "PAUSE" else "PLAY"
    }

    private fun resetFocusTimer() {
        secondsElapsed = 0L
        studyPrefs.edit()
            .putLong("seconds_elapsed", 0L)
            .putLong("last_active_time", System.currentTimeMillis())
            .apply()
        tvTimerDisplay?.text = "00:00:00"
    }

    override fun onDestroy() {
        // Mark service as stopped
        val prefs = getSharedPreferences("akademic_assistive_touch_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_service_running", false).apply()

        // Unregister theme changes listener
        val themePrefs = getSharedPreferences("akademic_theme_prefs", Context.MODE_PRIVATE)
        themePrefs.unregisterOnSharedPreferenceChangeListener(themeListener)

        clockRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable?.let { handler.removeCallbacks(it) }
        notificationAnimationRunnable?.let { handler.removeCallbacks(it) }

        // Unregister screen receiver
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Remove foreground notification
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        floatingView?.let { 
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() } 
        }
        menuView?.let { 
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() } 
        }
        
        super.onDestroy()
    }
}
