package com.coronado.centrotareas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coronado.centrotareas.model.CargaUi
import com.coronado.centrotareas.model.PrimosUi
import com.coronado.centrotareas.viewmodel.TareasViewModel

// Valor por defecto del cálculo de primos. Ajústalo si en tu emulador tarda menos de 2 s o más de 5 s.
private const val N_POR_DEFECTO = "500000"

@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    var toques by rememberSaveable { mutableIntStateOf(0) }
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val primos by vm.primos.collectAsState()
    var texto by rememberSaveable { mutableStateOf(N_POR_DEFECTO) }
    var ultimoN by rememberSaveable { mutableIntStateOf(0) }
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    Scaffold(topBar = { Barra() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Tarjeta("Tarea con progreso") {
                Button(onClick = { toques++ }) { Text("Tócame: $toques") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.tareaLarga() }, enabled = !corriendo) { Text("Iniciar") }
                    OutlinedButton(onClick = { vm.cancelar() }, enabled = corriendo) { Text("Cancelar") }
                }
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
                Text("${(progreso * 100).toInt()} %")
                Text(mensaje)
            }
            Tarjeta("Contar números primos") {
                val n = texto.toIntOrNull()
                val valido = n != null && n >= 2
                val calculando = primos is PrimosUi.Calculando
                OutlinedTextField(
                    value = texto,
                    onValueChange = { nuevo ->
                        // solo dígitos: ignora textos no numéricos
                        if (nuevo.all { it.isDigit() } && nuevo.length <= 9) texto = nuevo
                    },
                    label = { Text("Hasta el número") },
                    singleLine = true,
                    enabled = !calculando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (n != null && n >= 2) {
                                ultimoN = n
                                vm.contar(n)
                            }
                        },
                        enabled = valido && !calculando
                    ) { Text("Contar") }
                    OutlinedButton(onClick = { vm.cancelarPrimos() }, enabled = calculando) { Text("Cancelar") }
                }
                when (val e = primos) {
                    PrimosUi.Inicial -> {}
                    is PrimosUi.Calculando -> {
                        LinearProgressIndicator(
                            progress = { e.progreso },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${(e.progreso * 100).toInt()} %")
                    }
                    is PrimosUi.Listo -> Recuadro(VerdeFondo, VerdeTexto) {
                        Text("Primos hasta $ultimoN: ${e.cantidad}", fontWeight = FontWeight.Bold)
                        Text("Tiempo: ${e.milisegundos} ms")
                    }
                    PrimosUi.Cancelado -> Text("Cancelado")
                    is PrimosUi.Error -> Recuadro(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(e.mensaje)
                    }
                }
            }
            Tarjeta("Dos cargas") {
                val cargando = carga is CargaUi.Cargando
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Switch(checked = simularError, onCheckedChange = { vm.cambiarSimularError(it) })
                    Text("Simular error")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.cargarUnaPorUna() }, enabled = !cargando) { Text("Una por una") }
                    Button(onClick = { vm.cargarALaVez() }, enabled = !cargando) { Text("A la vez") }
                }
                when (val e = carga) {
                    CargaUi.Inicial -> {}
                    CargaUi.Cargando -> CircularProgressIndicator()
                    is CargaUi.Exito -> {
                        Recuadro(VerdeFondo, VerdeTexto) {
                            Text("Modo: ${e.modo}", fontWeight = FontWeight.Bold)
                            Text("Perfil: ${e.perfil}")
                            Text("Pedidos: ${e.pedidos}")
                            Text("Tiempo: ${e.milisegundos} ms")
                        }
                        Aviso("«Una por una» tarda ≈ 5000 ms")
                    }
                    is CargaUi.Error -> {
                        Recuadro(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.onErrorContainer
                        ) { Text(e.mensaje) }
                        OutlinedButton(onClick = { vm.cargarALaVez() }) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Barra() {
    TopAppBar(
        title = { Text("CentroTareas", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun Tarjeta(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                titulo,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            content()
        }
    }
}

@Composable
private fun Recuadro(
    fondo: Color,
    contenido: Color,
    borde: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = fondo,
        contentColor = contenido,
        border = borde?.let { BorderStroke(1.dp, it) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

@Composable
private fun Aviso(texto: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            texto,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private val VerdeFondo = Color(0xFFE3F4E6)
private val VerdeTexto = Color(0xFF1B5E20)

