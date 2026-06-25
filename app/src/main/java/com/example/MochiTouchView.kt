package com.example

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class MochiTouchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Massive list of all positive, negative, and neutral / situational mannerisms requested
    val mannerismsList = listOf(
        // Core/existing ones
        "smiling", "sleeping", "nap", "balloon", "running", "eating", "sad", "happy", "anxiety", 
        "joyful", "rain", "teeth", "cute", "talking", "dinner", "yawning", "bleee", "walking",
        // Positive Emotions / Mannerisms
        "cheerful", "excited", "enthusiastic", "energetic", "playful", "proud", "confident", "brave", 
        "relaxed", "calm", "peaceful", "content", "satisfied", "relieved", "hopeful", "optimistic", 
        "inspired", "motivated", "determined", "curious", "friendly", "affectionate", "loving", 
        "caring", "compassionate", "grateful", "amused", "laughing", "celebrating", "admiring", 
        "trusting", "respectful",
        // Negative Emotions / Mannerisms
        "crying", "sobbing", "depressed", "heartbroken", "lonely", "miserable", "hopeless", 
        "disappointed", "regretful", "guilty", "ashamed", "embarrassed", "angry", "furious", 
        "annoyed", "irritated", "frustrated", "resentful", "jealous", "envious", "bitter", 
        "worried", "nervous", "anxious", "stressed", "panicking", "fearful", "terrified", 
        "shocked", "startled", "confused", "suspicious", "distrustful", "disgusted", "sick", 
        "nauseous", "puking", "exhausted", "weak", "hurt", "grieving",
        // Neutral / Situational States
        "thinking", "concentrating", "observing", "listening", "waiting", "daydreaming", 
        "surprised", "idle", "sitting", "standing", "reading", "working"
    )

    var currentMannerism: String = "smiling"
        set(value) {
            field = value
            mannerismTime = 0f
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    // Breathing & Blinking state
    private var breathTime = 0f
    private var isBlinking = false
    private var blinkTimeCounter = 0

    // Called/Ice state
    private var calledTimeLeft = 0 // frames remaining in ice state

    // Mannerism progress counter
    private var mannerismTime = 0f
    private var mannerismCycleCounter = 0

    // Physics parameters for jump
    private var jumpOffset = 0f
    private var jumpVelocity = 0f
    private val gravity = 1.2f

    init {
        // Start 30 FPS animation loop
        post(object : Runnable {
            override fun run() {
                // Update breathing
                breathTime += 0.08f

                // Update mannerism time
                mannerismTime += 1f

                // Periodically change mannerisms randomly (approx. every 60 seconds - 2000 frames)
                mannerismCycleCounter++
                if (mannerismCycleCounter > 2000) {
                    mannerismCycleCounter = 0
                    currentMannerism = mannerismsList.random()
                }

                // Energetic/Joyful/Excited mannerisms jump automatically
                if ((currentMannerism == "joyful" || currentMannerism == "excited" || currentMannerism == "energetic" || currentMannerism == "celebrating") && (mannerismTime.toInt() % 45 == 0)) {
                    triggerJump()
                }

                // Decrement ice state timer
                if (calledTimeLeft > 0) {
                    calledTimeLeft--
                }

                // Update blinking
                blinkTimeCounter++
                if (!isBlinking && blinkTimeCounter > 150) {
                    if (Math.random() < 0.15) {
                        isBlinking = true
                        blinkTimeCounter = 0
                    }
                } else if (isBlinking) {
                    if (blinkTimeCounter > 4) {
                        isBlinking = false
                        blinkTimeCounter = 0
                    }
                }

                // Update jumping physics
                if (jumpOffset < 0f || jumpVelocity != 0f) {
                    jumpOffset += jumpVelocity
                    jumpVelocity += gravity
                    if (jumpOffset >= 0f) {
                        jumpOffset = 0f
                        jumpVelocity = 0f
                    }
                }

                invalidate()
                postDelayed(this, 30)
            }
        })
    }

    // Called when user touches her
    fun triggerCalled() {
        calledTimeLeft = 120 // ~4 seconds of icy cold state
        triggerJump()
        invalidate()
    }

    fun triggerJump() {
        jumpVelocity = -12f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        // 1. Calculations & Positioning
        val baseRadiusX = w * 0.38f
        val baseRadiusY = h * 0.42f

        // Balloon mannerism scale animation
        var balloonScale = 1.0f
        if (currentMannerism == "balloon") {
            val t = mannerismTime
            if (t < 140f) {
                // Squeeze-swell from 1.0 to 1.8
                balloonScale = 1.0f + 0.8f * (t / 140f)
            } else if (t < 155f) {
                // Pop back / collapse
                val ratio = (t - 140f) / 15f
                balloonScale = 1.8f - 0.8f * ratio
            } else {
                balloonScale = 1.0f
            }
        }

        // Running mannerism translation and wobble
        var runTransX = 0f
        var runWobbleY = 0f
        var runTiltAngle = 0f
        if (currentMannerism == "running") {
            val t = mannerismTime
            if (t < 60f) {
                // Run Left
                val ratio = t / 60f
                runTransX = -w * 0.25f * ratio
                runWobbleY = Math.abs(sin(t * 0.5f)) * -8f
                runTiltAngle = -10f
            } else if (t < 130f) {
                // Run Right
                val ratio = (t - 60f) / 70f
                runTransX = -w * 0.25f + (w * 0.5f * ratio)
                runWobbleY = Math.abs(sin(t * 0.5f)) * -8f
                runTiltAngle = 10f
            } else if (t < 170f) {
                // Run Back to Center
                val ratio = (t - 130f) / 40f
                runTransX = w * 0.25f - (w * 0.25f * ratio)
                runWobbleY = Math.abs(sin(t * 0.5f)) * -8f
                runTiltAngle = -5f
            } else {
                // Stand still and wave
                runTransX = 0f
                runWobbleY = 0f
                runTiltAngle = 0f
            }
        }

        // Anxiety shaking
        var anxietyShakeX = 0f
        var anxietyShakeY = 0f
        if (currentMannerism == "anxiety") {
            anxietyShakeX = (Math.random().toFloat() - 0.5f) * (w * 0.04f)
            anxietyShakeY = (Math.random().toFloat() - 0.5f) * (h * 0.04f)
        }

        // Apply animations inside canvas state
        canvas.save()
        canvas.translate(runTransX + anxietyShakeX, jumpOffset + runWobbleY + anxietyShakeY)
        if (runTiltAngle != 0f) {
            canvas.rotate(runTiltAngle, w / 2f, h / 2f)
        }

        val cx = w / 2f
        val cy = h / 2f + 2f

        // Breathing multipliers
        val squishX = (1f + 0.03f * sin(breathTime)) * balloonScale
        val squishY = (1f - 0.03f * sin(breathTime)) * balloonScale

        val radiusX = baseRadiusX * squishX
        val radiusY = baseRadiusY * squishY

        // --- DRAW ICE BACKDROP GLOW (If called in ice state) ---
        if (calledTimeLeft > 0) {
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#80DEEA")
            paint.strokeWidth = w * 0.08f
            paint.alpha = (100 + 50 * sin(breathTime * 2f)).toInt().coerceIn(0, 255)
            canvas.drawOval(RectF(cx - radiusX, cy - radiusY, cx + radiusX, cy + radiusY), paint)
        }

        // --- 1. DRAW BODY GRADIENT (Normal Phoenix OR Cold Icy Blue) ---
        val bodyRect = RectF(cx - radiusX, cy - radiusY, cx + radiusX, cy + radiusY)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        if (calledTimeLeft > 0) {
            // Cold icy dark blue gradient
            val iceGradient = LinearGradient(
                cx, cy - radiusY, cx, cy + radiusY,
                intArrayOf(Color.parseColor("#E0F7FA"), Color.parseColor("#00E5FF"), Color.parseColor("#0D47A1")),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = iceGradient
        } else {
            // Beautiful phoenix colors
            val phoenixGradient = LinearGradient(
                cx, cy - radiusY, cx, cy + radiusY,
                intArrayOf(Color.parseColor("#FBBF24"), Color.parseColor("#EA580C"), Color.parseColor("#9E1B32")),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = phoenixGradient
        }
        canvas.drawOval(bodyRect, paint)
        paint.shader = null

        // --- DRAW ICE CRYSTALS (If in cold ice state) ---
        if (calledTimeLeft > 0) {
            paint.color = Color.WHITE
            paint.alpha = 180
            paint.style = Paint.Style.FILL
            
            // Draw diamond ice stars on body
            fun drawIceSparkle(sx: Float, sy: Float, sz: Float) {
                path.reset()
                path.moveTo(sx, sy - sz)
                path.lineTo(sx + sz / 2, sy)
                path.lineTo(sx, sy + sz)
                path.lineTo(sx - sz / 2, sy)
                path.close()
                canvas.drawPath(path, paint)
            }
            drawIceSparkle(cx - radiusX * 0.4f, cy - radiusY * 0.4f, w * 0.08f)
            drawIceSparkle(cx + radiusX * 0.5f, cy - radiusY * 0.2f, w * 0.06f)
            drawIceSparkle(cx - radiusX * 0.3f, cy + radiusY * 0.3f, w * 0.07f)
            paint.alpha = 255
        }

        // --- 2. ROSY CHEEKS / BLUSH (Subtle pink, or slightly purple-ish if cold) ---
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        if (calledTimeLeft > 0) {
            paint.color = Color.parseColor("#B39DDB") // Lavender-purple cold blush
            paint.alpha = 140
        } else {
            paint.color = Color.parseColor("#FF8DA1") // Rosy cheek pink blush
            paint.alpha = 180
        }
        val cheekRadius = w * 0.11f
        val cheekOffsetY = radiusY * 0.14f
        val cheekOffsetX = radiusX * 0.52f
        canvas.drawCircle(cx - cheekOffsetX, cy + cheekOffsetY, cheekRadius, paint)
        canvas.drawCircle(cx + cheekOffsetX, cy + cheekOffsetY, cheekRadius, paint)
        paint.alpha = 255

        // --- 3. DRAW CUTE EYES ---
        paint.reset()
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#130608")
        paint.strokeWidth = w * 0.065f
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE

        val eyeWidth = w * 0.08f
        val eyeOffsetY = radiusY * 0.14f
        val leftEyeX = cx - radiusX * 0.34f
        val rightEyeX = cx + radiusX * 0.34f
        val eyeY = cy - eyeOffsetY

        // Determine eye drawing style based on state
        val finalExpression = if (calledTimeLeft > 0) "cold" else currentMannerism

        // Group definitions for mapping 80+ expressions cleanly
        val isCurvedUp = finalExpression in listOf(
            "happy", "joyful", "smiling", "cheerful", "excited", "enthusiastic", "energetic", 
            "playful", "proud", "confident", "brave", "relaxed", "calm", "peaceful", "content", 
            "satisfied", "relieved", "hopeful", "optimistic", "inspired", "motivated", "determined", 
            "friendly", "affectionate", "loving", "caring", "compassionate", "grateful", "amused", 
            "laughing", "celebrating", "admiring", "trusting", "respectful", "teeth", "talking", "dinner"
        )
        val isCurvedDown = finalExpression in listOf(
            "sad", "crying", "sobbing", "depressed", "heartbroken", "lonely", "miserable", "hopeless", 
            "disappointed", "regretful", "guilty", "ashamed", "embarrassed", "worried", "nervous", 
            "anxious", "stressed", "panicking", "fearful", "terrified", "grieving", "hurt", "anxiety"
        )
        val isClosedSleepy = finalExpression in listOf(
            "sleeping", "nap", "yawning", "exhausted", "weak", "daydreaming"
        )
        val isAnimeShiny = finalExpression in listOf(
            "cute", "curious", "surprised", "observing", "listening", "waiting", "idle", 
            "thinking", "concentrating", "reading", "working", "sitting", "standing"
        )
        val isSlantedAngry = finalExpression in listOf(
            "angry", "furious", "annoyed", "irritated", "frustrated", "resentful", "jealous", 
            "envious", "bitter", "suspicious", "distrustful"
        )
        val isDizzySpirals = finalExpression in listOf(
            "sick", "nauseous", "puking", "confused", "shocked", "startled", "disgusted"
        )

        // LEFT EYE
        if (isBlinking && !isClosedSleepy) {
            canvas.drawLine(leftEyeX - eyeWidth, eyeY, leftEyeX + eyeWidth, eyeY, paint)
        } else {
            when {
                isClosedSleepy -> {
                    // Curved down peaceful eyes u u
                    path.reset()
                    path.moveTo(leftEyeX - eyeWidth, eyeY - w * 0.02f)
                    path.quadTo(leftEyeX, eyeY + w * 0.04f, leftEyeX + eyeWidth, eyeY - w * 0.02f)
                    canvas.drawPath(path, paint)
                }
                isCurvedDown -> {
                    // Worried curved down/diagonal curves
                    path.reset()
                    path.moveTo(leftEyeX - eyeWidth, eyeY - w * 0.02f)
                    path.quadTo(leftEyeX, eyeY - w * 0.05f, leftEyeX + eyeWidth, eyeY + w * 0.03f)
                    canvas.drawPath(path, paint)
                }
                isCurvedUp -> {
                    // Curved up happy eyes ^ ^
                    path.reset()
                    path.moveTo(leftEyeX - eyeWidth, eyeY + w * 0.03f)
                    path.quadTo(leftEyeX, eyeY - w * 0.06f, leftEyeX + eyeWidth, eyeY + w * 0.03f)
                    canvas.drawPath(path, paint)
                }
                finalExpression == "eating" -> {
                    // Cute closed winking line or straight line
                    canvas.drawLine(leftEyeX - eyeWidth, eyeY, leftEyeX + eyeWidth, eyeY, paint)
                }
                isAnimeShiny -> {
                    // Big shiny anime eye with double sparkles
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(leftEyeX, eyeY, w * 0.085f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(leftEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.032f, paint)
                    canvas.drawCircle(leftEyeX + w * 0.02f, eyeY + w * 0.02f, w * 0.016f, paint)
                    paint.color = Color.parseColor("#130608")
                }
                finalExpression == "bleee" || finalExpression == "walking" -> {
                    // Squeezed closed winking left eye
                    path.reset()
                    path.moveTo(leftEyeX - eyeWidth, eyeY + w * 0.01f)
                    path.lineTo(leftEyeX, eyeY - w * 0.02f)
                    path.lineTo(leftEyeX + eyeWidth, eyeY + w * 0.01f)
                    canvas.drawPath(path, paint)
                }
                isSlantedAngry -> {
                    // Slanted angry eye line \ \
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(leftEyeX - eyeWidth, eyeY - w * 0.02f, leftEyeX + eyeWidth, eyeY + w * 0.03f, paint)
                    // draw angry eyebrow
                    canvas.drawLine(leftEyeX - eyeWidth, eyeY - w * 0.08f, leftEyeX + eyeWidth, eyeY - w * 0.04f, paint)
                }
                isDizzySpirals -> {
                    // Dizzy "X" eye
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = w * 0.03f
                    canvas.drawLine(leftEyeX - eyeWidth * 0.7f, eyeY - eyeWidth * 0.7f, leftEyeX + eyeWidth * 0.7f, eyeY + eyeWidth * 0.7f, paint)
                    canvas.drawLine(leftEyeX + eyeWidth * 0.7f, eyeY - eyeWidth * 0.7f, leftEyeX - eyeWidth * 0.7f, eyeY + eyeWidth * 0.7f, paint)
                    paint.strokeWidth = w * 0.065f
                }
                finalExpression == "cold" -> {
                    // Cute frozen round eyes with white sparkles
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(leftEyeX, eyeY, w * 0.07f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(leftEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.025f, paint)
                    paint.color = Color.parseColor("#130608")
                }
                else -> {
                    // Standard cute open eyes
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(leftEyeX, eyeY, w * 0.075f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(leftEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.028f, paint)
                    paint.color = Color.parseColor("#130608")
                }
            }
        }

        // RIGHT EYE
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#130608")
        paint.strokeWidth = w * 0.065f
        if (isBlinking && !isClosedSleepy) {
            canvas.drawLine(rightEyeX - eyeWidth, eyeY, rightEyeX + eyeWidth, eyeY, paint)
        } else {
            when {
                isClosedSleepy -> {
                    // Curved down u u
                    path.reset()
                    path.moveTo(rightEyeX - eyeWidth, eyeY - w * 0.02f)
                    path.quadTo(rightEyeX, eyeY + w * 0.04f, rightEyeX + eyeWidth, eyeY - w * 0.02f)
                    canvas.drawPath(path, paint)
                }
                isCurvedDown -> {
                    // Worried curve
                    path.reset()
                    path.moveTo(rightEyeX - eyeWidth, eyeY + w * 0.03f)
                    path.quadTo(rightEyeX, eyeY - w * 0.05f, rightEyeX + eyeWidth, eyeY - w * 0.02f)
                    canvas.drawPath(path, paint)
                }
                isCurvedUp -> {
                    // Curved up happy eyes ^ ^
                    path.reset()
                    path.moveTo(rightEyeX - eyeWidth, eyeY + w * 0.03f)
                    path.quadTo(rightEyeX, eyeY - w * 0.06f, rightEyeX + eyeWidth, eyeY + w * 0.03f)
                    canvas.drawPath(path, paint)
                }
                finalExpression == "eating" -> {
                    canvas.drawLine(rightEyeX - eyeWidth, eyeY, rightEyeX + eyeWidth, eyeY, paint)
                }
                isAnimeShiny -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(rightEyeX, eyeY, w * 0.085f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(rightEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.032f, paint)
                    canvas.drawCircle(rightEyeX + w * 0.02f, eyeY + w * 0.02f, w * 0.016f, paint)
                    paint.color = Color.parseColor("#130608")
                }
                finalExpression == "bleee" || finalExpression == "walking" -> {
                    // Playful wide open right eye
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(rightEyeX, eyeY, w * 0.075f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(rightEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.028f, paint)
                    paint.color = Color.parseColor("#130608")
                }
                isSlantedAngry -> {
                    // Slanted angry eye line / /
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(rightEyeX - eyeWidth, eyeY + w * 0.03f, rightEyeX + eyeWidth, eyeY - w * 0.02f, paint)
                    // draw angry eyebrow
                    canvas.drawLine(rightEyeX - eyeWidth, eyeY - w * 0.04f, rightEyeX + eyeWidth, eyeY - w * 0.08f, paint)
                }
                isDizzySpirals -> {
                    // Dizzy "X" eye
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = w * 0.03f
                    canvas.drawLine(rightEyeX - eyeWidth * 0.7f, eyeY - eyeWidth * 0.7f, rightEyeX + eyeWidth * 0.7f, eyeY + eyeWidth * 0.7f, paint)
                    canvas.drawLine(rightEyeX + eyeWidth * 0.7f, eyeY - eyeWidth * 0.7f, rightEyeX - eyeWidth * 0.7f, eyeY + eyeWidth * 0.7f, paint)
                    paint.strokeWidth = w * 0.065f
                }
                finalExpression == "cold" -> {
                    // Frozen eyes
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(rightEyeX, eyeY, w * 0.07f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(rightEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.025f, paint)
                    paint.color = Color.parseColor("#130608")
                }
                else -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(rightEyeX, eyeY, w * 0.075f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(rightEyeX - w * 0.02f, eyeY - w * 0.02f, w * 0.028f, paint)
                    paint.color = Color.parseColor("#130608")
                }
            }
        }

        // --- 4. BEAK (Cute little golden orange beak) ---
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        // If cold, color beak cyan-blue, otherwise cute golden-orange
        if (calledTimeLeft > 0) {
            paint.color = Color.parseColor("#00B8D4")
        } else {
            paint.color = Color.parseColor("#FFA000")
        }
        path.reset()
        val beakW = w * 0.07f
        val beakH = radiusY * 0.12f
        path.moveTo(cx - beakW, cy - w * 0.01f)
        path.lineTo(cx + beakW, cy - w * 0.01f)
        path.lineTo(cx, cy + beakH)
        path.close()
        canvas.drawPath(path, paint)

        // --- 5. CUTE MOUTH ---
        paint.reset()
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#130608")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.03f
        paint.strokeCap = Paint.Cap.ROUND

        val mouthW = w * 0.06f
        val mouthY = cy + radiusY * 0.12f
        val mouthH = radiusY * 0.06f

        path.reset()
        val isWaveyShivering = finalExpression in listOf(
            "anxiety", "cold", "worried", "nervous", "anxious", "stressed", "panicking", 
            "fearful", "terrified", "confused", "suspicious", "distrustful", "disgusted", "sick", "nauseous"
        )
        val isSadCurvedDown = finalExpression in listOf(
            "sad", "crying", "sobbing", "depressed", "heartbroken", "lonely", "miserable", "hopeless", 
            "disappointed", "regretful", "guilty", "ashamed", "embarrassed", "grieving", "hurt", 
            "angry", "furious", "annoyed", "irritated", "frustrated", "resentful", "jealous", "envious", "bitter"
        )
        val isEatingOpen = finalExpression in listOf(
            "eating", "puking", "exhausted", "weak", "surprised", "shocked", "startled"
        )
        val isTeethSmile = finalExpression in listOf(
            "teeth", "excited", "proud", "confident", "brave", "celebrating"
        )
        val isCuteCat3 = finalExpression in listOf(
            "cute", "curious", "cheerful", "playful", "relaxed", "calm", "peaceful", "content", 
            "satisfied", "relieved", "hopeful", "optimistic", "inspired", "motivated", "determined", 
            "friendly", "affectionate", "loving", "caring", "compassionate", "grateful", "amused", 
            "laughing", "admiring", "trusting", "respectful", "thinking", "concentrating", "observing", 
            "listening", "waiting", "daydreaming", "idle", "reading", "working", "sitting", "standing", "dinner"
        )

        when {
            isEatingOpen -> {
                // Open mouth that cycles open/shut or stands open
                val eatOpenFactor = if (finalExpression == "eating") Math.abs(sin(breathTime * 4f)) else 0.85f
                paint.style = Paint.Style.FILL
                val eatHeight = w * 0.08f * eatOpenFactor
                val eatRect = RectF(cx - mouthW, mouthY, cx + mouthW, mouthY + eatHeight)
                canvas.drawOval(eatRect, paint)
            }
            isSadCurvedDown -> {
                // Sad curved down mouth :(
                path.moveTo(cx - mouthW, mouthY + mouthH)
                path.quadTo(cx, mouthY, cx + mouthW, mouthY + mouthH)
                canvas.drawPath(path, paint)
            }
            isWaveyShivering -> {
                // Shivering wavy mouth ~~~
                path.moveTo(cx - mouthW, mouthY)
                path.quadTo(cx - mouthW * 0.5f, mouthY - w * 0.02f, cx - mouthW * 0.25f, mouthY)
                path.quadTo(cx, mouthY + w * 0.02f, cx + mouthW * 0.25f, mouthY)
                path.quadTo(cx + mouthW * 0.5f, mouthY - w * 0.02f, cx + mouthW, mouthY)
                canvas.drawPath(path, paint)
            }
            finalExpression == "sleeping" || finalExpression == "nap" -> {
                // Tiny round 'o' snoring mouth
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.035f
                canvas.drawCircle(cx, mouthY + w * 0.02f, w * 0.025f, paint)
            }
            isTeethSmile -> {
                // Wide open cute smile showing shiny white vampire teeth!
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#130608")
                val smileRect = RectF(cx - mouthW * 1.3f, mouthY - w * 0.01f, cx + mouthW * 1.3f, mouthY + w * 0.09f)
                canvas.drawOval(smileRect, paint)
                
                paint.color = Color.WHITE
                // Left tooth triangle
                path.reset()
                path.moveTo(cx - mouthW * 0.6f, mouthY)
                path.lineTo(cx - mouthW * 0.3f, mouthY)
                path.lineTo(cx - mouthW * 0.45f, mouthY + w * 0.04f)
                path.close()
                canvas.drawPath(path, paint)
                
                // Right tooth triangle
                path.reset()
                path.moveTo(cx + mouthW * 0.3f, mouthY)
                path.lineTo(cx + mouthW * 0.6f, mouthY)
                path.lineTo(cx + mouthW * 0.45f, mouthY + w * 0.04f)
                path.close()
                canvas.drawPath(path, paint)
            }
            isCuteCat3 -> {
                // Tiny delicate cute cat smile "3"
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.025f
                paint.color = Color.parseColor("#130608")
                val smallMouthW = mouthW * 0.7f
                path.moveTo(cx - smallMouthW, mouthY)
                path.quadTo(cx - smallMouthW / 2f, mouthY + mouthH * 0.8f, cx, mouthY)
                path.quadTo(cx + smallMouthW / 2f, mouthY + mouthH * 0.8f, cx + smallMouthW, mouthY)
                canvas.drawPath(path, paint)
            }
            finalExpression == "talking" || finalExpression == "walking" -> {
                // Animated speaking mouth opening and closing
                val talkOpenFactor = Math.abs(sin(breathTime * 5f))
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#130608")
                val talkHeight = w * 0.07f * talkOpenFactor
                val talkRect = RectF(cx - mouthW, mouthY, cx + mouthW, mouthY + talkHeight)
                canvas.drawOval(talkRect, paint)
            }
            finalExpression == "yawning" -> {
                // Large round yawning mouth
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#130608")
                val yawnSize = w * 0.055f + Math.abs(sin(breathTime * 2f)) * w * 0.03f
                canvas.drawCircle(cx, mouthY + w * 0.03f, yawnSize, paint)
                
                // Red tongue inside yawn
                paint.color = Color.parseColor("#FF5252")
                canvas.drawCircle(cx, mouthY + w * 0.03f + yawnSize * 0.4f, yawnSize * 0.4f, paint)
            }
            finalExpression == "bleee" -> {
                // Playful smile with pink tongue sticking out
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#130608")
                val baseRect = RectF(cx - mouthW * 1.1f, mouthY, cx + mouthW * 1.1f, mouthY + w * 0.05f)
                canvas.drawOval(baseRect, paint)
                
                // Stick out a cute pink tongue
                paint.color = Color.parseColor("#FF4081")
                val tongueRect = RectF(cx + w * 0.01f, mouthY + w * 0.01f, cx + mouthW * 1.3f, mouthY + w * 0.10f)
                canvas.drawRoundRect(tongueRect, w * 0.03f, w * 0.03f, paint)
                
                paint.color = Color.parseColor("#D81B60")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.01f
                canvas.drawLine(cx + mouthW * 0.7f, mouthY + w * 0.02f, cx + mouthW * 0.7f, mouthY + w * 0.08f, paint)
            }
            else -> {
                // Standard cute cat smile "w"
                path.moveTo(cx - mouthW, mouthY)
                path.quadTo(cx - mouthW / 2f, mouthY + mouthH, cx, mouthY)
                path.quadTo(cx + mouthW / 2f, mouthY + mouthH, cx + mouthW, mouthY)
                canvas.drawPath(path, paint)
            }
        }

        // --- 6. EXTRA DECORATIVE EFFECTS FOR MANNERISMS ---
        paint.reset()
        paint.isAntiAlias = true

        // A. Sleeping / Nap Zzz letters
        val showZzz = finalExpression in listOf("sleeping", "nap", "daydreaming", "exhausted", "weak", "waiting")
        if (showZzz) {
            paint.color = Color.parseColor("#FFFDF0")
            paint.style = Paint.Style.FILL
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            for (i in 0..2) {
                val offsetTime = (mannerismTime * 1.5f + i * 50f) % 150f
                val ratio = offsetTime / 150f
                paint.alpha = ((1f - ratio) * 200).toInt().coerceIn(0, 255)
                paint.textSize = (w * 0.07f + ratio * w * 0.06f)
                val zx = cx + radiusX * 0.5f + ratio * w * 0.3f + sin(breathTime + i) * (w * 0.05f)
                val zy = cy - radiusY * 0.5f - ratio * h * 0.4f
                canvas.drawText("Z", zx, zy, paint)
            }
            paint.alpha = 255
        }

        // B. Crying Teardrops (Sad/Crying/Sobbing/Heartbroken states)
        val showTears = finalExpression in listOf("sad", "crying", "sobbing", "depressed", "heartbroken", "lonely", "miserable", "hopeless", "disappointed", "regretful", "guilty", "ashamed", "grieving", "hurt")
        if (showTears) {
            paint.color = Color.parseColor("#4FC3F7") // Light blue
            paint.style = Paint.Style.FILL
            val isSobbing = finalExpression == "sobbing" || finalExpression == "crying"
            for (i in 0..1) {
                val sideSign = if (i == 0) -1f else 1f
                val eyeX = cx + sideSign * radiusX * 0.34f
                val speedMultiplier = if (isSobbing) 3f else 1.8f
                val dropOffset = (mannerismTime * speedMultiplier) % (h * 0.35f)
                val dropY = eyeY + dropOffset
                if (dropY < cy + radiusY) {
                    val tearSize = if (isSobbing) w * 0.04f else w * 0.025f
                    canvas.drawCircle(eyeX, dropY, tearSize, paint)
                    // draw tail
                    path.reset()
                    path.moveTo(eyeX - tearSize * 0.8f, dropY)
                    path.lineTo(eyeX, dropY - tearSize * 1.5f)
                    path.lineTo(eyeX + tearSize * 0.8f, dropY)
                    path.close()
                    canvas.drawPath(path, paint)
                }
            }
        }

        // C. Anxiety Sweat Drop / Nervous Tension
        val showAnxietySweat = finalExpression in listOf("anxiety", "worried", "nervous", "anxious", "stressed", "panicking", "fearful", "terrified", "shocked", "startled", "confused", "suspicious", "distrustful")
        if (showAnxietySweat) {
            paint.color = Color.parseColor("#80DEEA")
            paint.style = Paint.Style.FILL
            val sweatX = cx + radiusX * 0.55f
            val sweatY = cy - radiusY * 0.3f + (mannerismTime * 1.5f) % (h * 0.25f)
            canvas.drawCircle(sweatX, sweatY, w * 0.022f, paint)
            path.reset()
            path.moveTo(sweatX - w * 0.016f, sweatY)
            path.lineTo(sweatX, sweatY - w * 0.035f)
            path.lineTo(sweatX + w * 0.016f, sweatY)
            path.close()
            canvas.drawPath(path, paint)
        }

        // D. Joyful Sparkle Stars (All hyper-positive excited mannerisms)
        val showStars = finalExpression in listOf("joyful", "cheerful", "excited", "enthusiastic", "energetic", "playful", "proud", "confident", "brave", "relieved", "hopeful", "optimistic", "inspired", "motivated", "determined", "celebrating", "amused", "laughing")
        if (showStars) {
            paint.color = Color.parseColor("#FFD54F") // Gold yellow star
            paint.style = Paint.Style.FILL
            for (i in 0..2) {
                val angle = (mannerismTime * 3f + i * 120f) % 360f
                val starRad = Math.toRadians(angle.toDouble())
                val starDist = w * 0.45f + sin(breathTime * 3f) * 10f
                val sx = cx + starDist * cos(starRad).toFloat()
                val sy = cy + starDist * sin(starRad).toFloat()
                val sz = w * 0.045f
                
                path.reset()
                path.moveTo(sx, sy - sz)
                path.lineTo(sx + sz / 3, sy - sz / 3)
                path.lineTo(sx + sz, sy)
                path.lineTo(sx + sz / 3, sy + sz / 3)
                path.lineTo(sx, sy + sz)
                path.lineTo(sx - sz / 3, sy + sz / 3)
                path.lineTo(sx - sz, sy)
                path.lineTo(sx - sz / 3, sy - sz / 3)
                path.close()
                canvas.drawPath(path, paint)
            }
        }

        // E. Rain drops falling (Depressed or literal rain state)
        val showRain = finalExpression in listOf("rain", "depressed", "miserable", "lonely", "grieving", "heartbroken")
        if (showRain) {
            paint.color = Color.parseColor("#81D4FA")
            paint.strokeWidth = w * 0.015f
            paint.strokeCap = Paint.Cap.ROUND
            paint.style = Paint.Style.STROKE
            for (i in 0..4) {
                val rx = (cx - w * 0.5f + (i * w * 0.25f) + mannerismTime) % w
                val ry = (mannerismTime * 4f + i * h * 0.3f) % h
                canvas.drawLine(rx, ry, rx - w * 0.03f, ry + h * 0.08f, paint)
            }
        }

        // F. Eating Food Crumbs falling (Or puking green drops!)
        val showParticles = finalExpression in listOf("eating", "puking")
        if (showParticles) {
            val isPuking = finalExpression == "puking"
            paint.color = Color.parseColor(if (isPuking) "#81C784" else "#8D6E63") // Green puking vs brown breadcrumbs
            paint.style = Paint.Style.FILL
            for (i in 0..2) {
                val crumbTime = (mannerismTime * 2.5f + i * 40f) % 120f
                val ratio = crumbTime / 120f
                val cxOffset = sin(i * 1.5f) * w * 0.08f
                val cyOffset = mouthY + h * 0.05f + ratio * h * 0.18f
                paint.alpha = ((1f - ratio) * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(cx + cxOffset, cyOffset, if (isPuking) w * 0.02f else w * 0.015f, paint)
            }
            paint.alpha = 255
        }

        // G. Saying "Hi!" Speech Bubble (Running wave end)
        if (currentMannerism == "running" && mannerismTime >= 170f) {
            // Draw a cute miniature speech bubble with text "Hi! 🌸"
            val bubbleLeft = cx + radiusX * 0.3f
            val bubbleTop = cy - radiusY * 1.1f
            val bubbleRight = bubbleLeft + w * 0.35f
            val bubbleBottom = bubbleTop + h * 0.25f
            val bubbleRect = RectF(bubbleLeft, bubbleTop, bubbleRight, bubbleBottom)

            // Bubble body
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(bubbleRect, 12f, 12f, paint)

            // Bubble pointer arrow
            path.reset()
            path.moveTo(bubbleLeft + w * 0.08f, bubbleBottom)
            path.lineTo(bubbleLeft, bubbleBottom + h * 0.06f)
            path.lineTo(bubbleLeft + w * 0.15f, bubbleBottom)
            path.close()
            canvas.drawPath(path, paint)

            // Text "Hi! 🌸"
            paint.color = Color.parseColor("#9E1B32")
            paint.textSize = w * 0.09f
            paint.style = Paint.Style.FILL
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            canvas.drawText("Hi! 🌸", bubbleLeft + w * 0.05f, bubbleTop + h * 0.17f, paint)
        }

        // H. Talking with her friend Pip
        if (finalExpression == "talking") {
            val fx = cx - radiusX * 1.2f
            val fy = cy + radiusY * 0.3f
            val fr = w * 0.14f
            
            // Draw Pip friend body
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFE082")
            canvas.drawCircle(fx, fy, fr, paint)
            
            // Draw cheeks
            paint.color = Color.parseColor("#FFCDD2")
            canvas.drawCircle(fx - fr * 0.4f, fy + fr * 0.1f, fr * 0.2f, paint)
            canvas.drawCircle(fx + fr * 0.4f, fy + fr * 0.1f, fr * 0.2f, paint)
            
            // Draw eyes
            paint.color = Color.parseColor("#212121")
            canvas.drawCircle(fx - fr * 0.3f, fy - fr * 0.1f, fr * 0.1f, paint)
            canvas.drawCircle(fx + fr * 0.3f, fy - fr * 0.1f, fr * 0.1f, paint)
            
            // Draw orange beak
            paint.color = Color.parseColor("#FF8F00")
            path.reset()
            path.moveTo(fx - fr * 0.15f, fy)
            path.lineTo(fx + fr * 0.15f, fy)
            path.lineTo(fx, fy + fr * 0.25f)
            path.close()
            canvas.drawPath(path, paint)
            
            // Speech bubble
            val bubbleLeft = cx - radiusX * 0.7f
            val bubbleTop = cy - radiusY * 1.2f
            val bubbleRight = bubbleLeft + w * 0.58f
            val bubbleBottom = bubbleTop + h * 0.22f
            val bubbleRect = RectF(bubbleLeft, bubbleTop, bubbleRight, bubbleBottom)
            
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(bubbleRect, 14f, 14f, paint)
            
            path.reset()
            path.moveTo(bubbleLeft + w * 0.2f, bubbleBottom)
            path.lineTo(bubbleLeft + w * 0.12f, bubbleBottom + h * 0.05f)
            path.lineTo(bubbleLeft + w * 0.28f, bubbleBottom)
            path.close()
            canvas.drawPath(path, paint)
            
            val convoText = when (((mannerismTime / 40).toInt()) % 3) {
                0 -> "Hi friend! 🐣"
                1 -> "Let's study!"
                else -> "Pip Pip!~ 🌸"
            }
            
            paint.color = Color.parseColor("#EA580C")
            paint.textSize = w * 0.072f
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            canvas.drawText(convoText, bubbleLeft + w * 0.05f, bubbleTop + h * 0.14f, paint)
        }

        // I. Dinner Show
        if (finalExpression == "dinner") {
            val px = cx
            val py = cy + radiusY * 0.85f
            val pw = w * 0.28f
            val ph = h * 0.08f
            val plateRect = RectF(px - pw, py - ph, px + pw, py + ph)
            
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#B0BEC5")
            canvas.drawOval(plateRect, paint)
            
            paint.color = Color.WHITE
            val plateInnerRect = RectF(px - pw * 0.85f, py - ph * 0.75f, px + pw * 0.85f, py + ph * 0.75f)
            canvas.drawOval(plateInnerRect, paint)
            
            // pie or pudding
            paint.color = Color.parseColor("#FF7043")
            val foodRect = RectF(px - pw * 0.45f, py - ph * 0.9f, px + pw * 0.45f, py + ph * 0.3f)
            canvas.drawRoundRect(foodRect, 8f, 8f, paint)
            
            // cherry top
            paint.color = Color.parseColor("#E91E63")
            canvas.drawCircle(px, py - ph * 0.9f, w * 0.03f, paint)
            
            // rising steam
            paint.color = Color.WHITE
            paint.alpha = (120 + 80 * sin(breathTime * 3f)).toInt().coerceIn(0, 255)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = w * 0.015f
            paint.strokeCap = Paint.Cap.ROUND
            
            val steamOffset = (mannerismTime * 1.5f) % (h * 0.15f)
            for (i in -1..1) {
                val sx = px + i * (w * 0.08f) + sin(breathTime + i) * 6f
                val syStart = py - ph * 1.1f - steamOffset
                if (syStart > cy - radiusY) {
                    canvas.drawLine(sx, syStart, sx + w * 0.02f, syStart - h * 0.04f, paint)
                }
            }
            paint.alpha = 255
            
            // bubble label
            val bLeft = cx - w * 0.22f
            val bTop = cy - radiusY * 1.1f
            val bRect = RectF(bLeft, bTop, bLeft + w * 0.48f, bTop + h * 0.14f)
            
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#E2F1FM")
            canvas.drawRoundRect(bRect, 8f, 8f, paint)
            
            paint.color = Color.parseColor("#0D47A1")
            paint.textSize = w * 0.052f
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            canvas.drawText("Dinner! 🍲🌸", bLeft + w * 0.03f, bTop + h * 0.09f, paint)
        }

        // J. Cute Hearts and Stars (For loving, friendly, affectionate mannerisms)
        val showHearts = finalExpression in listOf("cute", "friendly", "affectionate", "loving", "caring", "compassionate", "grateful", "admiring", "trusting", "respectful", "smiling")
        if (showHearts) {
            paint.color = Color.parseColor("#EC407A")
            paint.style = Paint.Style.FILL
            
            for (i in 0..2) {
                val tOffset = (mannerismTime + i * 45f) % 135f
                val ratio = tOffset / 135f
                paint.alpha = ((1f - ratio) * 240).toInt().coerceIn(0, 255)
                
                val hSize = w * (0.03f + 0.02f * (1f - ratio))
                val hAngle = i * 120f + sin(breathTime) * 15f
                val hRad = Math.toRadians(hAngle.toDouble())
                val hDist = radiusX * 1.1f + ratio * w * 0.3f
                val hx = cx + hDist * cos(hRad).toFloat()
                val hy = cy - radiusY * 0.2f + hDist * sin(hRad).toFloat() - ratio * h * 0.2f
                
                path.reset()
                path.moveTo(hx, hy)
                path.cubicTo(hx - hSize, hy - hSize, hx - hSize * 1.5f, hy + hSize * 0.5f, hx, hy + hSize * 1.8f)
                path.cubicTo(hx + hSize * 1.5f, hy + hSize * 0.5f, hx + hSize, hy - hSize, hx, hy)
                path.close()
                canvas.drawPath(path, paint)
            }
            paint.alpha = 255
        }

        // K. Teeth sparkly glints
        if (finalExpression == "teeth") {
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            
            val glintOffset = Math.abs(sin(breathTime * 2f))
            fun drawGlint(gx: Float, gy: Float, gs: Float) {
                path.reset()
                path.moveTo(gx, gy - gs)
                path.lineTo(gx + gs / 3f, gy - gs / 3f)
                path.lineTo(gx + gs, gy)
                path.lineTo(gx + gs / 3f, gy + gs / 3f)
                path.lineTo(gx, gy + gs)
                path.lineTo(gx - gs / 3f, gy + gs / 3f)
                path.lineTo(gx - gs, gy)
                path.lineTo(gx - gs / 3f, gy - gs / 3f)
                path.close()
                canvas.drawPath(path, paint)
            }
            
            drawGlint(cx - mouthW * 1.3f, mouthY + w * 0.04f, w * (0.04f + 0.02f * glintOffset))
            drawGlint(cx + mouthW * 1.3f, mouthY + w * 0.04f, w * (0.04f + 0.02f * glintOffset))
            drawGlint(cx - radiusX * 0.8f, cy - radiusY * 0.8f, w * (0.05f + 0.01f * glintOffset))
            drawGlint(cx + radiusX * 0.8f, cy - radiusY * 0.8f, w * (0.05f + 0.01f * glintOffset))
        }

        // L. Yawning
        if (finalExpression == "yawning") {
            paint.color = Color.parseColor("#FFE082")
            paint.textSize = w * 0.055f
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            paint.style = Paint.Style.FILL
            
            val yawnText = "Yaaaawn... 🥱"
            val tx = cx - w * 0.18f
            val ty = cy - radiusY * 0.95f - Math.abs(sin(breathTime)) * 10f
            canvas.drawText(yawnText, tx, ty, paint)
            
            paint.color = Color.parseColor("#B3E5FC")
            paint.alpha = 130
            val bubbleRad = w * 0.04f + Math.abs(sin(breathTime * 1.5f)) * w * 0.04f
            canvas.drawCircle(cx + w * 0.1f, mouthY - w * 0.04f, bubbleRad, paint)
            paint.alpha = 255
        }

        // M. Angry Temple Vein/Cross (For angry, furious, annoyed, irritated, frustrated, resentful, jealous, envious, bitter)
        val showAngryVein = finalExpression in listOf("angry", "furious", "annoyed", "irritated", "frustrated", "resentful", "jealous", "envious", "bitter")
        if (showAngryVein) {
            paint.color = Color.parseColor("#E53935") // Bright red anger mark
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = w * 0.018f
            paint.strokeCap = Paint.Cap.ROUND
            
            val ax = cx - radiusX * 0.7f
            val ay = cy - radiusY * 0.6f + sin(breathTime * 4f) * 4f
            val alen = w * 0.04f
            
            // Draw standard cute anime anger mark (three curved branches or a cross)
            canvas.drawLine(ax - alen, ay, ax + alen, ay, paint)
            canvas.drawLine(ax, ay - alen, ax, ay + alen, paint)
            canvas.drawLine(ax - alen * 0.7f, ay - alen * 0.7f, ax + alen * 0.7f, ay + alen * 0.7f, paint)
        }

        // N. Lightbulb of Idea (For thinking, concentrating, inspired, curious, surprised, reading, working)
        val showIdeaLightbulb = finalExpression in listOf("thinking", "concentrating", "inspired", "curious", "surprised", "reading", "working")
        if (showIdeaLightbulb) {
            val bulbX = cx
            val bulbY = cy - radiusY * 1.25f + sin(breathTime * 1.8f) * 6f
            
            // Draw yellow glow backing
            paint.color = Color.parseColor("#FFF59D")
            paint.style = Paint.Style.FILL
            paint.alpha = (100 + 100 * Math.abs(sin(breathTime * 2f))).toInt()
            canvas.drawCircle(bulbX, bulbY, w * 0.07f, paint)
            
            // Draw yellow glass bulb
            paint.alpha = 255
            paint.color = Color.parseColor("#FFD54F")
            canvas.drawCircle(bulbX, bulbY - w * 0.01f, w * 0.04f, paint)
            
            // Draw bulb metal cap
            paint.color = Color.parseColor("#90A4AE")
            canvas.drawRect(bulbX - w * 0.02f, bulbY + w * 0.03f, bulbX + w * 0.02f, bulbY + w * 0.05f, paint)
            
            // Little sparkle lines around lightbulb
            paint.color = Color.parseColor("#FFD54F")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = w * 0.012f
            for (angle in listOf(0, 45, 135, 180, 225, 315)) {
                val rad = Math.toRadians(angle.toDouble())
                val x1 = bulbX + (w * 0.06f) * cos(rad).toFloat()
                val y1 = bulbY + (w * 0.06f) * sin(rad).toFloat()
                val x2 = bulbX + (w * 0.10f) * cos(rad).toFloat()
                val y2 = bulbY + (w * 0.10f) * sin(rad).toFloat()
                canvas.drawLine(x1, y1, x2, y2, paint)
            }
        }

        // O. Mini Book or Laptop (For reading / working states)
        if (finalExpression == "reading") {
            // Draw a cute pink/purple miniature open book below Mochi
            val bx = cx
            val by = cy + radiusY * 0.82f
            val bw = w * 0.22f
            val bh = h * 0.07f
            
            paint.style = Paint.Style.FILL
            // Book cover backing
            paint.color = Color.parseColor("#BA68C8")
            canvas.drawRoundRect(RectF(bx - bw - 4f, by - 4f, bx + bw + 4f, by + bh + 4f), 6f, 6f, paint)
            
            // Book pages
            paint.color = Color.parseColor("#F5F5F5")
            canvas.drawRoundRect(RectF(bx - bw, by, bx, by + bh), 4f, 4f, paint)
            canvas.drawRoundRect(RectF(bx, by, bx + bw, by + bh), 4f, 4f, paint)
            
            // Mini lines of text inside the book
            paint.color = Color.parseColor("#90A4AE")
            paint.strokeWidth = 3f
            for (i in 0..2) {
                val ly = by + bh * 0.25f + i * (bh * 0.22f)
                canvas.drawLine(bx - bw * 0.8f, ly, bx - bw * 0.15f, ly, paint)
                canvas.drawLine(bx + bw * 0.15f, ly, bx + bw * 0.8f, ly, paint)
            }
        } else if (finalExpression == "working") {
            // Draw a tiny cute retro blue/teal personal laptop below her
            val lx = cx
            val ly = cy + radiusY * 0.82f
            val lw = w * 0.25f
            val lh = h * 0.07f
            
            paint.style = Paint.Style.FILL
            // Laptop keyboard base
            paint.color = Color.parseColor("#4DD0E1")
            canvas.drawRoundRect(RectF(lx - lw, ly + lh * 0.3f, lx + lw, ly + lh * 1.1f), 8f, 8f, paint)
            
            // Laptop glowing screen angled up
            paint.color = Color.parseColor("#00ACC1")
            canvas.drawRoundRect(RectF(lx - lw * 0.9f, ly - lh * 0.7f, lx + lw * 0.9f, ly + lh * 0.3f), 8f, 8f, paint)
            
            // Glowing white glass
            paint.color = Color.parseColor("#E0F7FA")
            canvas.drawRoundRect(RectF(lx - lw * 0.82f, ly - lh * 0.58f, lx + lw * 0.82f, ly + lh * 0.22f), 6f, 6f, paint)
            
            // Tiny code lines on the laptop screen
            paint.color = Color.parseColor("#26A69A")
            paint.strokeWidth = 4f
            canvas.drawLine(lx - lw * 0.6f, ly - lh * 0.2f, lx - lw * 0.1f, ly - lh * 0.2f, paint)
            canvas.drawLine(lx - lw * 0.5f, ly, lx + lw * 0.4f, ly, paint)
            canvas.drawLine(lx - lw * 0.6f, ly + lh * 0.1f, lx + lw * 0.2f, ly + lh * 0.1f, paint)
        }

        // --- 7. STREAK FIRE ICON (UNLIT VS LIT) ---
        val touchPrefs = context.getSharedPreferences("akademic_assistive_touch_prefs", android.content.Context.MODE_PRIVATE)
        val fsStreak = touchPrefs.getInt("journal_streak", 0)
        val isFireLit = fsStreak > 0

        // Position the fire badge in the upper right corner of Mochi
        val fx = cx + radiusX * 0.65f
        val fy = cy - radiusY * 0.65f
        
        // Flicker / heartbeat animation for the fire size if lit
        val sizeFlicker = if (isFireLit) {
            1.0f + 0.12f * sin(mannerismTime * 0.25f)
        } else {
            1.0f
        }
        val baseFs = w * 0.15f
        val fs = baseFs * sizeFlicker

        canvas.save()
        // If lit, tilt it slightly back and forth
        if (isFireLit) {
            canvas.rotate(3f * sin(mannerismTime * 0.15f), fx, fy)
        }

        // Draw a soft glow behind the lit fire
        if (isFireLit) {
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFCC80")
            paint.alpha = (80 + 40 * sin(mannerismTime * 0.2f)).toInt().coerceIn(0, 255)
            canvas.drawCircle(fx, fy + fs * 0.1f, fs * 0.9f, paint)
        } else {
            // Draw a subtle dark outline backdrop for the unlit fire to make it stand out
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#37474F")
            paint.alpha = 40
            canvas.drawCircle(fx, fy + fs * 0.1f, fs * 0.7f, paint)
        }

        // Draw Outer Flame Path
        val outerPath = Path().apply {
            moveTo(fx, fy - fs * 0.8f) // tip
            // right belly
            quadTo(fx + fs * 0.55f, fy - fs * 0.2f, fx + fs * 0.45f, fy + fs * 0.25f)
            // bottom
            quadTo(fx, fy + fs * 0.6f, fx - fs * 0.45f, fy + fs * 0.25f)
            // left belly
            quadTo(fx - fs * 0.55f, fy - fs * 0.2f, fx, fy - fs * 0.8f)
        }

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        
        if (isFireLit) {
            // Lit outer flame: beautiful orange-red gradient
            val fireGrad = LinearGradient(
                fx, fy - fs * 0.8f, fx, fy + fs * 0.6f,
                Color.parseColor("#FF3D00"), // Red-Orange
                Color.parseColor("#FF9100"), // Bright Orange
                Shader.TileMode.CLAMP
            )
            paint.shader = fireGrad
        } else {
            // Unlit outer flame: flat cold blue-gray / ash color
            paint.color = Color.parseColor("#78909C")
        }
        canvas.drawPath(outerPath, paint)
        paint.shader = null

        // Draw Inner Flame Path
        val innerFs = fs * 0.55f
        val ify = fy + fs * 0.12f
        val innerPath = Path().apply {
            moveTo(fx, ify - innerFs * 0.8f)
            quadTo(fx + innerFs * 0.55f, ify - innerFs * 0.2f, fx + innerFs * 0.45f, ify + innerFs * 0.25f)
            quadTo(fx, ify + innerFs * 0.6f, fx - innerFs * 0.45f, ify + innerFs * 0.25f)
            quadTo(fx - innerFs * 0.55f, ify - innerFs * 0.2f, fx, ify - innerFs * 0.8f)
        }

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        if (isFireLit) {
            // Lit inner flame: bright glowing yellow
            paint.color = Color.parseColor("#FFEA00")
        } else {
            // Unlit inner flame: dark stone charcoal gray
            paint.color = Color.parseColor("#455A64")
        }
        canvas.drawPath(innerPath, paint)

        // Draw a tiny streak text badge on the bottom-right of the flame if lit
        if (isFireLit && fsStreak >= 1) {
            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#D32F2F") // Red circle backing
            
            val badgeX = fx + fs * 0.45f
            val badgeY = fy + fs * 0.45f
            val badgeR = fs * 0.38f
            canvas.drawCircle(badgeX, badgeY, badgeR, paint)
            
            // Draw streak number
            paint.color = Color.WHITE
            paint.textSize = badgeR * 1.3f
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.CENTER
            val textY = badgeY - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(fsStreak.toString(), badgeX, textY, paint)
        }

        canvas.restore()

        canvas.restore()
    }
}
