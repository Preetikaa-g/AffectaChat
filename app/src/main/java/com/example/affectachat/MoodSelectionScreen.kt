package com.example.affectachat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.affectachat.Screen

@Composable
fun MoodSelectionScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF001F3F), Color(0xFF003366))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "AffectaChat",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "We Are Your Companions",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Chat With Us",
                    fontSize = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatModeButton("TEXT") {
                        navController.navigate(Screen.TextMode.route)
                    }
                    ChatModeButton("VIDEO") {
                        navController.navigate(Screen.VideoMode.route)
                    }
                    ChatModeButton("AUDIO") {
                        navController.navigate(Screen.AudioMode.route)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Your Comfort and Privacy is all we need",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            BottomNavigationBar(navController)
        }
    }
}

@Composable
fun ChatModeButton(text: String, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF005BEA) else Color(0xFF007AFF), label = "button-color"
    )

    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .height(50.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF4A90E2), backgroundColor)
                ),
                shape = shape
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .background(Color(0xFF002244))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = "Journal",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    navController.navigate("journal")
                }
        )
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    navController.navigate("mode_selection")
                }
        )
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    navController.navigate("settings")
                }
        )
    }
}
