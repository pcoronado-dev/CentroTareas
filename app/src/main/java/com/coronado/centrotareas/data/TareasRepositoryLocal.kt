package com.coronado.centrotareas.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Cálculo pesado: corre en Dispatchers.Default y se puede cancelar a mitad.
    override suspend fun contarPrimos(hasta: Int, onProgress: (Float) -> Unit): Int = withContext(Dispatchers.Default) {
        if (hasta < 2) return@withContext 0
        var cantidad = 0
        val total = hasta - 1
        val step = maxOf(1, total / 200)

        onProgress(0f)
        for (n in 2..hasta) {
            ensureActive() // permite cancelar dentro del ciclo
            if (esPrimo(n)) cantidad++

            val processed = n - 1
            if (processed % step == 0 || n == hasta) {
                val progreso = processed.toFloat() / total
                onProgress(progreso)
            }
        }
        cantidad
    }

    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        while (d.toLong() * d <= n) { // evita desbordes sin calcular la raíz
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
