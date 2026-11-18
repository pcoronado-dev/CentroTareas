PROMPT1: Implementa la funcionalidad de **Primos con progreso** en mi aplicación Android.

Requisitos:
- Mostrar una barra de progreso mientras se realiza el cálculo de números primos.
- Mostrar el porcentaje de avance del cálculo en tiempo real (0% a 100%).
- Actualizar el porcentaje conforme avanza el procesamiento.
- Evitar bloquear la interfaz de usuario (UI); realiza el cálculo en segundo plano utilizando corrutinas.
- Implementar la lógica siguiendo la arquitectura MVVM.
- El ViewModel debe controlar el estado del progreso y el resultado.
- El Repository debe encargarse de la lógica de cálculo de números primos.
- La View debe mostrar la barra de progreso, el porcentaje y el resultado final.
- Mantener el diseño actual de la aplicación y utilizar los componentes que ya existen.
- Manejar correctamente los estados de inicio, progreso, finalización y errores.
- No modificar funcionalidades que no estén relacionadas con esta mejora.

Implementa todo el código necesario y conecta correctamente Repository, ViewModel y View.

PROMPT2: Implementa la funcionalidad de **Historial de resultados** en mi aplicación Android.

Requisitos:
- Utilizar Preferences DataStore para almacenar los resultados de forma persistente.
- Guardar los últimos 5 resultados de las operaciones de números primos y cargas.
- Cada resultado debe guardar la información necesaria para identificar la operación, el resultado obtenido y la fecha/hora.
- Cuando se registre un nuevo resultado, mantener únicamente los 5 resultados más recientes.
- Mostrar el historial automáticamente al abrir la sección correspondiente.
- Permitir visualizar claramente si el resultado corresponde a una operación de primos o cargas.
- Implementar la funcionalidad siguiendo MVVM.
- El Repository debe gestionar el almacenamiento y recuperación mediante DataStore.
- El ViewModel debe gestionar los datos del historial y su estado.
- La View debe mostrar el historial mediante una lista visual clara.
- Los datos deben mantenerse aunque se cierre la aplicación.
- No modificar funcionalidades existentes ni eliminar datos de otras funcionalidades.

Implementa el código completo necesario y conecta Repository, ViewModel y View.

PROMPT3: Implementa la funcionalidad de **Comparación de tiempos** en mi aplicación Android.

Requisitos:
- Crear una sección que permita comparar los tiempos de ejecución de los algoritmos u operaciones disponibles.
- Ejecutar las operaciones de comparación de manera secuencial, es decir, una por una, nunca simultáneamente.
- Medir el tiempo de ejecución de cada operación utilizando una herramienta de medición precisa, como System.nanoTime().
- Acumular los tiempos de las ejecuciones realizadas para poder compararlos.
- Mostrar los resultados en una tabla que incluya:
  - Nombre de la operación o algoritmo.
  - Tiempo de ejecución.
  - Diferencia de tiempo respecto a la otra operación.
- Actualizar la tabla cuando se realicen nuevas comparaciones.
- Implementar la lógica en el ViewModel y la interfaz en la View.
- Mantener la arquitectura MVVM y respetar la estructura actual del proyecto.
- Mostrar los tiempos en una unidad legible, como milisegundos.
- Evitar bloquear la interfaz durante las ejecuciones.
- No modificar funcionalidades ajenas a esta mejora.

Implementa el código completo, conectando correctamente ViewModel y View, y asegúrate de que la comparación funcione correctamente.

## Para pensar
**1. ¿Por qué no se veía «Trabajando...» en el Paso 1?**
Porque la espera bloqueante ocupaba el hilo principal, que es el que dibuja. Compose solo redibuja cuando
ese hilo queda libre; el mensaje pasó de «Trabajando...» a «Terminó» antes de que se pintara un solo cuadro nuevo.

**2. ¿Qué diferencia hay entre la espera bloqueante de `Thread` y `delay`?**
La espera bloqueante de `Thread` retiene el hilo y no puede hacer nada más hasta que termina la espera; si es el hilo principal, la
pantalla se congela. `delay` es una función `suspend`: suspende la corrutina y libera el hilo para otros trabajos, y
además es cancelable.

**3. ¿Por qué `Dispatchers.Default` para los primos y `Dispatchers.IO` para la red?**
`Default` tiene tantos hilos como núcleos de CPU y sirve para cálculo intensivo, donde más hilos no ayudan. `IO`
tiene un pool más grande para operaciones que pasan casi todo el tiempo esperando (red, disco), así muchas pueden
esperar a la vez sin agotar los hilos de cálculo.

**4. ¿Qué pasaría con la corrutina si el usuario cierra la pantalla? ¿Qué papel cumple `viewModelScope`?**
Al cerrar la pantalla de forma definitiva se destruye el ViewModel y `viewModelScope` cancela todas sus corrutinas,
así no quedan trabajos huérfanos ni fugas de memoria. Al rotar, el ViewModel sobrevive y la corrutina continúa.


<img width="702" height="1600" alt="WhatsApp Image 2026-11-18 at 4 11 29 AM (2)" src="https://github.com/user-attachments/assets/72249144-3f4e-491f-a448-b7d9d4bd6c92" />
<img width="702" height="1600" alt="WhatsApp Image 2026-11-18 at 4 11 29 AM" src="https://github.com/user-attachments/assets/00378266-1b37-4846-9364-b50ef190a980" />
<img width="702" height="1600" alt="WhatsApp Image 2026-11-18 at 4 11 29 AM (1)" src="https://github.com/user-attachments/assets/9ecee3d3-4e8e-4824-b663-8e8232298102" />



**5. ¿Por qué `async` tarda 3 s y no 5 s? ¿Qué pasa si una de las dos cargas falla?**
Porque las dos llamadas corren al mismo tiempo y el total es el máximo de ambas (3 s), no la suma (2 s + 3 s).
Si una falla, `coroutineScope` cancela a la otra y relanza la excepción; el `try/catch` la atrapa y la pantalla
muestra el estado de error con el botón Reintentar.
