package com.example.notesfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notesfrontend.ui.theme.NotesFrontendTheme

// PUBLIC_INTERFACE
class MainActivity : ComponentActivity() {
    /**
     * Entrypoint activity for the notes app. Sets up Compose content and the main UI/VM.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesFrontendTheme {
                val viewModel: NotesViewModel by viewModels()
                NotesApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp(viewModel: NotesViewModel) {
    val notes by viewModel.notes.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val dialogMode by viewModel.dialogMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Notes", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openDialog(create = true) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White)
        ) {
            // Minimal split view: List and (if present) note details side by side
            Row(modifier = Modifier.fillMaxSize()) {
                // Note list (weights enable simple split)
                NotesList(
                    notes = notes,
                    onNoteClick = { viewModel.selectNote(it) },
                    selectedNoteId = selectedNote?.id,
                    modifier = Modifier.weight(1.0f)
                )
                // Details panel
                if (selectedNote != null) {
                    NoteDetail(
                        note = selectedNote!!,
                        onEdit = { viewModel.openDialog(create = false) },
                        onDelete = { viewModel.deleteNote(selectedNote!!.id) },
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .background(Color(0xFFF8F8F8))
                    )
                }
            }
            // Add/Edit note dialog
            if (showDialog) {
                NoteDialog(
                    mode = dialogMode,
                    note = if (dialogMode == NoteDialogMode.EDIT) selectedNote else null,
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = { title, content ->
                        if (dialogMode == NoteDialogMode.CREATE)
                            viewModel.createNote(title, content)
                        else
                            viewModel.editNote(title, content)
                    }
                )
            }
            if (isLoading) {
                // Light minimal loading overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x88FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    selectedNoteId: String?,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxHeight().background(Color.White)) {
        LazyColumn {
            items(notes, key = { it.id }) { note ->
                val isSelected = note.id == selectedNoteId
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNoteClick(note) }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                    ) {
                        Text(
                            note.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteDetail(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight().padding(18.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    note.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(note.content, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
        }
    }
}

@Composable
fun NoteDialog(
    mode: NoteDialogMode,
    note: Note?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    val isCreate = mode == NoteDialogMode.CREATE
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isCreate) "Create Note" else "Edit Note",
                color = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title.trim(), content.trim())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(if (isCreate) "Create" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
