package com.example.affectachat.ui

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.affectachat.R
import java.io.File

@Composable
fun SettingsScreen(onClearData: () -> Unit, onExport: () -> Unit) {
    val context = LocalContext.current
    var showUserDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedLanguage by remember { mutableStateOf("English") }
    val availableLanguages = listOf("English", "Hindi", "Tamil", "Spanish")

    val storageDir = context.cacheDir
    val storageSize = File(storageDir.path).walkBottomUp().filter { it.isFile }.map { it.length() }.sum() / 1024

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Settings", fontSize = 26.sp, color = Color.White)

        SettingItem(
            icon = Icons.Default.AccountCircle,
            title = "User Details",
            description = "View or update your profile information",
            onClick = { showUserDialog = true }
        )

        SettingItem(
            icon = Icons.Default.Storage,
            title = "Storage",
            description = "App is using ~${storageSize}KB",
            onClick = {
                onExport()
                Toast.makeText(context, "Export started", Toast.LENGTH_SHORT).show()
            }
        )

        SettingItem(
            icon = Icons.Default.Language,
            title = "Language",
            description = "Current: $selectedLanguage",
            onClick = {
                selectedLanguage = when (selectedLanguage) {
                    "English" -> "Hindi"
                    "Hindi" -> "Tamil"
                    "Tamil" -> "Spanish"
                    else -> "English"
                }
                Toast.makeText(context, "Language set to $selectedLanguage", Toast.LENGTH_SHORT).show()
            }
        )

        SettingItem(
            icon = Icons.Default.Delete,
            title = "Clear All Data",
            description = "Delete all journal notes and recordings",
            onClick = {
                onClearData()
                Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showUserDialog) {
        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text("Edit User Details") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile_placeholder),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable {
                                Toast.makeText(context, "Profile image edit coming soon", Toast.LENGTH_SHORT).show()
                            }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Name updated to ${userName.text}", Toast.LENGTH_SHORT).show()
                    showUserDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF002B5C))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color.White)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 18.sp)
                Text(description, color = Color.LightGray, fontSize = 14.sp)
            }
        }
    }
}
