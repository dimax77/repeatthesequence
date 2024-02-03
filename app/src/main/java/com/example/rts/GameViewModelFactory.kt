//GameViewModelFactory.kt
package com.example.rts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameViewModelFactory(private val soundPlayer: SoundPlayer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java))
            return GameViewModel(soundPlayer) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}