package com.example.notesfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Main ViewModel for Notes. Handles state and interaction with the repository.
 */
// PUBLIC_INTERFACE
class NotesViewModel : ViewModel() {
    private val repo = NotesRepository()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _dialogMode = MutableStateFlow(NoteDialogMode.CREATE)
    val dialogMode: StateFlow<NoteDialogMode> = _dialogMode.asStateFlow()

    init {
        refreshNotes()
    }

    // PUBLIC_INTERFACE
    fun refreshNotes() {
        _isLoading.value = true
        viewModelScope.launch {
            _notes.value = repo.listNotes()
            _isLoading.value = false
        }
    }

    // PUBLIC_INTERFACE
    fun selectNote(note: Note) {
        _selectedNote.value = note
    }

    // PUBLIC_INTERFACE
    fun openDialog(create: Boolean) {
        _dialogMode.value = if (create) NoteDialogMode.CREATE else NoteDialogMode.EDIT
        _showDialog.value = true
    }

    // PUBLIC_INTERFACE
    fun closeDialog() {
        _showDialog.value = false
    }

    // PUBLIC_INTERFACE
    fun createNote(title: String, content: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repo.createNote(title, content)
            _showDialog.value = false
            refreshNotes()
        }
    }

    // PUBLIC_INTERFACE
    fun editNote(title: String, content: String) {
        val note = _selectedNote.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            repo.editNote(note.id, title, content)
            _showDialog.value = false
            refreshNotes()
        }
    }

    // PUBLIC_INTERFACE
    fun deleteNote(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repo.deleteNote(id)
            _selectedNote.value = null
            refreshNotes()
        }
    }
}
