package com.ashmeet.hyperlauncher.utils

import android.util.Log
import androidx.compose.runtime.*
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class TranslationResponse(
    val result: List<String>? = null,
    val info: String? = null
)

object Translator {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val cache = ConcurrentHashMap<Pair<String, String>, String>()
    private val languageMap = mapOf(
        "en" to "english",
        "ru" to "russian",
        "es" to "spanish",
        "fr" to "french",
        "zh" to "chinese_simplified",
        "ja" to "japanese",
        "ko" to "korean",
        "de" to "german",
        "it" to "italian",
        "pt" to "portuguese",
        "tr" to "turkish",
        "vi" to "vietnamese",
        "hi" to "hindi",
        "ar" to "arabic"
    )

    private fun getTargetLanguage(): String {
        if (LauncherPreferences.PREF_FORCE_ENGLISH) return "english"
        val lang = LauncherPreferences.PREF_LANGUAGE ?: "en"
        return languageMap[lang] ?: "english"
    }

    suspend fun translate(text: String): String {
        if (text.isBlank()) return text
        val target = getTargetLanguage()
        if (target == "english") return text
        
        val cacheKey = text to target
        cache[cacheKey]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val response: TranslationResponse = client.post("https://siliconflow.zvo.cn/translate.json") {
                    setBody(FormDataContent(Parameters.build {
                        append("to", target)
                        append("text", "[\"$text\"]")
                    }))
                }.body()

                val translated = response.result?.firstOrNull() ?: text
                cache[cacheKey] = translated
                translated
            } catch (e: Exception) {
                Log.e("Translator", "Translation failed for: $text", e)
                text
            }
        }
    }
}

/**
 * Composable that translates a given text to the current language.
 */
@Composable
fun translatedText(text: String): String {
    val target = remember { 
        if (LauncherPreferences.PREF_FORCE_ENGLISH) "english"
        else LauncherPreferences.PREF_LANGUAGE ?: "en"
    }
    
    if (target == "en" || target == "english") return text

    val translated by produceState(initialValue = text, text, target) {
        value = Translator.translate(text)
    }
    return translated
}
