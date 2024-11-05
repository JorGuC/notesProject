package com.example.notesproyect.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesproyect.Screens.TipoNota
import com.example.notesproyect.data.Note
import com.example.notesproyect.data.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log


data class NotesUiState(
    val notesList: List<Note> = emptyList(),
    val tasksList: List<Note> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
class NotesViewModel(private val notesRepository: NotesRepository) : ViewModel() {

    /************ Estado UI ************/

    val noteUiState: StateFlow<NotesUiState> = combine(
        notesRepository.getAllNotesStream(),
        notesRepository.getAllTasksStream()
    ) { notes, tasks ->
        NotesUiState(notesList = notes, tasksList = tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState()
    )


    /********** Edit **********/

    private val _isEditing = MutableStateFlow(false)

    private val _currentNote = MutableStateFlow<Note?>(null) // Nota actual
    val currentNote: StateFlow<Note?> get() = _currentNote

    private fun parseFecha(fechaString: String): LocalDateTime? {
        return try {
            // Supongamos que usaste este formato al guardar
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            LocalDateTime.parse(fechaString, formatter) // Parsear el String a LocalDateTime
        } catch (e: DateTimeParseException) {
            null // Retorna null si la fecha no se puede parsear
        }
    }

    fun fetchNoteById(noteId: Int) {
        viewModelScope.launch {
            if (noteId != -1) {
                Log.d("NotesViewModel", "Mi id es : $noteId")
                notesRepository.getNoteStream(noteId).collect { note ->
                    _currentNote.value = note
                    if (note != null) {
                        _titulo.value = note.titulo
                        _descripcion.value = note.descripcion
                        _isEditing.value = true
                    }

                }
            } else {
                _currentNote.value = null
                _isEditing.value = false

            }
        }
    }

    fun updateNoteHecha(note: Note, hecha : Boolean) {

            note?.let {
                Log.d("ViewModel", "Si se actualizo papi: $it")
                val nota = it.copy(hecha = hecha)
                Log.d("ViewModel", "Si se actualizo papi: $nota")
                viewModelScope.launch {
                    notesRepository.updateNote(nota) // Llama a tu método de actualización
                }


            }

    }

    /********** Delete **********/

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            if (noteId != -1) {
                // Obtenemos el flujo de la nota y recogemos su valor
                notesRepository.getNoteStream(noteId).collect { note ->
                    // Asegúrate de que la nota no sea null antes de intentar eliminar
                    note?.let {
                        notesRepository.deleteNote(it) // Pasar la nota al método de eliminación
                        Log.d("NotesViewModel", "Nota eliminada: $it")
                    } ?: run {
                        Log.d("NotesViewModel", "No se encontró la nota con id: $noteId")
                    }
                }
            } else {
                Log.d("NotesViewModel", "ID inválido para eliminar: $noteId")
            }
        }
    }


    /********** Pestañas de Navegación **********/

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index

    }

    /********** Campos de Notas **********/

    private val _titulo = MutableStateFlow("")
    val titulo: StateFlow<String> get() = _titulo

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> get() = _descripcion

    private val _fecha = MutableStateFlow(LocalDate.now())
    val fecha: StateFlow<LocalDate> get() = _fecha

    private val _horas = MutableStateFlow(currentHours())
    val horas: StateFlow<Int> get() = _horas

    private val _minutos = MutableStateFlow(currentMinutes())
    val minutos: StateFlow<Int> get() = _minutos

    fun updateTitle(newTitle: String) {
        _titulo.value = newTitle
    }

    fun updateDescription(newDescription: String) {
        _descripcion.value = newDescription
    }

    fun updateDate(newDate: LocalDate) {
        _fecha.value = newDate
    }

    fun updateTime(newH: Int, newM: Int) {
        _horas.value = newH
        _minutos.value = newM
    }

    /********** Funciones de Guardado **********/
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    fun saveNote(tipo: String) {


        val fechaCreacion = dateFormat.format(Date()) // Convertimos la fecha actual a String

        // Convertimos `LocalDateTime` a `Date` para formatear correctamente `fechaVencimiento`
        val fechaVencimiento = _fecha.value?.let {
            val vencimientoDate =
                Date.from(fechaConHora().atZone(ZoneId.systemDefault()).toInstant())
            dateFormat.format(vencimientoDate) // Formateamos como String
        }

        val nuevaNota = Note(
            id = if (_isEditing.value) _currentNote.value?.id ?: 0 else 0,
            tipo = if (_isEditing.value) _currentNote.value?.tipo ?: "NOTA" else tipo,
            hecha = if (_isEditing.value) _currentNote.value?.hecha ?: false else false,
            titulo = _titulo.value,
            descripcion = _descripcion.value,
            fechaCreacion = fechaCreacion,
            fechaVencimiento = fechaVencimiento,
            recordatorioTimestamp = System.currentTimeMillis(),
            archivosAdjuntos = ""

        )

        viewModelScope.launch {
            if (_isEditing.value) {
                // Lógica para actualizar la nota existente
                Log.d("NotesViewModel", "Estoy actualizando : $nuevaNota")
                notesRepository.updateNote(nuevaNota) // Asegúrate de tener este método en tu repositorio
            } else {
                // Lógica para insertar una nueva nota
                Log.d("NotesViewModel", "Estoy agregando : $nuevaNota")
                notesRepository.insertNote(nuevaNota) // Insertamos la nota en la base de datos
            }
        }

        // Limpiamos los campos después de guardar
        _titulo.value = ""
        _descripcion.value = ""
        _isEditing.value = false // Reseteamos el estado
    }

    private fun fechaConHora(): LocalDateTime {
        return LocalDateTime.of(_fecha.value, LocalTime.of(_horas.value, _minutos.value))
    }

    private fun currentHours() = LocalDateTime.now().hour
    private fun currentMinutes() = LocalDateTime.now().minute

    /************ Pantalla principal: búsqueda y filtro ************/

    private val _buscar = MutableStateFlow("")
    val buscar: StateFlow<String> get() = _buscar

    fun onSearchChange(search: String) {
        _buscar.value = search

    }

    val listaFiltradas: StateFlow<List<Note>> = combine(
        noteUiState,
        _selectedTabIndex,
        _buscar
    ) { uiState, selectedTab, search ->

        val listaVisible = if (selectedTab == 0) uiState.notesList else uiState.tasksList


        if (search.isBlank()) listaVisible else {
            listaVisible.filter {
                it.titulo.contains(search, ignoreCase = true) ||
                        it.descripcion.contains(search, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
