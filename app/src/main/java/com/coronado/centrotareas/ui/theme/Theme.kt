package com.coronado.centrotareas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Azul = Color(0xFF1B6CA8)

private val Esquema = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    background = Color(0xFFFBF8FF),
    surface = Color.White
)

@Composable
fun CentroTareasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}