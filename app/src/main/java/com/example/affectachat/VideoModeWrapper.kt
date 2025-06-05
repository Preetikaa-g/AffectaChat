package com.example.affectachat.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.affectachat.*
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun VideoModeWrapper() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var audioPermissionGranted by remember { mutableStateOf(false) }
    var currentEmotion by remember { mutableStateOf(Emotion.UNKNOWN) }
    var lastEmotion by remember { mutableStateOf(Emotion.UNKNOWN) }
    var hasReactedToEmotion by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val ttsRef = remember { mutableStateOf<TextToSpeech?>(null) }
    val deviceLocale = Locale.getDefault()
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    var isListening by remember { mutableStateOf(false) }
    var enableMoodDetection by remember { mutableStateOf(false) }

    val listenForUser = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, deviceLocale)
        }
        speechRecognizer.startListening(intent)
        isListening = true
    }

    LaunchedEffect(Unit) {
        val tempTtsRef = arrayOfNulls<TextToSpeech>(1)
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val t = tempTtsRef[0]
                if (t != null) {
                    val result = t.setLanguage(deviceLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        t.setLanguage(Locale.US)
                    }
                    t.setSpeechRate(0.95f)
                    ttsRef.value = t
                }
            }
        }
        tempTtsRef[0] = tts
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsRef.value?.stop()
            ttsRef.value?.shutdown()
            speechRecognizer.destroy()
            mediaPlayer?.release()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermissionGranted = granted }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> audioPermissionGranted = granted }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            cameraPermissionGranted = true
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            audioPermissionGranted = true
        }
    }

    if (cameraPermissionGranted && audioPermissionGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (enableMoodDetection) {
                CameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onEmotionDetected = { detectedEmotion ->
                        if (detectedEmotion != lastEmotion) {
                            currentEmotion = detectedEmotion
                            lastEmotion = detectedEmotion
                            hasReactedToEmotion = false
                        }

                        if (!hasReactedToEmotion && detectedEmotion != Emotion.UNKNOWN) {
                            hasReactedToEmotion = true
                            coroutineScope.launch {
                                val msg = "You look ${detectedEmotion.name.lowercase()}. Do you want to talk?"
                                ttsRef.value?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    }
                )
            }

            LaunchedEffect(Unit) {
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val userInput = matches?.firstOrNull()?.trim() ?: return

                        coroutineScope.launch {
                            val prompt = when (currentEmotion) {
                                Emotion.HAPPY -> "You're in a happy mood. The user says: '$userInput'. Respond excitedly."
                                Emotion.SAD -> "You're in a sad mood. The user says: '$userInput'. Respond with comfort."
                                Emotion.ANGRY -> "You're in an angry mood. The user says: '$userInput'. Respond calmly and helpfully."
                                Emotion.TIRED -> "You're tired. The user says: '$userInput'. Respond softly and supportively."
                                Emotion.CONFUSED -> "You're confused. The user says: '$userInput'. Respond clearly and kindly."
                                Emotion.ANNOYED -> "You're annoyed. The user says: '$userInput'. Respond gently and light-heartedly."
                                Emotion.CALM -> "You're calm. The user says: '$userInput'. Respond peacefully and friendly."
                                Emotion.NEUTRAL -> "You're neutral. The user says: '$userInput'. Respond naturally."
                                else -> "The user says: '$userInput'. Respond appropriately."
                            }
                            val reply = fetchAIResponseMistral(prompt)
                            ttsRef.value?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) { isListening = false }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            Column(modifier = Modifier.fillMaxSize()) {
                VideoModeScreen(currentEmotion = currentEmotion)

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (isListening) Color.Red else Color.Gray,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { enableMoodDetection = !enableMoodDetection }) {
                        Text(if (enableMoodDetection) "Stop Mood Scan" else "Start Mood Scan")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = listenForUser) {
                        Text("🎤 Speak")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera and microphone permissions are required.",
                color = Color.Red,
                fontSize = 18.sp
            )
        }
    }
}