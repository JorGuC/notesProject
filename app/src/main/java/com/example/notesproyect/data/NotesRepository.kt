package com.example.notesproyect.data

import android.util.Log
import kotlinx.coroutines.flow.Flow


interface NotesRepository {

    fun getAllNotesStream(): Flow<List<Note>>

    fun getAllTasksStream(): Flow<List<Note>>

    fun getNoteStream(id: Int): Flow<Note?>

    suspend fun insertNote(item: Note)


    suspend fun deleteNote(item: Note)

    suspend fun updateNote(item: Note)
}
