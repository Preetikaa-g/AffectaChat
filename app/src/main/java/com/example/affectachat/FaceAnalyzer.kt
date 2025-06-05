package com.example.affectachat

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*

fun mapFaceToEmotion(face: Face?): Emotion {
    if (face == null) return Emotion.UNKNOWN

    val smileProb = face.smilingProbability ?: -1f
    val leftEyeOpen = face.leftEyeOpenProbability ?: -1f
    val rightEyeOpen = face.rightEyeOpenProbability ?: -1f
    val eyesOpenAvg = listOf(leftEyeOpen, rightEyeOpen).filter { it >= 0 }.average().toFloat()
    val eyeDiff = kotlin.math.abs(leftEyeOpen - rightEyeOpen)

    Log.d("FaceAnalyzer", "Smile: $smileProb, EyesOpenAvg: $eyesOpenAvg, EyeDiff: $eyeDiff")

    return when {
        smileProb > 0.8f && eyesOpenAvg > 0.5f -> Emotion.HAPPY
        smileProb in 0.4f..0.6f && eyesOpenAvg > 0.5f -> Emotion.CALM
        smileProb < 0.3f && eyesOpenAvg < 0.4f -> Emotion.TIRED
        smileProb < 0.2f && eyeDiff > 0.2f -> Emotion.CONFUSED
        smileProb < 0.2f && eyesOpenAvg > 0.6f && eyeDiff < 0.1f -> Emotion.ANNOYED
        smileProb < 0.1f && eyesOpenAvg > 0.7f -> Emotion.ANGRY
        smileProb < 0.1f && eyesOpenAvg > 0.5f -> Emotion.SAD
        smileProb in 0.2f..0.4f && eyesOpenAvg > 0.5f -> Emotion.NEUTRAL
        else -> Emotion.UNKNOWN
    }
}

fun emotionToEmoji(emotion: Emotion): String = when (emotion) {
    Emotion.HAPPY -> "😊"
    Emotion.CALM -> "😌"
    Emotion.TIRED -> "😴"
    Emotion.SAD -> "😢"
    Emotion.ANGRY -> "😠"
    Emotion.NEUTRAL -> "😐"
    Emotion.UNKNOWN -> "❓"
    Emotion.ANNOYED -> "😒"
    Emotion.CONFUSED -> "😕"
}

fun emotionToGradientColors(emotion: Emotion): List<Color> = when (emotion) {
    Emotion.HAPPY -> listOf(Color(0xFFFFE57F), Color(0xFFFFC107))
    Emotion.CALM -> listOf(Color(0xFF80D8FF), Color(0xFF0091EA))
    Emotion.TIRED -> listOf(Color(0xFFB39DDB), Color(0xFF7E57C2))
    Emotion.SAD -> listOf(Color(0xFF90CAF9), Color(0xFF1E88E5))
    Emotion.ANGRY -> listOf(Color(0xFFFF8A80), Color(0xFFD32F2F))
    Emotion.NEUTRAL -> listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    Emotion.UNKNOWN -> listOf(Color(0xFFEEEEEE), Color(0xFFB0BEC5))
    Emotion.ANNOYED -> listOf(Color(0xFFFFCDD2), Color(0xFFEF5350))
    Emotion.CONFUSED -> listOf(Color(0xFFCE93D8), Color(0xFF8E24AA))
}

class FaceAnalyzer(
    private val onFaceDetected: (Face?) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .enableTracking()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    onFaceDetected(faces.firstOrNull())
                }
                .addOnFailureListener {
                    Log.e("FaceAnalyzer", "Detection failed", it)
                    onFaceDetected(null)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}