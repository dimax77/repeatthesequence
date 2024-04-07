//GameViewModelFactory.kt
package com.example.rts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rts.ui.GameViewModel

class GameViewModelFactory(
    private val soundPlayer: SoundPlayer,
    private val topLevelValue: Int
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java))
            return GameViewModel(soundPlayer, topLevelValue) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}