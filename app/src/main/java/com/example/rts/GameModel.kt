// GameModel.kt
package com.example.rts

class GameModel {
    var currentLevel = 1
    val topLevel = 1
    var userPlaying = false
    val gameOver = false
    val userInput = mutableListOf<Int>()
    var randomSequence = mutableListOf<Int>()
    val buttons = List<ButtonState>(4) { it ->
        ButtonState(
            id = it,
            soundResId = SoundUtil.getSoundResource(it),
            title = SoundUtil.getTitle(it)
        )
    }
}