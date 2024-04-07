// GameModel.kt
package com.example.rts

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.example.rts.utils.SoundUtil

class GameModel(topLevelValue: Int) {
    private var _waitForUserInput = mutableStateOf(false)
    fun setWaitForUserInput(value: Boolean) {
        _waitForUserInput.value = value
        Log.d("waitForUserInput", "was set to $value")
    }

    val waitForUserInput: Boolean
        get() {
            return _waitForUserInput.value
        }
    var currentLevel = mutableIntStateOf(1)
    var topLevel = mutableIntStateOf(topLevelValue)
    var userPlaying = mutableStateOf(false)
    var gameOver = mutableStateOf(false)
    var userInput = mutableListOf<Int>()
    val buttons = List(4) {
        ButtonState(
            title = SoundUtil.getTitle(it),
            color = getResColor("buttonColor${it + 1}")
        )
    }

    private fun getResColor(colorId: String): Color {
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