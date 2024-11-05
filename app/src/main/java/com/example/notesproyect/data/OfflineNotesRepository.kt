package com.example.notesproyect.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

class OfflineNotesRepository(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNotesStream(): Flow<List<Note>> = noteDao.getAllNotes()

    override fun getAllTasksStream(): Flow<List<Note>> = noteDao.getAllTasks()

    override fun getNoteStream(id: Int): Flow<Note?> = noteDao.getNote(id)

    override suspend fun insertNote(item: Note) = noteDao.insert(item)

    override suspend fun deleteNote(item: Note) = noteDao.delete(item)

    override suspend fun updateNote(item: Note) = noteDao.update(item)

}
