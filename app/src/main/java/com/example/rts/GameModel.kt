// GameModel.kt
package com.example.rts

import androidx.compose.runtime.mutableIntStateOf

class GameModel {
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
            title = SoundUtil.getTitle(it)
        )
    }
}