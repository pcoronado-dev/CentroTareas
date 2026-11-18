package com.coronado.centrotareas.model

import java.util.UUID

data class ResultadoHistorial(
    val id: String = UUID.randomUUID().toString(),
    val tipo: String, // "Primos" o "Cargas"
    val titulo: String,
    val detalle: String,
    val milisegundos: Long,
    val fechaHora: String
)
