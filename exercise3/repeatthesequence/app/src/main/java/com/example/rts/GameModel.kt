// GameModel.kt
package com.example.rts

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

class GameModel(private val context: Context, topLevelValue: Int) {
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

    @SuppressLint("DiscouragedApi")
    private fun getResColor(colorId: String): Color {
        val colorResId = context.resources.getIdentifier(colorId, "color", context.packageName)
        return Color(ContextCompat.getColor(context, colorResId))
    }
}