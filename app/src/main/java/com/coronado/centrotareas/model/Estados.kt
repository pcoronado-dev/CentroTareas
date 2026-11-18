package com.coronado.centrotareas.model

import java.util.UUID

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    data class Calculando(val progreso: Float = 0f) : PrimosUi
    data class Listo(val cantidad: Int, val milisegundos: Long) : PrimosUi
    data object Cancelado : PrimosUi
    data class Error(val mensaje: String) : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data object Cargando : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}

data class ItemComparacion(
    val nombreOperacion: String,
    val tiempoMs: Double,
    val diferenciaMs: Double,
    val esMasRapido: Boolean = false
)

data class SesionComparacion(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val fechaHora: String,
    val items: List<ItemComparacion>,
    val resumen: String
)

sealed interface ComparacionUi {
    data object Inicial : ComparacionUi
    data class Ejecutando(val pasoActual: String) : ComparacionUi
    data class Completado(
        val sesionActual: SesionComparacion,
        val acumulados: List<SesionComparacion> = emptyList()
    ) : ComparacionUi
    data class Error(val mensaje: String) : ComparacionUi
}
