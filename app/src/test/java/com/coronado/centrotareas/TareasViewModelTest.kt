package com.coronado.centrotareas

import com.coronado.centrotareas.data.TareasRepository
import com.coronado.centrotareas.model.PrimosUi
import com.coronado.centrotareas.viewmodel.TareasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTareasRepository : TareasRepository {
    var ultimoHasta: Int = 0
    var simuladoResultado: Int = 0

    override suspend fun contarPrimos(hasta: Int, onProgress: (Float) -> Unit): Int {
        ultimoHasta = hasta
        onProgress(0f)
        onProgress(0.5f)
        onProgress(1.0f)
        return simuladoResultado
    }

    override suspend fun cargarPerfil(): String = "Perfil Test"
    override suspend fun cargarPedidos(simularError: Boolean): List<String> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class TareasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun contar_actualizaEstadosCorrectamente() = runBlocking {
        val repo = FakeTareasRepository()
        repo.simuladoResultado = 25
        val vm = TareasViewModel(repo)

        assertEquals(PrimosUi.Inicial, vm.primos.value)

        vm.contar(100)
        testDispatcher.scheduler.advanceUntilIdle()

        val estadoFinal = vm.primos.value
        assertTrue(estadoFinal is PrimosUi.Listo)
        val listo = estadoFinal as PrimosUi.Listo
        assertEquals(25, listo.cantidad)
    }

    @Test
    fun contar_numeroInvalido_produceError() = runBlocking {
        val repo = FakeTareasRepository()
        val vm = TareasViewModel(repo)

        vm.contar(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val estadoFinal = vm.primos.value
        assertTrue(estadoFinal is PrimosUi.Error)
        val error = estadoFinal as PrimosUi.Error
        assertEquals("El número debe ser mayor o igual a 2", error.mensaje)
    }
}
