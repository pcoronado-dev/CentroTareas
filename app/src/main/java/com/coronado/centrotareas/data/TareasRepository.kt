package com.coronado.centrotareas.data

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int, onProgress: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>
}