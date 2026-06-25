package com.example.notification

import android.content.Context
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object AkiQuotes {
    val quotes = listOf(
        "Mochi needs your study warmth! Please don't let me get cold... 🥺❤️",
        "Brrr... Mochi is shivering! Write a study note to warm me up! ❄️🔥",
        "Is that a draft? Mochi's little feathers are cold... Write a journal note! 💨🌸",
        "Keep the study fire burning! Write in your journal so Mochi can cuddle! 🥶✨",
        "Mochi is shivering... Only your brilliant focus can warm my heart! 📝🔥",
        "Hey study buddy, hug me! Let's write a note and warm up! 🕯️❤️",
        "Don't let my little flame go out! Log your progress for Mochi! 🪵🔥",
        "Mochi's wings are turning to ice! Fast, write down your achievements! ❄️🪶",
        "Warm me up with your study secrets! Mochi is waiting for you! 🥰🔥",
        "Mochi's embers need some fuel. Pen down your study thoughts, please! 📓✨",
        "My golden flames miss your focus! Log a study journal entry! 🔥💡",
        "Brrr... A cold breeze just blew in. Let's study and cozy up together! 🌬️🔥",
        "Mochi is getting sleepy in the cold... Wake me up with a sweet note! 😴🔥",
        "Keep our study spark alive! Write a quick summary for Mochi! ⚡❤️",
        "Mochi is hugging a tiny ember... Send some cozy study warmth! 🤗🔥",
        "Your study habits keep my heart burning bright! *happy squeak* ❤️📖",
        "The night is cold, but your study streak is so warm! Mochi is proud! 🌌🔥",
        "Mochi's glowing heart depends on your focus! Don't let me cool down! 💓🔥",
        "Keep the flame dancing! Jot down your study journal entries! 💃🔥",
        "Mochi's golden flame needs you! Don't let me turn to ash! 🍂💛",
        "Brrr! Light me up, study hero! Mochi is cheering for you! 🥶🗡️",
        "Pour some academic oil into my lamp! Write in your study journal! 🪔✨",
        "Your study logs are like warm blankets for Mochi! Wrap me up! 🧣🔥",
        "Mochi's flames are running low... Power me up with a study note! 🔋🔥",
        "Studying is the best heater! Let's write a note and warm up! 🌡️❤️",
        "Fuel my fire with your focus! Write a new note before it gets cold! 🔥📘"
    )

    fun getCurrentQuote(context: Context): String {
        val prefs = context.getSharedPreferences("akademic_timezone_prefs", Context.MODE_PRIVATE)
        val zoneIdStr = prefs.getString("selected_zone", "Asia/Manila") ?: "Asia/Manila"
        val zoneId = try { ZoneId.of(zoneIdStr) } catch (e: Exception) { ZoneId.systemDefault() }
        
        val zonedDateTime = ZonedDateTime.now(zoneId)
        
        // Compute 12-hour block count since epoch
        val epochSeconds = zonedDateTime.toInstant().epochSecond
        val hour12BlockIndex = epochSeconds / (12 * 3600)
        
        val index = (hour12BlockIndex % quotes.size).toInt()
        return quotes[Math.abs(index)]
    }
}
