package com.coronado.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coronado.centrotareas.data.TareasRepository
import com.coronado.centrotareas.model.CargaUi
import com.coronado.centrotareas.model.ComparacionUi
import com.coronado.centrotareas.model.ItemComparacion
import com.coronado.centrotareas.model.PrimosUi
import com.coronado.centrotareas.model.ResultadoHistorial
import com.coronado.centrotareas.model.SesionComparacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Historial de resultados
    val historial: StateFlow<List<ResultadoHistorial>> = repo.historial.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // ----- Comparación de tiempos
    private val _comparacion = MutableStateFlow<ComparacionUi>(ComparacionUi.Inicial)
    val comparacion: StateFlow<ComparacionUi> = _comparacion.asStateFlow()
    private val _acumuladosComparacion = mutableListOf<SesionComparacion>()

    fun compararCargas() {
        if (_comparacion.value is ComparacionUi.Ejecutando) return
        viewModelScope.launch {
            try {
                _comparacion.value = ComparacionUi.Ejecutando("Ejecutando (1/2): Carga Una por Una...")

                // Medición de Operación 1: Carga Una por Una (secuencial)
                val t1Start = System.nanoTime()
                repo.cargarPerfil()
                repo.cargarPedidos(_simularError.value)
                val t1End = System.nanoTime()
                val tiempo1Ms = (t1End - t1Start) / 1_000_000.0

                _comparacion.value = ComparacionUi.Ejecutando("Ejecutando (2/2): Carga A la Vez...")

                // Medición de Operación 2: Carga A la Vez (concurrente)
                val t2Start = System.nanoTime()
                coroutineScope {
                    val a = async { repo.cargarPerfil() }
                    val b = async { repo.cargarPedidos(_simularError.value) }
                    a.await()
                    b.await()
                }
                val t2End = System.nanoTime()
                val tiempo2Ms = (t2End - t2Start) / 1_000_000.0

                val dif1 = tiempo1Ms - tiempo2Ms
                val dif2 = tiempo2Ms - tiempo1Ms

                val items = listOf(
                    ItemComparacion(
                        nombreOperacion = "Carga Una por Una",
                        tiempoMs = tiempo1Ms,
                        diferenciaMs = dif1,
                        esMasRapido = tiempo1Ms < tiempo2Ms
                    ),
                    ItemComparacion(
                        nombreOperacion = "Carga A la Vez",
                        tiempoMs = tiempo2Ms,
                        diferenciaMs = dif2,
                        esMasRapido = tiempo2Ms < tiempo1Ms
                    )
                )

                val diferenciaAbs = kotlin.math.abs(tiempo1Ms - tiempo2Ms)
                val masRapida = if (tiempo2Ms < tiempo1Ms) "Carga A la Vez" else "Carga Una por Una"
                val resumen = String.format(Locale.getDefault(), "«%s» fue %.2f ms más rápida", masRapida, diferenciaAbs)

                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val sesion = SesionComparacion(
                    titulo = "Comparación: Cargas de datos",
                    fechaHora = fechaHora,
                    items = items,
                    resumen = resumen
                )

                _acumuladosComparacion.add(0, sesion)
                _comparacion.value = ComparacionUi.Completado(
                    sesionActual = sesion,
                    acumulados = _acumuladosComparacion.toList()
                )
            } catch (e: CancellationException) {
                _comparacion.value = ComparacionUi.Inicial
                throw e
            } catch (e: Exception) {
                _comparacion.value = ComparacionUi.Error("Error en comparación: ${e.message}")
            }
        }
    }

    fun compararPrimosVsCarga() {
        if (_comparacion.value is ComparacionUi.Ejecutando) return
        viewModelScope.launch {
            try {
                _comparacion.value = ComparacionUi.Ejecutando("Ejecutando (1/2): Conteo de Primos (N=300,000)...")

                // Medición de Operación 1: Primos
                val t1Start = System.nanoTime()
                repo.contarPrimos(300000)
                val t1End = System.nanoTime()
                val tiempo1Ms = (t1End - t1Start) / 1_000_000.0

                _comparacion.value = ComparacionUi.Ejecutando("Ejecutando (2/2): Carga A la Vez...")

                // Medición de Operación 2: Carga A la Vez
                val t2Start = System.nanoTime()
                coroutineScope {
                    val a = async { repo.cargarPerfil() }
                    val b = async { repo.cargarPedidos(_simularError.value) }
                    a.await()
                    b.await()
                }
                val t2End = System.nanoTime()
                val tiempo2Ms = (t2End - t2Start) / 1_000_000.0

                val dif1 = tiempo1Ms - tiempo2Ms
                val dif2 = tiempo2Ms - tiempo1Ms

                val items = listOf(
                    ItemComparacion(
                        nombreOperacion = "Conteo Primos (300k)",
                        tiempoMs = tiempo1Ms,
                        diferenciaMs = dif1,
                        esMasRapido = tiempo1Ms < tiempo2Ms
                    ),
                    ItemComparacion(
                        nombreOperacion = "Carga A la Vez",
                        tiempoMs = tiempo2Ms,
                        diferenciaMs = dif2,
                        esMasRapido = tiempo2Ms < tiempo1Ms
                    )
                )

                val diferenciaAbs = kotlin.math.abs(tiempo1Ms - tiempo2Ms)
                val masRapida = if (tiempo2Ms < tiempo1Ms) "Carga A la Vez" else "Conteo Primos (300k)"
                val resumen = String.format(Locale.getDefault(), "«%s» fue %.2f ms más rápida", masRapida, diferenciaAbs)

                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val sesion = SesionComparacion(
                    titulo = "Comparación: Primos vs Carga",
                    fechaHora = fechaHora,
                    items = items,
                    resumen = resumen
                )

                _acumuladosComparacion.add(0, sesion)
                _comparacion.value = ComparacionUi.Completado(
                    sesionActual = sesion,
                    acumulados = _acumuladosComparacion.toList()
                )
            } catch (e: CancellationException) {
                _comparacion.value = ComparacionUi.Inicial
                throw e
            } catch (e: Exception) {
                _comparacion.value = ComparacionUi.Error("Error en comparación: ${e.message}")
            }
        }
    }

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
        if (_primos.value is PrimosUi.Calculando) return
        primosJob = viewModelScope.launch {
            _primos.value = PrimosUi.Calculando(0f)
            val inicio = System.currentTimeMillis()
            try {
                if (hasta < 2) {
                    throw IllegalArgumentException("El número debe ser mayor o igual a 2")
                }
                val cantidad = repo.contarPrimos(hasta) { progreso ->
                    _primos.value = PrimosUi.Calculando(progreso)
                }
                val ms = System.currentTimeMillis() - inicio
                _primos.value = PrimosUi.Listo(cantidad, ms)

                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val resultado = ResultadoHistorial(
                    tipo = "Primos",
                    titulo = "Conteo de Primos",
                    detalle = "Primos hasta $hasta: $cantidad",
                    milisegundos = ms,
                    fechaHora = fechaHora
                )
                repo.guardarResultado(resultado)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // nunca tragarse la cancelación
            } catch (e: Exception) {
                _primos.value = PrimosUi.Error(e.message ?: "Error al calcular primos")
            }
        }
    }

    fun cancelarPrimos() {
        primosJob?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    // Paso 7: interruptor para simular que los pedidos fallan
    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    // Una después de otra: ≈ 2 s + 3 s = 5 s
    fun cargarUnaPorUna() {
        if (_carga.value == CargaUi.Cargando) return
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            try {
                val perfil = repo.cargarPerfil()
                val pedidos = repo.cargarPedidos(_simularError.value)
                val ms = System.currentTimeMillis() - inicio
                _carga.value = CargaUi.Exito("Una por una", perfil, pedidos.size, ms)

                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val resultado = ResultadoHistorial(
                    tipo = "Cargas",
                    titulo = "Carga de Datos (Una por una)",
                    detalle = "Perfil: $perfil | Pedidos: ${pedidos.size}",
                    milisegundos = ms,
                    fechaHora = fechaHora
                )
                repo.guardarResultado(resultado)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }

    // Las dos al mismo tiempo con async: ≈ máximo(2 s, 3 s) = 3 s
    fun cargarALaVez() {
        if (_carga.value == CargaUi.Cargando) return
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            try {
                val (perfil, pedidos) = coroutineScope {
                    val a = async { repo.cargarPerfil() }
                    val b = async { repo.cargarPedidos(_simularError.value) }
                    Pair(a.await(), b.await())
                }
                val ms = System.currentTimeMillis() - inicio
                _carga.value = CargaUi.Exito("A la vez", perfil, pedidos.size, ms)

                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val resultado = ResultadoHistorial(
                    tipo = "Cargas",
                    titulo = "Carga de Datos (A la vez)",
                    detalle = "Perfil: $perfil | Pedidos: ${pedidos.size}",
                    milisegundos = ms,
                    fechaHora = fechaHora
                )
                repo.guardarResultado(resultado)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }
}
