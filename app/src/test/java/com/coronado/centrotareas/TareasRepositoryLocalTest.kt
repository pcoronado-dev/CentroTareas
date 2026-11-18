package com.coronado.centrotareas

import com.coronado.centrotareas.data.TareasRepositoryLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TareasRepositoryLocalTest {

    @Test
    fun contarPrimos_calculaCorrectamenteYNotificaProgreso() = runBlocking {
        val repo = TareasRepositoryLocal()
        val progresos = mutableListOf<Float>()
        val resultado = repo.contarPrimos(10) { progreso ->
            progresos.add(progreso)
        }
        // Primos <= 10 son 2, 3, 5, 7 (total: 4)
        assertEquals(4, resultado)
        assertTrue(progresos.isNotEmpty())
        assertEquals(0f, progresos.first(), 0.001f)
        assertEquals(1f, progresos.last(), 0.001f)
    }

    @Test
    fun contarPrimos_hastaMenorQueDos_retornaCero() = runBlocking {
        val repo = TareasRepositoryLocal()
        val resultado = repo.contarPrimos(1)
        assertEquals(0, resultado)
    }
}
