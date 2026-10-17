package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AudioHelper(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("AudioHelper", "Language US not supported or missing data")
                } else {
                    isInitialized = true
                    textToSpeech?.setPitch(1.08f) // Friendly, slightly upbeat pitch for Nana
                    textToSpeech?.setSpeechRate(1.02f)
                }
            } else {
                Log.e("AudioHelper", "TextToSpeech init failed")
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String) {
        if (!isInitialized) return
        stop()
        val cleanText = text
            .replace(Regex("[*#`_~]"), "") // Strip markdown symbols
            .take(1500) // Read reasonable snippet
        val utteranceId = "nana_speech_${System.currentTimeMillis()}"
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        _isSpeaking.value = false
    }

    fun release() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
