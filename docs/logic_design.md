# Diseño lógico y arquitectura — Android Architecture Lab

**Documento:** `docs/logic_design.md`  
**Estado:** Borrador consolidado  
**Versión:** 1.0  
**Idioma:** Español  
**Documentos de referencia:** `docs/specification.md` (v1.0), `docs/requirements_traceability.md` (v1.0)

---

## 1. Propósito y Visión General

Este documento define la arquitectura lógica, las responsabilidades de los componentes, las interfaces, las reglas de negocio y los casos de uso para la aplicación Android de gestión de tareas.

El diseño sigue los principios de **Clean Architecture**, dividiendo el sistema en tres capas lógicas bien delimitadas:

```
+-------------------------------------------------------------------+
|                        PRESENTATION LAYER                         |
|   (ViewModels, UI States, UI Events, Jetpack Compose Screens)    |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                           DOMAIN LAYER                            |
|   (Use Cases, Entities, Value Objects, Repository Interfaces)     |
+-------------------------------------------------------------------+
                                  ^
                                  |
+-------------------------------------------------------------------+
|                            DATA LAYER                             |
|   (Repository Impl, Local Data Sources, Mappers, DB Entities)     |
+-------------------------------------------------------------------+
```

### Reglas de Dependencia
1. La capa **Domain** es pura en Kotlin y no tiene dependencias de Android Framework, bibliotecas de UI ni tecnologías concretas de persistencia.
2. La capa **Presentation** depende únicamente de la capa **Domain**.
3. La capa **Data** implementa los contratos/interfaces definidos en la capa **Domain**.

---

## 2. Capa de Dominio (`Domain`)

### 2.1. Objetos de Valor (*Value Objects*)

#### `TaskId`
Representa el identificador único e inmutable de una tarea.
- Tipo interno: `String` (UUID v4 o equivalente alfanumérico).

#### `TaskTitle`
Representa el título normalizado y validado de una tarea (RF-02, RN-01, RN-02).
- **Reglas de Construcción y Normalización:**
  1. Se eliminan los espacios en blanco iniciales y finales (`trim`).
  2. Si el título resultante está vacío, se lanza un error de dominio `InvalidTaskTitleException.EmptyTitle`.
  3. Se calcula la longitud en puntos de código Unicode (*Unicode code points*, no unidades UTF-16). Si supera los 36 puntos de código, se lanza `InvalidTaskTitleException.ExceedsMaxLength`.
  4. Los espacios internos se conservan sin alteración.

#### `Priority` (Enum)
Niveles de importancia de una tarea (RF-08, RN-03).
- Valóres: `LOW`, `MEDIUM`, `HIGH`.
- Valor por defecto al crear: `MEDIUM`.

#### `TaskStatus` (Enum)
Estado funcional de una tarea (RF-05, RF-12).
- Valores: `PENDING`, `COMPLETED`.

#### `TaskDueDate`
Representa la fecha y hora de vencimiento opcional de una tarea (RN-04, RN-05).
- Tipo interno: `Instant` o `LocalDateTime` con referencia temporal clara.
- **Regla de asignación de hora por defecto (RN-05):** Si el usuario especifica una fecha sin hora, se asignan automáticamente las `00:00` (medianoche) de esa fecha en la zona horaria del dispositivo. La medianoche es una hora real y válida.

---

### 2.2. Entidad Principal: `Task`

La entidad `Task` encapsula el estado y las invariantes de negocio de una tarea.

```kotlin
data class Task(
    val id: TaskId,
    val title: TaskTitle,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: TaskDueDate? = null,
    val status: TaskStatus = TaskStatus.PENDING
) {
    /**
     * Una tarea está vencida si está PENDIENTE, tiene fecha de vencimiento
     * y su vencimiento es menor o igual al instante actual (now).
     * Las tareas completadas o sin vencimiento NUNCA se consideran vencidas (RF-12, RN-06).
     */
    fun isOverdue(now: Instant): Boolean {
        if (status != TaskStatus.PENDING || dueDate == null) return false
        return dueDate.value <= now
    }
}
```

---

### 2.3. Abstracción Temporal: `TimeProvider`

Para garantizar el determinismo en las pruebas automatizadas (RNF-04, CA-26), el dominio abstrae la fuente del tiempo actual mediante la interfaz `TimeProvider`:

```kotlin
interface TimeProvider {
    fun now(): Instant
    fun timeZone(): ZoneId
}
```

- **En Producción:** Proporciona `Clock.systemDefaultZone()`.
- **En Pruebas Unitarias:** Permite inyectar un reloj fijo/controlado (`Clock.fixed(...)`).

---

### 2.4. Modelos de Filtro y Ordenamiento

#### `TaskFilter`
Encapsula los criterios de filtrado de tareas (RF-07, RF-08, RF-09).

- **Filtros de Vencimiento:**
  - `All`: Incluye todas las tareas.
  - `Overdue`: Tareas pendientes con `dueDate <= now`.
  - `DueToday`: Tareas pendientes cuyo `dueDate` se encuentre en el día calendario actual y sea estrictamente posterior a `now`.
  - `Upcoming`: Tareas de cualquier estado cuyo `dueDate` sea mayor o igual al inicio del día calendario siguiente.
  - `NoDueDate`: Tareas sin fecha de vencimiento (`dueDate == null`).
  - `SpecificDate(date: LocalDate)`: Tareas de cualquier estado cuyo `dueDate` pertenezca al día seleccionado.
- **Filtro de Prioridad:**
  - `ByPriority(priority: Priority)`: Tareas que coincidan con la prioridad seleccionada.
- **Filtro Combinado:**
  - `Combined(dueDateFilter: TaskFilter, priorityFilter: TaskFilter)`: Requiere que se cumplan ambas condiciones simultáneamente (AND lógico, RF-09, RN-08).

#### `TaskSort`
Encapsula el criterio de ordenamiento solicitado por el usuario (RF-10, RN-07).
- Valóres: `DueDateAsc` (predeterminado), `DueDateDesc`, `PriorityAsc`, `PriorityDesc`.

---

### 2.5. Algoritmo de Ordenamiento Determinista (`TaskSorter`)

El comparador `TaskSorter` implementa las reglas de ordenamiento primario y los desempates secundarios requeridos por **RF-10** y **RN-07**:

```
Reglas de Ordenamiento por Fecha (DueDateAsc / DueDateDesc):
1. Tareas CON fecha de vencimiento se ordenan entre sí por fecha (ascendente o descendente según selección).
2. Tareas SIN fecha de vencimiento se colocan SIEMPRE al final de la lista (tanto en orden asc como desc).
3. Desempate secundario entre tareas con igual fecha (o entre tareas sin fecha):
   a. Prioridad descendente: HIGH -> MEDIUM -> LOW.
   b. Título alfabético ascendente: Normalizado Unicode NFC, sin distinguir mayúsculas/minúsculas, conservando acentos.
   c. Identificador de tarea (TaskId): Orden alfabético ascendente como desempate técnico final.

Reglas de Ordenamiento por Prioridad (PriorityAsc / PriorityDesc):
1. Ordenamiento por valor de prioridad (Baja -> Media -> Alta o viceversa).
2. Desempate secundario entre tareas con igual prioridad:
   a. Título alfabético ascendente (NFC, case-insensitive).
   b. Identificador de tarea (TaskId) ascendente.
```

---

### 2.6. Contratos e Interfaces del Dominio

#### Contrato de Repositorio: `TaskRepository`

```kotlin
interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: TaskId): Task?
    suspend fun saveTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: TaskId)
}
```

---

### 2.7. Casos de Uso (*Use Cases*)

#### 1. `CreateTaskUseCase`
- **Propósito:** Crear e ingresar una nueva tarea.
- **Entradas:** `titleRaw: String`, `description: String?`, `priority: Priority?`, `dueDateRaw: Instant?`.
- **Comportamiento:**
  1. Construye `TaskTitle(titleRaw)` (aplica trim, valida vacíos y límite de 36 code points Unicode).
  2. Asigna la prioridad explícita o `Priority.MEDIUM` por defecto si no se indicó (RN-03).
  3. Construye `TaskDueDate` (si se indica fecha sin hora, asigna `00:00` en la zona horaria actual) (RN-05).
  4. Crea la entidad `Task` con estado inicial `PENDING`.
  5. Invoca `TaskRepository.saveTask(task)`.

#### 2. `UpdateTaskUseCase`
- **Propósito:** Modificar una tarea existente.
- **Entradas:** `id: TaskId`, `titleRaw: String`, `description: String?`, `priority: Priority`, `dueDate: TaskDueDate?`.
- **Comportamiento:**
  1. Construye y valida el nuevo `TaskTitle`.
  2. Actualiza los campos permitiendo modificar título, descripción, prioridad y asignar o remover la fecha de vencimiento.
  3. Invoca `TaskRepository.updateTask(task)`.

#### 3. `ToggleTaskStatusUseCase`
- **Propósito:** Cambiar el estado de una tarea entre pendiente y completada (RF-05, CA-11).
- **Entradas:** `id: TaskId`.
- **Comportamiento:**
  1. Obtiene la tarea actual vía `TaskRepository.getTaskById(id)`.
  2. Si el estado actual es `PENDING`:
     - Cambia el estado a `COMPLETED`.
  3. Si el estado actual es `COMPLETED` (reapertura de tarea):
     - Obtiene el instante actual `now = timeProvider.now()`.
     - Si la tarea tiene fecha de vencimiento `dueDate` y `dueDate.value <= now` (la tarea ya venció), **elimina la fecha de vencimiento** (`dueDate = null`).
     - Si `dueDate.value > now`, **conserva** la fecha de vencimiento.
     - Cambia el estado a `PENDING`.
  4. Invoca `TaskRepository.updateTask(updatedTask)`.

#### 4. `DeleteTaskUseCase`
- **Propósito:** Eliminar una tarea guardada (RF-06).
- **Entradas:** `id: TaskId`.
- **Comportamiento:**
  1. Invoca `TaskRepository.deleteTask(id)`.

#### 5. `GetTasksUseCase`
- **Propósito:** Obtener las tareas filtradas y ordenadas de manera determinista (RF-03, RF-07, RF-08, RF-09, RF-10).
- **Entradas:** `filter: TaskFilter`, `sort: TaskSort`.
- **Comportamiento:**
  1. Escucha la lista de tareas desde `TaskRepository.getTasks()`.
  2. Obtiene `now = timeProvider.now()` y la zona horaria del dispositivo.
  3. Aplica los filtros especificados en `filter` (evaluando `isOverdue`, rangos del día calendario para "Hoy" y "Próximas", y prioridad).
  4. Ordena los resultados aplicando `TaskSorter` según el parámetro `sort`.
  5. Retorna un `Flow<List<Task>>` con la lista resultante.

---

## 3. Capa de Datos (`Data`)

### 3.1. Responsabilidades de la Capa de Datos
- Implementar la interfaz `TaskRepository` mediante `TaskRepositoryImpl`.
- Proporcionar acceso a la persistencia local vía `TaskLocalDataSource`.
- Realizar el mapeo de objetos entre los DTOs/Entidades de la base de datos local (`TaskEntity`) y las entidades de dominio (`Task`).

### 3.2. Modelo de Persistencia Local (`TaskEntity`)
```kotlin
data class TaskEntity(
    val id: String,
    val title: String,
    val description: String?,
    val priority: String, // "LOW", "MEDIUM", "HIGH"
    val dueDateEpochMillis: Long?, // Timestamp en milisegundos o null
    val status: String // "PENDING", "COMPLETED"
)
```

---

## 4. Capa de Presentación (`Presentation`)

### 4.1. Responsabilidades
- Gestionar el estado de la interfaz de usuario mediante `ViewModel` y `StateFlow`.
- Transformar interacciones de usuario en llamados a los casos de uso.
- Renderizar la UI de manera declarativa con **Jetpack Compose**.

### 4.2. Estados de UI (`UiState`)

#### `TaskListUiState`
```kotlin
data class TaskListUiState(
    val tasks: List<TaskItemUiModel> = emptyList(),
    val activeFilter: TaskFilter = TaskFilter.All,
    val activeSort: TaskSort = TaskSort.DueDateAsc,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class TaskItemUiModel(
    val id: String,
    val title: String,
    val description: String?,
    val priority: Priority,
    val dueDateFormatted: String?,
    val isCompleted: Boolean,
    val isOverdue: Boolean // Evaluado para resaltar visualmente si la tarea está vencida
)
```

---

## 5. Flujos Lógicos y Secuencias de Ejecución

### 5.1. Flujo de Reapertura de Tarea Vencida (RF-05, CA-11)

```
[Usuario] -> Toca "Reabrir tarea completada"
   |
   v
[TaskListViewModel] -> Llama a ToggleTaskStatusUseCase(taskId)
   |
   v
[ToggleTaskStatusUseCase]
   |--> 1. getTaskById(taskId) desde TaskRepository
   |--> 2. Consulta timeProvider.now()
   |--> 3. Evalúa: ¿dueDate <= now?
   |       |-- SÍ: Asigna dueDate = null, status = PENDING (Elimina vencimiento vencido)
   |       |-- NO: Conserva dueDate, status = PENDING
   |--> 4. updateTask(updatedTask) en TaskRepository
   |
   v
[TaskRepositoryImpl] -> Persiste cambios en TaskLocalDataSource
   |
   v
[Flow<List<Task>>] Emite lista actualizada -> ViewModel -> UI re-renderizada
```

---

## 6. Manejo de Errores y Excepciones de Dominio

La capa de dominio define excepciones explícitas para fallos de reglas de negocio:

```kotlin
sealed class TaskDomainException(message: String) : Exception(message) {
    sealed class InvalidTitle(message: String) : TaskDomainException(message) {
        object Empty : InvalidTitle("El título de la tarea no puede estar vacío.")
        data class ExceedsLength(val actualLength: Int) : 
            InvalidTitle("El título no puede superar los 36 puntos de código Unicode (actual: $actualLength).")
    }
    object TaskNotFound : TaskDomainException("La tarea solicitada no existe.")
}
```

---

## 7. Próximos Pasos

1. Documentar la selección de tecnologías concretas (versión de Android, persistencia local, CI y análisis estático) en `docs/implementation_plan.md`.
2. Registrar las decisiones de arquitectura adoptadas mediante ADRs en `docs/adr/`.
