// GameModel.kt
package com.example.rts

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

class GameModel(private val context: Context) {
    var currentLevel = mutableIntStateOf(1)
    var topLevel = mutableIntStateOf(1)
    var userPlaying = false
    val gameOver = false
    var userInput = mutableListOf<Int>()
    var randomSequence = mutableListOf<Int>()
    val buttons = List<ButtonState>(4) { it ->
        ButtonState(
            id = it,
            soundResId = SoundUtil.getSoundResource(it),
            title = SoundUtil.getTitle(it),
            color = getResColor("buttonColor${it + 1}")
        )
    }

    private fun getResColor(colorId: String): Color {
        val colorResId = context.resources.getIdentifier(colorId, "color", context.packageName)
        return Color(ContextCompat.getColor(context, colorResId))
    }
}