package com.coronado.centrotareas

import com.coronado.centrotareas.data.TareasRepository
import com.coronado.centrotareas.model.PrimosUi
import com.coronado.centrotareas.model.ResultadoHistorial
import com.coronado.centrotareas.viewmodel.TareasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTareasRepository : TareasRepository {
    var ultimoHasta: Int = 0
    var simuladoResultado: Int = 0

    private val _historial = MutableStateFlow<List<ResultadoHistorial>>(emptyList())
    override val historial: StateFlow<List<ResultadoHistorial>> = _historial.asStateFlow()

    override suspend fun guardarResultado(resultado: ResultadoHistorial) {
        val lista = _historial.value.toMutableList()
        lista.add(0, resultado)
        _historial.value = lista.take(5)
    }

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
    fun contar_actualizaEstadosCorrectamenteYGuardaHistorial() = runBlocking {
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

        // Verificar que se haya guardado en el historial
        assertEquals(1, vm.historial.value.size)
        assertEquals("Primos", vm.historial.value.first().tipo)
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

    @Test
    fun historial_mantieneMaximoCincoResultados() = runBlocking {
        val repo = FakeTareasRepository()
        repo.simuladoResultado = 10
        val vm = TareasViewModel(repo)

        // Ejecutamos 7 operaciones de conteo
        for (i in 1..7) {
            vm.contar(10 + i)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // El historial solo debe conservar los 5 más recientes
        assertEquals(5, vm.historial.value.size)
    }
}
