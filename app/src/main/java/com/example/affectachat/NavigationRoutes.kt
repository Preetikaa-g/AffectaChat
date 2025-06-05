// NavigationRoutes.kt
package com.example.affectachat

sealed class Screen(val route: String) {
    object ModeSelection : Screen("mode_selection")
    object TextMode : Screen("text_mode")
    object VideoMode : Screen("video_mode")
    object AudioMode : Screen("audio_mode")
    object Journal : Screen("journal") // ✅ Add this
    object Settings : Screen("settings") // ✅ And this
}

