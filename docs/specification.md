# Especificación del producto — Android Architecture Lab

**Documento:** `docs/specification.md`  
**Estado:** Borrador consolidado para revisión  
**Versión:** 1.0  
**Idioma:** Español

## 1. Propósito

Definir los requisitos funcionales y no funcionales de una aplicación Android de gestión de tareas que sirva como proyecto de referencia para explorar, implementar y evaluar decisiones de arquitectura de software.

La especificación establece qué debe hacer el sistema y qué restricciones debe respetar, sin definir todavía los detalles técnicos de implementación.

## 2. Objetivos

- Construir una aplicación Android funcional para la gestión personal de tareas.
- Aplicar principios de Clean Architecture y separación de responsabilidades.
- Mantener el dominio independiente de la interfaz de usuario y de los mecanismos de persistencia.
- Incorporar pruebas automatizadas y validación continua desde las primeras etapas.
- Documentar las decisiones técnicas relevantes y sus fundamentos.
- Utilizar el proyecto como referencia práctica de desarrollo de software mediante SDD (Specification-Driven Development).

## 3. Alcance

### 3.1. Incluido en el MVP

- Crear, consultar, editar y eliminar tareas.
- Marcar tareas como completadas y volver a dejarlas pendientes.
- Asignar prioridades.
- Establecer fechas y horas de vencimiento opcionales.
- Identificar tareas vencidas.
- Filtrar tareas por fecha de vencimiento y prioridad.
- Ordenar tareas por vencimiento y prioridad.
- Guardar las tareas localmente para conservarlas después de cerrar o reiniciar la aplicación.
- Ejecutar pruebas automatizadas y verificaciones de integración continua mediante GitHub Actions.

### 3.2. Fuera del alcance inicial

- Autenticación y gestión de usuarios.
- Backend remoto y sincronización entre dispositivos.
- Dependencia de Internet para el funcionamiento normal.
- Notificaciones y recordatorios.
- Publicación en Google Play.
- Funcionalidades colaborativas o compartición de tareas.

## 4. Modelo conceptual

### 4.1. Tarea (`Task`)

Cada tarea deberá contener los siguientes atributos:

| Atributo                    | Descripción                                         |
|-----------------------------|-----------------------------------------------------|
| Identificador               | Valor único que permite identificar la tarea.       |
| Título                      | Texto obligatorio que describe la tarea.            |
| Descripción                 | Texto opcional con información adicional.           |
| Prioridad                   | Nivel de importancia: baja, media o alta.           |
| Fecha y hora de vencimiento | Valor opcional que determina cuándo vence la tarea. |
| Estado                      | Pendiente o completada.                             |

### 4.2. Valores de prioridad

La prioridad admite exclusivamente estos valores:

- `LOW`: baja.
- `MEDIUM`: media.
- `HIGH`: alta.

Toda tarea nueva tendrá prioridad `MEDIUM` si el usuario no selecciona otra.

### 4.3. Estados

- **Pendiente:** la tarea todavía no fue completada.
- **Completada:** el usuario indicó que finalizó la tarea.

Una tarea completada podrá volver al estado pendiente.

## 5. Requisitos funcionales

### RF-01 — Crear tareas

El sistema deberá permitir crear una tarea indicando un título obligatorio y, opcionalmente, una descripción, una prioridad y una fecha y hora de vencimiento.

La prioridad predeterminada será `MEDIUM`.

El sistema deberá validar el título antes de guardar la tarea.

### RF-02 — Validar y normalizar el título

El título deberá cumplir las siguientes reglas:

1. Eliminar los espacios en blanco al principio y al final antes de validar y guardar.
2. Rechazar títulos vacíos o compuestos únicamente por espacios en blanco.
3. Permitir espacios en blanco dentro del título.
4. Admitir un máximo de 36 puntos de código Unicode.
5. Aplicar las mismas reglas al crear y editar una tarea.

La validación deberá formar parte de las reglas del dominio. La interfaz podrá realizar validaciones adicionales para ofrecer una respuesta inmediata al usuario.

### RF-03 — Consultar y listar tareas

El sistema deberá mostrar las tareas guardadas y permitir consultar sus atributos, estado y vencimiento.

La lista deberá respetar los filtros y el criterio de ordenamiento seleccionados.

### RF-04 — Editar tareas

El sistema deberá permitir modificar el título, la descripción, la prioridad y la fecha y hora de vencimiento de una tarea existente.

También deberá permitir quitar el vencimiento asignado.

Las reglas de validación del título serán idénticas a las aplicadas durante la creación.

### RF-05 — Completar y reabrir tareas

El sistema deberá permitir cambiar el estado de una tarea entre pendiente y completada.

Una tarea completada no deberá considerarse vencida.

Al reabrir una tarea completada cuyo vencimiento sea anterior o igual al instante actual, se eliminará su fecha y hora de vencimiento. La tarea volverá al estado pendiente y quedará sin vencimiento. Si el vencimiento es posterior al instante actual, se conservará al reabrir la tarea.

### RF-06 — Eliminar tareas

El sistema deberá permitir eliminar una tarea guardada.

La tarea eliminada dejará de aparecer en las consultas y listas de tareas.

### RF-07 — Filtrar por vencimiento

El sistema deberá ofrecer los siguientes filtros de fecha:

| Filtro           | Comportamiento                                                                                                                                                                   |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Todas            | No restringe por fecha de vencimiento.                                                                                                                                           |
| Vencidas         | Incluye tareas pendientes cuyo vencimiento sea anterior o igual al momento actual.                                                                                               |
| Vencen hoy       | Incluye tareas pendientes cuyo vencimiento se encuentre dentro del día calendario actual y sea estrictamente posterior al instante actual.                                       |
| Próximas         | Incluye tareas de cualquier estado cuyo vencimiento sea mayor o igual al inicio del día calendario siguiente.                                                                    |
| Sin vencimiento  | Incluye tareas que no tienen fecha y hora de vencimiento.                                                                                                                        |
| Fecha específica | Incluye tareas de cualquier estado cuyo vencimiento sea mayor o igual al inicio del día calendario seleccionado y estrictamente anterior al inicio del día calendario siguiente. |

Los filtros temporales utilizarán una referencia temporal consistente para determinar el instante actual, la fecha local y los límites del día calendario. La fuente de tiempo y la zona horaria definitivas se establecerán en el diseño lógico. La implementación deberá aplicar esos criterios de forma consistente en todos los filtros temporales.

### RF-08 — Filtrar por prioridad

El sistema deberá permitir filtrar tareas por prioridad:

- Baja.
- Media.
- Alta.

### RF-09 — Combinar filtros

El sistema deberá permitir combinar un filtro de vencimiento con un filtro de prioridad.

Una tarea solo aparecerá en los resultados cuando cumpla simultáneamente todas las condiciones seleccionadas.

Los filtros de vencimiento y prioridad pueden aplicarse de forma independiente o combinada. Cuando se aplica únicamente un filtro de prioridad, deben incluirse todas las tareas que cumplan esa prioridad, tengan o no vencimiento. Cuando se combinan filtros, las tareas deben cumplir todas las condiciones seleccionadas.

### RF-10 — Ordenar tareas

El sistema deberá permitir ordenar las tareas mediante los siguientes criterios:

- Fecha y hora de vencimiento ascendente.
- Fecha y hora de vencimiento descendente.
- Prioridad ascendente: baja, media y alta.
- Prioridad descendente: alta, media y baja.

El orden predeterminado será por fecha y hora de vencimiento ascendente.

Las tareas sin vencimiento aparecerán siempre al final, tanto en orden ascendente como descendente.

Entre las tareas sin vencimiento, se aplicarán los siguientes criterios de ordenamiento: prioridad descendente (alta, media y baja), título en orden alfabético ascendente según la política de comparación definida en este requisito y, si los títulos son equivalentes, identificador de la tarea en orden ascendente.

Cuando varias tareas tengan el mismo valor de vencimiento, se aplicarán estos criterios secundarios:

1. Prioridad descendente: alta, media y baja.
2. Título en orden alfabético ascendente, sin distinguir entre mayúsculas y minúsculas.
3. Identificador de la tarea como último criterio técnico de desempate.

Cuando se ordene por prioridad, las tareas con igual prioridad se ordenarán por título en orden alfabético ascendente, aplicando la política de comparación definida en este requisito. Si los títulos resultan equivalentes según esa política, se utilizará el identificador de la tarea en orden ascendente como criterio final de desempate.

Para las comparaciones alfabéticas, los títulos se normalizarán a Unicode NFC y se compararán sin distinguir entre mayúsculas y minúsculas mediante una transformación independiente de la configuración regional del dispositivo. Se conservarán los acentos y los caracteres especiales: por ejemplo, `ñ` se considerará diferente de `n`. Estas reglas se aplicarán de manera consistente en todos los criterios de ordenamiento que utilicen el título.

### RF-11 — Persistir los datos localmente

El sistema deberá guardar las tareas en almacenamiento local y conservarlas después de cerrar o reiniciar la aplicación.

Las operaciones de creación, edición, cambio de estado y eliminación deberán reflejarse en los datos persistidos.

Los cambios realizados al reabrir una tarea, incluida la eliminación de su fecha y hora de vencimiento cuando corresponda, deberán persistirse y mantenerse después de cerrar y volver a iniciar la aplicación.

### RF-12 — Identificar tareas vencidas

Una tarea se considerará vencida cuando se cumplan todas estas condiciones:

1. Su estado sea pendiente.
2. Tenga una fecha y hora de vencimiento.
3. Su vencimiento sea anterior o igual al instante actual.

Una tarea cuyo vencimiento coincida exactamente con el instante actual se considerará vencida.

Las tareas completadas no se considerarán vencidas, independientemente de su fecha y hora de vencimiento.

## 6. Reglas de negocio

### RN-01 — Normalización del título

El título se normalizará eliminando los espacios en blanco iniciales y finales, sin modificar los espacios en blanco internos. La versión normalizada será la que se valide y almacene.

### RN-02 — Límite del título

El máximo de 36 caracteres se medirá mediante puntos de código Unicode, no mediante unidades de código UTF-16.

### RN-03 — Prioridad predeterminada

Toda tarea creada sin una prioridad explícita tendrá prioridad media.

### RN-04 — Vencimiento opcional

Una tarea podrá existir sin fecha y hora de vencimiento. En ese caso, no podrá clasificarse como vencida y deberá aparecer en el filtro «Sin vencimiento».

### RN-05 — Fecha y hora predeterminadas

La fecha y hora de vencimiento serán opcionales al crear o editar una tarea.

Si el usuario indica una fecha, pero no especifica una hora, el sistema asignará automáticamente las `00:00` (medianoche) de esa fecha.

La hora ingresada por el usuario se interpretará y mostrará en formato de 24 horas, de `00:00` a `23:59`.

El valor predeterminado `00:00` será una hora real de vencimiento, no un indicador de que el usuario omitió la hora.

Por lo tanto, una tarea pendiente con vencimiento a las `00:00` podrá considerarse vencida inmediatamente después de ese instante, según la regla RN-06.

### RN-06 — Cálculo del vencimiento

El vencimiento se determinará comparando la fecha y hora de vencimiento con el instante actual.

Una tarea pendiente con fecha y hora de vencimiento será considerada vencida cuando su vencimiento sea anterior o igual al instante actual.

La comparación deberá utilizar una referencia temporal controlable para que las pruebas automatizadas no dependan del reloj real.

Los filtros temporales y el cálculo del vencimiento deberán utilizar una referencia temporal consistente para determinar el instante actual, la fecha local y los límites del día calendario.

La zona horaria y el mecanismo definitivo para obtener la hora actual se formalizarán en el diseño lógico.

### RN-07 — Ordenamiento determinista

El ordenamiento deberá producir resultados deterministas para un mismo conjunto de tareas, filtros y criterio de ordenamiento.

Cuando dos o más tareas coincidan en los criterios funcionales de ordenamiento, se aplicarán los criterios secundarios definidos en RF-10. Si las tareas continúan siendo equivalentes según esos criterios, el identificador de la tarea en orden ascendente resolverá el desempate final.

### RN-08 — Composición de filtros

Cuando se aplique más de un filtro, todos deberán cumplirse para que una tarea forme parte del resultado.

### RN-09 — Independencia de la interfaz

Las reglas de negocio no deberán depender de componentes visuales ni de la interacción directa con Android UI.

## 7. Requisitos no funcionales

### RNF-01 — Arquitectura

El proyecto deberá aplicar los principios de Clean Architecture mediante una separación lógica entre:

- **Presentation:** interfaz de usuario y gestión de interacciones.
- **Domain:** reglas de negocio, entidades y casos de uso.
- **Data:** persistencia y mecanismos de acceso a los datos.

La separación será inicialmente lógica. La decisión de convertir estas capas en módulos Gradle independientes se evaluará por separado.

### RNF-02 — Independencia del dominio

El dominio no deberá depender de la interfaz de usuario, de Android UI ni de una tecnología concreta de persistencia.

### RNF-03 — Testabilidad

Las reglas de negocio deberán poder probarse mediante pruebas unitarias sin requerir un dispositivo Android ni un emulador.

### RNF-04 — Determinismo temporal

Los casos de uso y las reglas que dependan del momento actual deberán admitir una referencia temporal controlable para facilitar pruebas reproducibles.

### RNF-05 — Funcionamiento offline

Las funcionalidades incluidas en el MVP deberán funcionar sin conexión a Internet. Los datos de las tareas deberán almacenarse localmente y mantenerse disponibles después de cerrar y volver a abrir la aplicación.

### RNF-06 — Mantenibilidad

El código deberá mantener responsabilidades claras, dependencias controladas y una estructura que facilite la evolución del proyecto. La organización del código deberá respetar las responsabilidades definidas para las capas de Presentation, Domain y Data, de acuerdo con la arquitectura lógica establecida.

### RNF-07 — Integración continua

GitHub Actions deberá incorporarse desde el inicio de la implementación funcional.

Como mínimo, el flujo de integración continua deberá ejecutarse ante eventos `push` y `pull_request`, compilar el proyecto y ejecutar las pruebas automatizadas configuradas.

### RNF-08 — Calidad del código

Se incorporarán herramientas de análisis estático y verificaciones adicionales de calidad. La selección de herramientas y sus umbrales se definirá durante el diseño técnico.

### RNF-09 — Reproducibilidad

Una persona que clone el repositorio deberá disponer de instrucciones para configurar el entorno, compilar el proyecto y ejecutar sus pruebas mediante herramientas y versiones documentadas.

## 8. Restricciones técnicas conocidas

- Plataforma: Android.
- Lenguaje de programación: Kotlin.
- Interfaz: Jetpack Compose.
- Arquitectura: Clean Architecture.
- Automatización: GitHub Actions.
- Persistencia: local.
- Conectividad: no requerida para las funciones del MVP.
- Documentación: español.
- Identificadores de código, paquetes, clases y archivos: podrán utilizar inglés.

Las tecnologías específicas de persistencia, inyección de dependencias y análisis estático todavía no están seleccionadas.

## 9. Criterios de aceptación

| ID    | Criterio                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|-------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CA-01 | Se puede crear una tarea con un título válido, indicando opcionalmente una descripción, una prioridad y una fecha y hora de vencimiento.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| CA-02 | Al crear o editar una tarea, se rechaza un título vacío o compuesto solo por espacios en blanco.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| CA-03 | Al crear o editar una tarea, se eliminan los espacios en blanco al principio y al final del título antes de guardarlo. Los espacios en blanco internos del título se conservan.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| CA-04 | Al crear o editar una tarea, se acepta un título de hasta 36 puntos de código Unicode.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| CA-05 | Al crear o editar una tarea, se rechaza un título que supere los 36 puntos de código Unicode.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| CA-06 | La prioridad predeterminada es media.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| CA-07 | Se puede crear una tarea sin vencimiento.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| CA-08 | Si se indica fecha sin hora, el vencimiento se establece a las `00:00`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| CA-09 | Se pueden ingresar y consultar horas en formato de 24 horas.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| CA-10 | Se puede editar el título, la descripción, la prioridad y la fecha y hora de vencimiento de una tarea, incluida la posibilidad de quitar un vencimiento existente.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| CA-11 | Se puede marcar una tarea como completada y volver a dejarla pendiente. Al reabrir una tarea completada cuyo vencimiento sea anterior o igual al instante actual, se elimina su fecha y hora de vencimiento. Si el vencimiento es posterior al instante actual, se conserva. En ambos casos, la tarea vuelve al estado pendiente.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| CA-12 | Se puede eliminar una tarea guardada y, una vez eliminada, deja de aparecer en las consultas y listas de tareas.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| CA-13 | Una tarea pendiente con vencimiento anterior o igual al instante actual se considera vencida. Si el vencimiento coincide exactamente con el instante actual, la tarea se considera vencida.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| CA-14 | Una tarea completada no se considera vencida.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| CA-15 | Una tarea sin vencimiento no se considera vencida.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| CA-16 | Se pueden consultar todas las tareas sin filtro de fecha y filtrar por vencidas, vencen hoy, próximas, sin vencimiento o fecha específica. El filtro «Vencidas» incluye únicamente tareas pendientes cuyo vencimiento sea anterior o igual al instante actual; «Vencen hoy» incluye únicamente tareas pendientes cuyo vencimiento sea estrictamente posterior al instante actual y anterior al inicio del día calendario siguiente; «Próximas» incluye tareas de cualquier estado cuyo vencimiento sea mayor o igual al inicio del día calendario siguiente; «Sin vencimiento» incluye tareas que no tienen vencimiento; y «Fecha específica» incluye tareas de cualquier estado cuyo vencimiento sea mayor o igual al inicio del día calendario seleccionado y estrictamente anterior al inicio del día calendario siguiente. |
| CA-17 | Se pueden filtrar tareas por prioridad. Cuando se aplica únicamente un filtro de prioridad, los resultados incluyen todas las tareas que cumplen la prioridad seleccionada, tengan o no fecha y hora de vencimiento.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| CA-18 | Al combinar un filtro de vencimiento con uno de prioridad, una tarea aparece en los resultados únicamente si cumple simultáneamente ambas condiciones (AND lógico).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| CA-19 | Se puede ordenar por vencimiento ascendente y descendente. El ordenamiento predeterminado es por vencimiento ascendente.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| CA-20 | Las tareas sin vencimiento aparecen siempre al final al ordenar por fecha.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| CA-21 | Se pueden ordenar las tareas por prioridad ascendente y descendente. Cuando dos o más tareas tienen la misma prioridad, se ordenan por título según la política de comparación alfabética definida en la especificación y, si los títulos son equivalentes, por ID ascendente.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| CA-22 | Cuando dos o más tareas tienen el mismo vencimiento, se ordenan por prioridad descendente (alta, media y baja), después por título según la política de comparación alfabética definida en la especificación y, si los títulos son equivalentes, por ID ascendente. Entre las tareas sin vencimiento, se aplican esos mismos criterios de ordenamiento.                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| CA-23 | Ante el mismo conjunto de tareas y los mismos filtros y parámetros de ordenamiento, la aplicación produce siempre el mismo orden, independientemente del orden en que las tareas se hayan recibido o almacenado.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| CA-24 | Las tareas se guardan en almacenamiento local y se conservan después de cerrar o reiniciar la aplicación. Las operaciones de creación, edición, cambio de estado y eliminación se reflejan en los datos persistidos. Cuando se reabre una tarea cuyo vencimiento es anterior o igual al instante actual, la eliminación de su fecha y hora de vencimiento también se persiste y se mantiene después de cerrar y volver a iniciar la aplicación.                                                                                                                                                                                                                                                                                                                                                                                |
| CA-25 | Las reglas de dominio pueden probarse mediante pruebas unitarias sin requerir un dispositivo Android ni un emulador.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| CA-26 | Los casos de uso y las reglas de dominio que dependan del momento actual admiten una referencia temporal controlable. Las pruebas automatizadas pueden utilizarla para verificar el cálculo del vencimiento y los filtros temporales, incluidos los límites del día calendario, de forma reproducible. En particular, se verifica que una tarea pendiente cuyo vencimiento coincide exactamente con el instante actual se considere vencida y aparezca en el filtro «Vencidas», mientras que una tarea completada no se considere vencida. También se verifica que, al reabrir una tarea completada, se elimine el vencimiento si es anterior o igual al instante actual y se conserve si es posterior.                                                                                                                        |
| CA-27 | Desde el inicio de la implementación funcional, GitHub Actions ejecuta un flujo ante eventos `push` y `pull_request` que compila el proyecto y ejecuta las pruebas automatizadas configuradas.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| CA-28 | El repositorio incluye instrucciones para configurar el entorno, compilar el proyecto y ejecutar las pruebas, con las herramientas y versiones necesarias documentadas.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| CA-29 | La aplicación puede utilizarse sin conexión a Internet para las funcionalidades del MVP.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| CA-30 | Se pueden consultar las tareas guardadas y visualizar su título, descripción, prioridad, fecha y hora de vencimiento (si existe) y estado. La lista respeta los filtros y el ordenamiento activos.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| CA-31 | La estructura del proyecto separa las responsabilidades en las capas Presentation, Domain y Data. Cada capa tiene responsabilidades definidas, las dependencias entre ellas respetan la arquitectura establecida en el diseño lógico y la organización del código permite identificar dónde implementar cambios según la responsabilidad afectada.                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| CA-32 | La capa Domain no depende de APIs de Android, componentes de Presentation ni implementaciones concretas de persistencia.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| CA-33 | El proyecto configura las herramientas de análisis estático y las verificaciones adicionales de calidad, junto con sus umbrales correspondientes, según lo definido en docs/implementation_plan.md. El flujo de integración continua ejecuta esas verificaciones.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |

## 10. Estrategia de validación

La validación del producto deberá incluir:

- Pruebas unitarias para la normalización y validación de títulos.
- Pruebas unitarias para prioridades, estados y cálculo del vencimiento.
- Pruebas de límites temporales que verifiquen vencimientos anteriores, iguales y posteriores al instante actual, así como el comportamiento de tareas completadas y sin vencimiento.
- Pruebas de filtros individuales y combinados.
- Pruebas de ordenamiento, empates y tareas sin vencimiento.
- Pruebas de persistencia y recuperación de datos.
- Compilación y ejecución automatizada de pruebas en GitHub Actions.

Las pruebas que dependan de la fecha y hora deberán utilizar una referencia temporal fija o controlable y no depender del reloj real del entorno de ejecución.

## 11. Trazabilidad y decisiones técnicas

Los requisitos funcionales y no funcionales deberán vincularse con los casos de uso, las pruebas y, cuando corresponda, los registros de decisiones arquitectónicas (ADR).

Las decisiones técnicas significativas deberán documentarse indicando el contexto, las alternativas evaluadas, la decisión adoptada y sus consecuencias.

## 12. Decisiones pendientes

Las siguientes decisiones pendientes deberán definirse en la etapa correspondiente, antes de implementar los componentes que dependan de ellas. Algunas precisan aspectos técnicos de la solución y otras determinan detalles necesarios para aplicar de forma consistente los requisitos funcionales ya establecidos.
1. Versión mínima de Android.
2. Tecnología de persistencia local.
3. Biblioteca de inyección de dependencias, si se utiliza.
4. Modularización Gradle y límites entre módulos.
5. Herramientas de análisis estático y umbrales de calidad.
6. Política definitiva de zona horaria y obtención del instante actual.

## 13. Próximos entregables

Una vez revisada y aprobada esta especificación, los siguientes documentos serán:

- `docs/requirements_traceability.md`: matriz de trazabilidad de requisitos, casos de uso y pruebas.
- `docs/logic_design.md`: diseño lógico, flujos, casos de uso y reglas de negocio.
- `docs/implementation_plan.md`: plan de implementación específico para Kotlin y Android.
- `docs/adr/`: registros de decisiones arquitectónicas relevantes.