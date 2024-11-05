@file:Suppress("DEPRECATION")

package com.example.notesproyect.navegacion

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notesproyect.Screens.DetailsNoteScreen
import com.example.notesproyect.Screens.Notas
import com.example.notesproyect.Screens.Principal
import com.example.notesproyect.viewmodel.NotesViewModel
import com.example.notesproyect.data.Note


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreen.FirstScreen.route) {
        composable(route = AppScreen.FirstScreen.route) {
            Principal(navController)
        }
        composable(route = AppScreen.SecondScreen.route + "/{text}/{noteId}",
            arguments = listOf(
                navArgument(name = "text") {
                    type = NavType.StringType
                },
                navArgument(name = "noteId") {
                    type = NavType.IntType // Asegúrate de que el ID sea un entero
                }
            )
        ) { backStackEntry ->
            val text = backStackEntry.arguments?.getString("text") ?: "" // Proporciona un valor por defecto si es null
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            Notas(navController, text, noteId) // Pasa ambos parámetros
        }
        composable(
            route = AppScreen.ThirdScreen.route + "/{id}/{tipo}/{titulo}/{descripcion}/{fechaCreacion}/{archivosAdjuntos}/{hecha}/{fechaVencimiento}/{recordatorioTimestamp}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("tipo") { type = NavType.StringType },
                navArgument("titulo") { type = NavType.StringType },
                navArgument("descripcion") { type = NavType.StringType },
                navArgument("fechaCreacion") { type = NavType.StringType },
                navArgument("archivosAdjuntos") { type = NavType.StringType },
                navArgument("hecha") { type = NavType.BoolType },
                navArgument("fechaVencimiento") { type = NavType.StringType },
                navArgument("recordatorioTimestamp") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
            val titulo = backStackEntry.arguments?.getString("titulo") ?: ""
            val descripcion = backStackEntry.arguments?.getString("descripcion") ?: ""
            val fechaCreacion = backStackEntry.arguments?.getString("fechaCreacion") ?: ""
            val archivosAdjuntos = backStackEntry.arguments?.getString("archivosAdjuntos") ?: ""
            val hecha = backStackEntry.arguments?.getBoolean("hecha") ?: false
            val fechaVencimiento = backStackEntry.arguments?.getString("fechaVencimiento")
            val recordatorioTimestamp = backStackEntry.arguments?.getLong("recordatorioTimestamp")

            // Aquí se crea una nueva nota con los parámetros obtenidos
            val note = Note(id, tipo, titulo, descripcion, fechaCreacion, archivosAdjuntos, hecha, fechaVencimiento, recordatorioTimestamp)

            DetailsNoteScreen(navController, note)
        }


    }
}