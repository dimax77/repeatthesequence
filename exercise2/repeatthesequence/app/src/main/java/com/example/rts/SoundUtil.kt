//SoundUtil.kt
package com.example.rts

object SoundUtil {
    fun getSoundResource(soundId: Int): Int {
        return when (soundId) {
            0 -> R.raw.katana
            1 -> R.raw.machine_gun
            2 -> R.raw.reload
            3 -> R.raw.shot
            else -> throw IllegalArgumentException("Invalid sound ID")
        }
    }

    fun getTitle(soundId: Int): String {
        return when (soundId) {
            0 -> "Katana"
            1 -> "Machine Gun"
            2 -> "Reload"
            3 -> "Shot"
            else -> throw IllegalArgumentException("Invalid sound ID")
        }
    }
}