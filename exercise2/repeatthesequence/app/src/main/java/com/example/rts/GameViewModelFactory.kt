//GameViewModelFactory.kt
package com.example.rts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameViewModelFactory(private val context: Context, private val soundPlayer: SoundPlayer) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java))
            return GameViewModel(context, soundPlayer) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}