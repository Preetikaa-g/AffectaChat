package com.example.affectachat.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.affectachat.BuildConfig
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@Composable
fun VoiceChatMoodSelect() {
    val context = LocalContext.current
    var selectedVoice by rememberSaveable { mutableStateOf("female") }
    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val scrollState = rememberScrollState()
    val availableVoices = remember { mutableStateListOf<Voice>() }

    val tts = remember(selectedVoice) {
        TextToSpeech(context) {
            if (it != TextToSpeech.SUCCESS) {
                Toast.makeText(context, "TTS init failed", Toast.LENGTH_SHORT).show()
            }
        }.apply {
            language = Locale.US
            val voicesList = voices?.toList() ?: emptyList()
            availableVoices.clear()
            availableVoices.addAll(voicesList)
            val maleVoice = voicesList.find { it.name.contains("en-us-x-sfg#male", ignoreCase = true) }
            val femaleVoice = voicesList.find { it.name.contains("en-us-x-sfg#female", ignoreCase = true) }
            val chosenVoice = if (selectedVoice == "male") maleVoice ?: femaleVoice else femaleVoice ?: maleVoice
            chosenVoice?.let { this.voice = it }
        }
    }

    var isListening by remember { mutableStateOf(false) }
    var response by remember { mutableStateOf("Tap the mic and talk to your AI friend.") }
    var loading by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) {
                        isListening = false
                        loading = true
                        response = "Thinking..."

                        CoroutineScope(Dispatchers.Main).launch {
                            response = callMistralAI(spoken)
                            loading = false
                            isSpeaking = true
                            tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    } else {
                        response = "Sorry, I didn't hear anything."
                        isListening = false
                    }
                }

                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) {
                    isListening = false
                    response = "Sorry, something went wrong."
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val barHeight by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barHeight"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFF001F3F))
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DropdownMenuDemo(selectedVoice) { selectedVoice = it }
        Spacer(modifier = Modifier.height(16.dp))

        Text("AI Voice Friend", fontSize = 24.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFF003366),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(
                response,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSpeaking) {
            Row(modifier = Modifier.height(32.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .width(6.dp)
                            .height(barHeight.dp)
                            .clip(RectangleShape)
                            .background(Color.Cyan)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004BA0))
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isListening) "Listening..."
                    else if (loading) "Thinking..."
                    else "Talk",
                    color = Color.White
                )
            }

            if (isSpeaking) {
                Button(
                    onClick = {
                        tts.stop()
                        isSpeaking = false
                        isPaused = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop Speaking", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop", color = Color.White)
                }

                Button(
                    onClick = {
                        if (isPaused) {
                            tts.playSilentUtterance(1, TextToSpeech.QUEUE_ADD, null)
                            isPaused = false
                        } else {
                            tts.stop()
                            isPaused = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Resume" else "Pause", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun DropdownMenuDemo(selectedVoice: String, onVoiceSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("female", "male")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { expanded = true }) {
            Text("Voice: $selectedVoice", color = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(label.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium)
                    },
                    onClick = {
                        onVoiceSelected(label)
                        expanded = false
                    }
                )
            }
        }
    }
}

suspend fun callMistralAI(prompt: String): String = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val apiKey = BuildConfig.MISTRAL_API_KEY

        val json = JSONObject().apply {
            put("model", "mistral-tiny")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a warm, emotionally intelligent AI friend. Respond with empathy, using gentle tone and brief human-like language. Always acknowledge the user's feelings and speak in short, thoughtful replies.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.mistral.ai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext "Hmm... I had trouble thinking. Try again?"
            val jsonResponse = JSONObject(response.body?.string() ?: return@withContext "No reply")
            return@withContext jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    } catch (e: Exception) {
        return@withContext "Oops, I ran into a problem."
    }
}
