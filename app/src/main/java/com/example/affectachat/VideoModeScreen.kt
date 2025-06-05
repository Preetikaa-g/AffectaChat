package com.example.affectachat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.affectachat.Emotion
import com.example.affectachat.emotionToEmoji
import com.example.affectachat.emotionToGradientColors

@Composable
fun VideoModeScreen(currentEmotion: Emotion) {
    val emoji = emotionToEmoji(currentEmotion)
    val colors = emotionToGradientColors(currentEmotion)
    val gradient = Brush.horizontalGradient(colors)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(brush = gradient)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Emotion: $currentEmotion $emoji",
            fontSize = 20.sp,
            color = Color.White
        )
    }
}
