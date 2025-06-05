package com.example.affectachat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.*
import com.example.affectachat.ui.*
import com.example.affectachat.ui.theme.AffectaChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AffectaChatTheme {
                val navController = rememberNavController()
                var currentEmotion by rememberSaveable { mutableStateOf(Emotion.HAPPY) }



                NavHost(
                    navController = navController,
                    startDestination = Screen.ModeSelection.route
                ) {
                    composable(Screen.ModeSelection.route) {
                        MoodSelectionScreen(navController)
                    }
                    composable(Screen.Journal.route) {
                        JournalScreen(currentEmotion = currentEmotion)
                    }


                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onClearData = { /* clear data logic */ },
                            onExport = { /* export logic */ }
                        )


                    }
                    composable(Screen.TextMode.route) {
                        TextModeScreen()
                    }
                    composable("audio_mode") {
                        VoiceChatMoodSelect()


                }
                        composable(Screen.VideoMode.route) {
                            VideoModeWrapper() // initial value
                        }

                    }
                }
            }
        }
    }

