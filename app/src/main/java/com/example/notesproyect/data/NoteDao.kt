package com.example.notesproyect.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {

    @Query("SELECT * from notas WHERE tipo = 'NOTA'")
    fun getAllNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notas 
        WHERE tipo = 'TAREA' 
        ORDER BY 
            CASE WHEN hecha THEN 1 ELSE 0 END,  -- Primero las notas hechas
            fechaVencimiento ASC,  -- Luego por fecha de vencimiento
            titulo ASC  -- Finalmente por título
    """)
    fun getAllTasks(): Flow<List<Note>>

    @Query("SELECT * from notas WHERE id = :id")
    fun getNote(id: Int): Flow<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Note)

    @Update
    suspend fun update(item: Note)

    @Delete
    suspend fun delete(item: Note)
}
