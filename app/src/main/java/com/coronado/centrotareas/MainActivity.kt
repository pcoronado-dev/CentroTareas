package com.coronado.centrotareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coronado.centrotareas.data.TareasRepositoryLocal
import com.coronado.centrotareas.ui.PantallaTareas
import com.coronado.centrotareas.ui.theme.CentroTareasTheme
import com.coronado.centrotareas.viewmodel.TareasViewModel
import com.coronado.centrotareas.viewmodel.TareasViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CentroTareasTheme {
                val vm: TareasViewModel = viewModel(
                    factory = TareasViewModelFactory(TareasRepositoryLocal(applicationContext))
                )
                PantallaTareas(vm)
            }
        }
    }
}