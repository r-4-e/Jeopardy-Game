package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Collections

interface SoundEffectsPlayer {
    val isMuted: StateFlow<Boolean>
    fun toggleMute()
    fun playCorrect()
    fun playTierUnlock()
    fun playIncorrect()
    fun playSelect()
    fun release()
}

/**
 * Plays distinct game sound effects via Android's [MediaPlayer] backed by bundled
 * raw WAV resources (`res/raw/sfx_*.wav`).
 */
class MediaPlayerSoundEffectsManager(context: Context) : SoundEffectsPlayer {
    private val appContext: Context = context.applicationContext

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val activePlayers: MutableSet<MediaPlayer> =
        Collections.synchronizedSet(mutableSetOf())

    private val gameAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    override fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    override fun playCorrect() {
        playRawSfx(R.raw.sfx_correct)
    }

    override fun playTierUnlock() {
        playRawSfx(R.raw.sfx_unlock)
    }

    override fun playIncorrect() {
        playRawSfx(R.raw.sfx_incorrect)
    }

    override fun playSelect() {
        playRawSfx(R.raw.sfx_select)
    }

    override fun release() {
        val snapshot = synchronized(activePlayers) {
            val copy = activePlayers.toList()
            activePlayers.clear()
            copy
        }
        snapshot.forEach { player ->
            safelyReleasePlayer(player)
        }
    }

    private fun playRawSfx(@RawRes resId: Int) {
        if (_isMuted.value) return
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(gameAudioAttributes)
            }
            val afd = appContext.resources.openRawResourceFd(resId) ?: run {
                player.release()
                return
            }
            afd.use { descriptor ->
                player.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length
                )
            }
            activePlayers.add(player)

            player.setOnCompletionListener { mp ->
                activePlayers.remove(mp)
                safelyReleasePlayer(mp)
            }
            player.setOnErrorListener { mp, what, extra ->
                Log.w(TAG, "MediaPlayer error (what=$what, extra=$extra)")
                activePlayers.remove(mp)
                safelyReleasePlayer(mp)
                true
            }
            player.prepare()
            player.start()
        } catch (e: Exception) {
            Log.w(TAG, "MediaPlayer SFX playback skipped: ${e.message}")
        }
    }

    private fun safelyReleasePlayer(player: MediaPlayer) {
        try {
            if (player.isPlaying) {
                player.stop()
            }
        } catch (_: Exception) {
        }
        try {
            player.release()
        } catch (_: Exception) {
        }
    }

    private companion object {
        const val TAG = "SoundEffectsManager"
    }
}
