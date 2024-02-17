//SoundPlayer.kt
package com.example.rts

import android.content.Context
import android.media.MediaPlayer

class SoundPlayer(private val context: Context) {
    fun playSound(soundId: Int) {
        val mediaPlayer = MediaPlayer.create(context, soundId)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mediaPlayer.release() }
    }
}