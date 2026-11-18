package com.coronado.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coronado.centrotareas.data.TareasRepository
import com.coronado.centrotareas.model.CargaUi
import com.coronado.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Paso 3: progreso de 0f a 1f. Solo se expone la versión de lectura.
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    // Paso 4: el Job permite cancelar; "corriendo" evita dos tareas a la vez.
    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    fun tareaLarga() {
        if (_corriendo.value) return // doble inicio: no hace nada
        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                for (i in 1..100) {
                    withContext(Dispatchers.Default) {
                        if (i == 1) Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
                        delay(40) // delay revisa la cancelación
                    }
                    _progreso.value = i / 100f
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                _corriendo.value = false // termine bien o cancelada
            }
        }
    }

    fun cancelar() {
        if (!_corriendo.value) return
        trabajo?.cancel()
        _mensaje.value = "Cancelado" // la barra se queda donde estaba
    }

    // ----- Contar primos (paso 5)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()
    private var primosJob: Job? = null

    fun contar(hasta: Int) {
        if (_primos.value == PrimosUi.Calculando) return
        primosJob = viewModelScope.launch {
            _primos.value = PrimosUi.Calculando
            val inicio = System.currentTimeMillis()
            try {
                val cantidad = repo.contarPrimos(hasta)
                _primos.value = PrimosUi.Listo(cantidad, System.currentTimeMillis() - inicio)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // nunca tragarse la cancelación
            }
        }
    }

    fun cancelarPrimos() {
        primosJob?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    // Una después de otra: ≈ 2 s + 3 s = 5 s
    fun cargarUnaPorUna() {
        if (_carga.value == CargaUi.Cargando) return
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            val perfil = repo.cargarPerfil()
            val pedidos = repo.cargarPedidos(false)
            val ms = System.currentTimeMillis() - inicio
            _carga.value = CargaUi.Exito("Una por una", perfil, pedidos.size, ms)
        }
    }

    // Las dos al mismo tiempo con async: ≈ máximo(2 s, 3 s) = 3 s
    fun cargarALaVez() {
        if (_carga.value == CargaUi.Cargando) return
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            val (perfil, pedidos) = coroutineScope {
                val a = async { repo.cargarPerfil() }
                val b = async { repo.cargarPedidos(false) }
                Pair(a.await(), b.await())
            }
            val ms = System.currentTimeMillis() - inicio
            _carga.value = CargaUi.Exito("A la vez", perfil, pedidos.size, ms)
        }
    }

    // TODO Paso 7: simularError y manejo de errores
}
