//SoundPlayer.kt
package com.example.rts

import android.content.Context
import android.media.MediaPlayer

class SoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    fun playSound(soundId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundId)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}