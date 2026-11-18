package com.coronado.centrotareas.data

import com.coronado.centrotareas.model.ResultadoHistorial
import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int, onProgress: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>
    val historial: Flow<List<ResultadoHistorial>>
    suspend fun guardarResultado(resultado: ResultadoHistorial)
}