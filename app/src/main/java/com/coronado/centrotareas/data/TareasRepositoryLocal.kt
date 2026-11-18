package com.coronado.centrotareas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coronado.centrotareas.model.ResultadoHistorial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "centro_tareas_prefs")
private val HISTORIAL_KEY = stringPreferencesKey("historial_resultados")

class TareasRepositoryLocal(private val context: Context? = null) : TareasRepository {

    override val historial: Flow<List<ResultadoHistorial>> =
        context?.dataStore?.data?.map { prefs ->
            val jsonString = prefs[HISTORIAL_KEY] ?: ""
            deserializarHistorial(jsonString)
        } ?: flowOf(emptyList())

    override suspend fun guardarResultado(resultado: ResultadoHistorial) {
        val dataStore = context?.dataStore ?: return
        dataStore.edit { prefs ->
            val jsonString = prefs[HISTORIAL_KEY] ?: ""
            val listaActual = deserializarHistorial(jsonString).toMutableList()
            listaActual.add(0, resultado)
            val listaReciente = listaActual.take(5)
            prefs[HISTORIAL_KEY] = serializarHistorial(listaReciente)
        }
    }

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

    private fun serializarHistorial(lista: List<ResultadoHistorial>): String {
        val jsonArray = JSONArray()
        lista.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("tipo", item.tipo)
                put("titulo", item.titulo)
                put("detalle", item.detalle)
                put("milisegundos", item.milisegundos)
                put("fechaHora", item.fechaHora)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun deserializarHistorial(jsonString: String): List<ResultadoHistorial> {
        if (jsonString.isBlank()) return emptyList()
        val list = mutableListOf<ResultadoHistorial>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ResultadoHistorial(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        tipo = obj.optString("tipo", ""),
                        titulo = obj.optString("titulo", ""),
                        detalle = obj.optString("detalle", ""),
                        milisegundos = obj.optLong("milisegundos", 0L),
                        fechaHora = obj.optString("fechaHora", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
