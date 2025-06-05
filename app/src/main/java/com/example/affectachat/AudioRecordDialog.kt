package com.example.affectachat.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AudioRecordDialog(
    onStop: () -> Unit,
    isRecording: Boolean
) {
    AlertDialog(
        onDismissRequest = { onStop() },
        confirmButton = {},
        title = { Text("Recording Voice Note") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isRecording) {
                    MicAnimation()
                    Text("Recording...", color = Color.LightGray)
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onStop() }) {
                    Text("Stop Recording")
                }
            }
        }
    )
}

@Composable
fun MicAnimation() {
    val infiniteTransition: InfiniteTransition = rememberInfiniteTransition(label = "mic")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Icon(
        imageVector = Icons.Default.Mic,
        contentDescription = "Mic",
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        tint = Color.Red
    )
}

