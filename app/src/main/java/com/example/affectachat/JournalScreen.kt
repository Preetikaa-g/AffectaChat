package com.example.affectachat.ui

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.affectachat.Emotion
import com.example.affectachat.emotionToEmoji
import com.example.affectachat.model.TempJournalEntry
import java.io.File

@Composable
fun JournalScreen(currentEmotion: Emotion) {
    val context = LocalContext.current

    var entries by remember { mutableStateOf<List<TempJournalEntry>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<TempJournalEntry?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }

    var showAudioDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFilePath by remember { mutableStateOf("") }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTarget: TempJournalEntry? by remember { mutableStateOf(null) }
    var renameText by remember { mutableStateOf(TextFieldValue("")) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val dir = File(context.cacheDir, "audio")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "voice_${System.currentTimeMillis()}.m4a"
            val file = File(dir, fileName)
            audioFilePath = file.absolutePath

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }

            isRecording = true
            showAudioDialog = true
        } else {
            Toast.makeText(context, "Mic permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        showAudioDialog = false

        entries = entries + TempJournalEntry(
            id = System.currentTimeMillis(),
            title = "",
            content = audioFilePath,
            emotion = currentEmotion,
            isAudio = true
        )
    }

    fun saveEntry(title: String, note: String) {
        if (editingEntry != null) {
            entries = entries.map {
                if (it.id == editingEntry!!.id) it.copy(title = title, content = note, emotion = currentEmotion)
                else it
            }
        } else {
            entries = entries + TempJournalEntry(
                id = System.currentTimeMillis(),
                title = title,
                content = note,
                emotion = currentEmotion
            )
        }
        titleText = ""
        inputText = ""
        editingEntry = null
        showDialog = false
    }

    fun deleteEntry(id: Long) {
        entries = entries.filterNot { it.id == id }
    }

    fun playAudio(filePath: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
        }
    }

    fun renameAudio(entry: TempJournalEntry, newName: String) {
        val oldFile = File(entry.content)
        val newFile = File(oldFile.parent, "$newName.m4a")
        if (oldFile.exists() && oldFile.renameTo(newFile)) {
            entries = entries.map {
                if (it.id == entry.id) it.copy(content = newFile.absolutePath)
                else it
            }
        } else {
            Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF001F3F))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Your Journal", color = Color.White, fontSize = 24.sp)
            Spacer(Modifier.height(12.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Notes you add appear here", color = Color.LightGray)
                }
            } else {
                LazyColumn {
                    items(entries.sortedByDescending { it.id }) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF003366))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emotionToEmoji(entry.emotion), fontSize = 20.sp, color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (entry.isAudio) "[Audio Note] ${File(entry.content).name}" else "${entry.title}\n${entry.content}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (entry.isAudio) {
                                    IconButton(onClick = { playAudio(entry.content) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                    }
                                    IconButton(onClick = {
                                        renameTarget = entry
                                        renameText = TextFieldValue(File(entry.content).nameWithoutExtension)
                                        showRenameDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.White)
                                    }
                                } else {
                                    IconButton(onClick = {
                                        editingEntry = entry
                                        titleText = entry.title
                                        inputText = entry.content
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                                    }
                                }
                                IconButton(onClick = { deleteEntry(entry.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(visible = fabExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExtendedFloatingActionButton(
                        text = { Text("Audio") },
                        icon = { Icon(Icons.Default.Mic, contentDescription = "Audio") },
                        onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
                        containerColor = Color(0xFF004BA0),
                        contentColor = Color.White
                    )
                    ExtendedFloatingActionButton(
                        text = { Text("Text") },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Text") },
                        onClick = {
                            fabExpanded = false
                            titleText = ""
                            inputText = ""
                            editingEntry = null
                            showDialog = true
                        },
                        containerColor = Color(0xFF004BA0),
                        contentColor = Color.White
                    )
                }
            }
            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = Color(0xFF80D8FF)
            ) {
                Icon(
                    imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Toggle FAB"
                )
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    inputText = ""
                    titleText = ""
                    editingEntry = null
                },
                title = { Text(if (editingEntry != null) "Edit Note" else "New Note") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = { Text("Note") },
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (titleText.isNotBlank() || inputText.isNotBlank()) {
                            saveEntry(titleText, inputText)
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        titleText = ""
                        inputText = ""
                        editingEntry = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAudioDialog) {
            AudioRecordDialog(
                onStop = { stopRecording() },
                isRecording = isRecording
            )
        }

        if (showRenameDialog && renameTarget != null) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Audio") },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("New name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        renameTarget?.let {
                            renameAudio(it, renameText.text.trim())
                        }
                        showRenameDialog = false
                    }) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
