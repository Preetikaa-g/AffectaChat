package com.example.affectachat.ui

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.affectachat.BuildConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import org.json.JSONException

@Composable
fun TextModeScreen() {
    var userInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Triple<String, Boolean, String>>()) }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F3F))
            .padding(16.dp)
    ) {
        Text(
            text = "Today ${LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))}",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            reverseLayout = true
        ) {
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
            items(messages.reversed()) { (message, isUser, timestamp) ->
                MessageBubble(text = message, isUser = isUser, timestamp = timestamp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF003366), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.White)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (userInput.trim().isNotEmpty()) {
                        val input = userInput.trim()
                        val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
                        messages = messages + Triple(input, true, "$currentTime · Sent")
                        userInput = ""
                        isTyping = true

                        coroutineScope.launch {
                            try {
                                val response =  fetchAIResponseMistral(input)
                                val responseTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
                                messages = messages + Triple(response, false, "$responseTime · Seen")
                            } catch (e: Exception) {
                                Log.e("TextModeScreen", "AI error", e)
                                messages = messages + Triple("Error: ${e.localizedMessage}", false, "")
                            } finally {
                                isTyping = false
                            }
                        }
                    }
                },
                modifier = Modifier.height(50.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "dots")
    val scale1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "dot1"
    )
    val scale2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "dot2"
    )
    val scale3 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(modifier = Modifier.padding(12.dp)) {
        listOf(scale1, scale2, scale3).forEach {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(it)
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(text: String, isUser: Boolean, timestamp: String) {
    val backgroundColor = if (isUser) Color(0xFF336699) else Color(0xFF1A1A2E)
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val shape = if (isUser) RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
    else RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape)
                .padding(12.dp)
                .align(alignment)
                .widthIn(min = 50.dp, max = 280.dp)
        ) {
            Text(text = text, color = Color.White, fontSize = 16.sp)
        }
        if (timestamp.isNotEmpty()) {
            Text(
                text = timestamp,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(alignment).padding(top = 2.dp)
            )
        }
    }
}

suspend fun fetchAIResponseMistral(userMessage: String): String {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return try {
        val response: HttpResponse = client.post("https://api.mistral.ai/v1/chat/completions") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
                append(HttpHeaders.Authorization, "Bearer ${BuildConfig.MISTRAL_API_KEY}")
            }
            setBody(
                """
    {
      "model": "mistral-medium",
      "temperature": 1.0,
      "top_p": 0.95,
      "messages": [
        {
          "role": "system",
          "content": "You are a deeply empathetic, expressive, and warm companion. Reply with personality, variation, and human-like compassion. Understand and reply in the user's language or transliteration format."
        },
        {
          "role": "user",
          "content": "$userMessage"
        }
      ]
    }
    """.trimIndent()
            )


        }

        val raw = response.bodyAsText()
        val json = JSONObject(raw)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

    } catch (e: Exception) {
        Log.e("MistralAPI", "Error", e)
        "Oops, I'm having trouble answering that right now."
    }
}
