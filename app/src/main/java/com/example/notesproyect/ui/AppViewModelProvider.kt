package com.example.notesproyect.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notesproyect.StorageApplication
import com.example.notesproyect.viewmodel.NotesViewModel

@RequiresApi(Build.VERSION_CODES.O)
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            NotesViewModel(storageApplication().container.notesRepository)
        }
    }
}

fun CreationExtras.storageApplication(): StorageApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as StorageApplication)

