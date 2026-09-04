package com.ashmeet.hyperlauncher.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import com.kdt.mcgui.ProgressLayout
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import java.util.concurrent.ConcurrentHashMap
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.ashmeet.hyperlauncher.R
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class TranslationResponse(
    val result: JsonElement? = null,
    val text: List<String>? = null,
    val info: String? = null
)

@Serializable
data class PersistentCache(
    val translations: Map<String, Map<String, String>> = emptyMap() // language -> (original -> translated)
)

object Translator {
    // if we needed to add more services just in case
    private val TRANSLATION_SERVICES = listOf(
        "https://api.translate.zvo.cn/translate.json"
    )

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }
    }

    private fun extractResults(response: TranslationResponse): List<String>? {
        response.text?.let { return it }
        val res = response.result ?: return null
        return if (res is JsonArray) {
            res.map { it.jsonPrimitive.content }
        } else {
            null
        }
    }

    private val cache = ConcurrentHashMap<String, ConcurrentHashMap<String, String>>()
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

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var prefetchJob: Job? = null
    private var appContext: Context? = null
    private const val PROGRESS_KEY = ProgressLayout.DOWNLOAD_TRANSLATIONS
    private var isInitialized = false

    // State to trigger UI updates when a language is finished downloading
    private val _refreshTrigger = mutableIntStateOf(0)
    val refreshTrigger: State<Int> = _refreshTrigger

    fun getTargetLanguage(): String {
        if (LauncherPreferences.PREF_FORCE_ENGLISH) return "english"
        var lang = LauncherPreferences.PREF_LANGUAGE ?: "system"
        if (lang == "system") {
            lang = java.util.Locale.getDefault().language
        }
        return languageMap[lang] ?: "english"
    }

    private fun getCacheFile(context: Context): File {
        return File(context.filesDir, "translations_cache.json")
    }

    fun init(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        scope.launch(Dispatchers.IO) {
            try {
                val file = getCacheFile(context)
                if (file.exists()) {
                    val content = file.readText()
                    val persistentCache = Json.decodeFromString<PersistentCache>(content)
                    persistentCache.translations.forEach { (lang, map) ->
                        cache[lang] = ConcurrentHashMap(map)
                    }
                }
            } catch (e: Exception) {
                Log.e("Translator", "Failed to load cache", e)
            }
            isInitialized = true
            
            // Trigger prefetch on init if needed
            prefetchTranslations(context)
        }
    }

    private fun saveCache(context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                val persistentCache = PersistentCache(cache.mapValues { it.value.toMap() })
                getCacheFile(context).writeText(Json.encodeToString(persistentCache))
            } catch (e: Exception) {
                Log.e("Translator", "Failed to save cache", e)
            }
        }
    }

    suspend fun translate(text: String, targetLanguage: String? = null): String {
        if (text.isBlank()) return text
        val target = targetLanguage ?: getTargetLanguage()
        if (target == "english") return text
        
        cache[target]?.get(text)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val jsonArray = Json.encodeToString(listOf(text))
                var lastException: Exception? = null
                var translated: String? = null

                for (serviceUrl in TRANSLATION_SERVICES) {
                    try {
                        val httpResponse: HttpResponse = client.post(serviceUrl) {
                            setBody(FormDataContent(Parameters.build {
                                append("to", target)
                                append("text", jsonArray)
                            }))
                        }

                        if (httpResponse.status.isSuccess() && httpResponse.contentType()?.match(ContentType.Application.Json) == true) {
                            val response: TranslationResponse = httpResponse.body()
                            translated = extractResults(response)?.firstOrNull()
                            if (translated != null) break
                        } else {
                            Log.w("Translator", "Service $serviceUrl returned ${httpResponse.status}")
                        }
                    } catch (e: Exception) {
                        lastException = e
                        Log.w("Translator", "Service $serviceUrl failed, trying next...")
                    }
                }

                if (translated == null && lastException != null) throw lastException

                val finalResult = translated ?: text
                val langCache = cache.getOrPut(target) { ConcurrentHashMap() }
                langCache[text] = finalResult
                
                // Save cache after individual translation
                appContext?.let { saveCache(it) }
                
                finalResult
            } catch (e: Exception) {
                Log.e("Translator", "Translation failed for: $text", e)
                text
            }
        }
    }

    fun prefetchTranslations(context: Context) {
        val target = getTargetLanguage()
        if (target == "english") {
            ProgressKeeper.submitProgress(PROGRESS_KEY, -1, -1)
            return
        }

        prefetchJob?.cancel()
        prefetchJob = scope.launch {
            val myJob = coroutineContext[Job]
            try {
                val strings = extractStrings(context)
                if (strings.isEmpty()) return@launch

                val langCache = cache.getOrPut(target) { ConcurrentHashMap() }
                val toTranslate = strings.filter { !langCache.containsKey(it) }
                if (toTranslate.isEmpty()) {
                    _refreshTrigger.intValue++ // Already up to date
                    return@launch
                }

                val totalCount = toTranslate.size
                val capitalizedTarget = target.replaceFirstChar { it.uppercase() }
                
                ProgressKeeper.submitProgress(PROGRESS_KEY, 0, R.string.translation_prefetching, capitalizedTarget, 0, totalCount)

                val batchSize = 20
                val batches = toTranslate.chunked(batchSize)
                var completed = 0

                for (batch in batches) {
                    ensureActive()
                    var retryCount = 0
                    var success = false
                    while (retryCount < 3 && !success) {
                        ensureActive()
                        try {
                            val jsonArray = Json.encodeToString(batch)
                            
                            var batchResults: List<String>? = null
                            var lastException: Exception? = null

                            for (serviceUrl in TRANSLATION_SERVICES) {
                                try {
                                    val httpResponse: HttpResponse = withContext(Dispatchers.IO) {
                                        client.post(serviceUrl) {
                                            setBody(FormDataContent(Parameters.build {
                                                append("to", target)
                                                append("text", jsonArray)
                                            }))
                                        }
                                    }

                                    if (httpResponse.status.isSuccess() && httpResponse.contentType()?.match(ContentType.Application.Json) == true) {
                                        val response: TranslationResponse = httpResponse.body()
                                        batchResults = extractResults(response)
                                        if (batchResults != null) break
                                    } else {
                                        Log.w("Translator", "Service $serviceUrl returned ${httpResponse.status} for batch")
                                    }
                                } catch (e: Exception) {
                                    lastException = e
                                    Log.w("Translator", "Service $serviceUrl failed for batch, trying next...")
                                }
                            }

                            if (batchResults == null && lastException != null) throw lastException

                            batchResults?.let { results ->
                                batch.zip(results).forEach { (original, translated) ->
                                    langCache[original] = translated
                                }
                            }
                            success = true
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            retryCount++
                            Log.e("Translator", "Batch translation failed (retry $retryCount/3)", e)
                            if (retryCount < 3) delay((2000L * retryCount).milliseconds)
                            else throw e
                        }
                    }

                    completed += batch.size
                    val progress = (completed.toFloat() / totalCount * 100).toInt()
                    ProgressKeeper.submitProgress(PROGRESS_KEY, progress, R.string.translation_prefetching, capitalizedTarget, completed, totalCount)
                    
                    // Respect rate limit (2 requests per 2 seconds)
                    delay(1100L.milliseconds)
                }

                saveCache(context)
                // Trigger UI refresh after prefetch is done
                _refreshTrigger.intValue++
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("Translator", "Prefetch failed", e)
                }
            } finally {
                // Only clear if this is still the active job
                if (prefetchJob == myJob) {
                    ProgressKeeper.submitProgress(PROGRESS_KEY, -1, -1)
                }
            }
        }
    }

    private fun extractStrings(context: Context): List<String> {
        val strings = mutableListOf<String>()
        try {
            val fields = R.string::class.java.fields
            for (field in fields) {
                try {
                    val id = field.getInt(null)
                    val s = context.getString(id)
                    if (s.isNotBlank() && !s.startsWith("http") && s.length > 1) {
                        strings.add(s)
                    }
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            Log.e("Translator", "Failed to extract strings", e)
        }
        return strings.distinct()
    }
}

/**
 * Composable that translates a given text to the current language.
 */
@Composable
fun translatedText(text: String): String {
    // Session-fixed language for this composable instance.
    // It will only update if the composable is re-created (e.g. screen reload)
    val target = remember { Translator.getTargetLanguage() }
    
    if (target == "en" || target == "english") return text

    // Use refreshTrigger to force re-evaluation when prefetch completes
    val trigger by Translator.refreshTrigger

    val translated by produceState(initialValue = text, text, target, trigger) {
        value = Translator.translate(text, target)
    }
    return translated
}
