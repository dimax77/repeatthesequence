//SoundUtil.kt
package com.example.rts.data

import androidx.compose.ui.graphics.Color
import com.example.rts.R

object GameUtils {
    fun getSoundResource(soundId: Int): Int {
        return when (soundId) {
            0 -> R.raw.katana
            1 -> R.raw.machine_gun
            2 -> R.raw.reload
            3 -> R.raw.shot
            else -> throw IllegalArgumentException("Invalid sound ID")
        }
    }

    fun getTitle(soundId: Int): String {
        return when (soundId) {
            0 -> "Katana"
            1 -> "Machine Gun"
            2 -> "Reload"
            3 -> "Shot"
            else -> throw IllegalArgumentException("Invalid sound ID")
        }
    }

    fun getResColor(colorId: String): Color {
        val colorResId = when (colorId) {
            "buttonColor1" -> R.color.buttonColor1
            "buttonColor2" -> R.color.buttonColor2
            "buttonColor3" -> R.color.buttonColor3
            "buttonColor4" -> R.color.buttonColor4
            else -> throw IllegalArgumentException("Invalid color ID")
        }
        return Color(colorResId)
    }
}