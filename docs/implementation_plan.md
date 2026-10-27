# Plan de implementación y decisiones técnicas — Android Architecture Lab

**Documento:** `docs/implementation_plan.md`  
**Estado:** Borrador consolidado  
**Versión:** 1.0  
**Idioma:** Español  
**Documentos de referencia:** `docs/specification.md` (v1.0), `docs/requirements_traceability.md` (v1.0), `docs/logic_design.md` (v1.0)

---

## 1. Propósito y Alcance

Este documento establece el plan de implementación técnica para la aplicación Android de gestión de tareas. Define la resolución explícita de las decisiones técnicas que permanecían pendientes en la especificación, la estructura de módulos Gradle, las fases incrementales de desarrollo, la estrategia de controles de calidad y la automatización en GitHub Actions.

---

## 2. Resolución de Decisiones Técnicas Pendientes

De acuerdo con la Sección 12 de `docs/specification.md`, se formalizan las siguientes decisiones técnicas:

### 2.1. Versión Mínima de Android (`minSdk`)
- **Decisión:** `minSdk = 26` (Android 8.0 Oreo).
- **Justificación:** Proporciona soporte nativo para la API `java.time.*` (`Instant`, `LocalDate`, `ZoneId`, `Clock`) en el sistema operativo sin requerir desugaring pesado de API. Cubre más del 95% de los dispositivos Android activos.
- **Configuración adicional:** `compileSdk = 35`, `targetSdk = 35`.

### 2.2. Tecnología de Persistencia Local
- **Decisión:** **Room Database** (`androidx.room`).
- **Justificación:** Biblioteca de persistencia relacional oficial de Google. Proporciona verificación de consultas SQL en tiempo de compilación, integración directa con Coroutines y `Flow`, migraciones seguras y la capacidad de ejecutar pruebas de integración de persistencia usando bases de datos en memoria (`Room.inMemoryDatabaseBuilder`).

### 2.3. Inyección de Dependencias
- **Decisión:** **Inyección Manual de Dependencias** mediante un contenedor explícito (`AppContainer`).
- **Justificación:** Mantiene el proyecto liviano, simple y directo para un laboratorio de arquitectura, evitando procesadores de anotaciones pesados o generación excesiva de código al inicio. El contenedor instanciará de forma centralizada la base de datos de Room, los repositorios, los casos de uso y los ViewModels.

### 2.4. Modularización Gradle
- **Decisión:** Estructura de 2 módulos Gradle:
  1. **`:domain`** (*Módulo Kotlin JVM puro*): Aplica exclusivamente `plugin id("kotlin")`. **No contiene ningún plugin ni dependencia de Android Framework**. Esto garantiza por construcción en tiempo de compilación que las reglas de dominio permanezcan 100% aisladas e independientes de Android (RNF-02, CA-32).
  2. **`:app`** (*Módulo Android Application*): Aplica `com.android.application`. Contiene la capa de datos (`data`), la capa de presentación (`presentation`), las pantallas en Jetpack Compose, el `AppContainer` y la configuración de Room. Depende del módulo `:domain`.

### 2.5. Herramientas de Análisis Estático y Umbrales de Calidad
- **Decisión:**
  - **ktlint:** Verificación y formateo de estilo de código idiomatico Kotlin.
  - **detekt:** Análisis estático de complejidad, smells y detección de errores de código.
  - **Android Lint:** Análisis estático oficial de recursos y configuraciones de Android.
- **Umbrales en CI:** 0 errores de compilación, 0 advertencias de ktlint, 0 errores de severidad alta/media en detekt. El flujo de CI fallará si no se cumplen estos umbrales.

### 2.6. Política de Zona Horaria y Referencia Temporal
- **Decisión:**
  - Las marcas de vencimiento se almacenan y comparan en **UTC** usando `java.time.Instant`.
  - Las operaciones que requieren fecha local (ej. determinar si una tarea "Vence hoy") utilizan la zona horaria del dispositivo vía `TimeProvider.timeZone()` (por defecto `ZoneId.systemDefault()`).
  - Para pruebas automatizadas deterministas (CA-26), se inyecta un `TimeProvider` sustentado en un `Clock` fijo (`Clock.fixed(...)`).

### 2.7. Metodología de Desarrollo Dirigida por Pruebas (TDD)
- **Decisión:** Implementar el ciclo **TDD (Red ➔ Green ➔ Refactor)** como flujo de trabajo obligatorio para las capas de componentes lógicos y ViewModels.
- **Flujo de Trabajo:**
  1. **Red (Rojo):** Redactar primero la suite de pruebas unitarias (`*Test.kt`) especificando los estados esperados ante cada evento. El test fallará o no compilará antes de implementar la clase.
  2. **Green (Verde):** Implementar el componente o ViewModel con el código mínimo necesario para que todas las pruebas pasen.
  3. **Refactor (Refactorización):** Limpiar el código, aplicar linters (`ktlintFormat`, `detekt`) y verificar que todos los tests continúen en verde.


---

## 3. Plan de Implementación Incremental por Fases

El desarrollo del proyecto se ejecutará en 5 fases secuenciales e incrementales:

```
+-------------------------------------------------------------------+
| FASE 1: Estructura Gradle (:domain, :app), Linters y CI GitHub   |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
| FASE 2: Capa Domain (Value Objects, Entities, UseCases) + UT JVM  |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
| FASE 3: Capa Data (Room Database, Entity, Dao, Repo) + IT Data    |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
| FASE 4: Capa Presentation (ViewModels, Jetpack Compose UI)        |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
| FASE 5: Verificación Global en CI y Documentación Final           |
+-------------------------------------------------------------------+
```

### Fase 1 — Infraestructura Gradle, Calidad y CI
- Configuración de `settings.gradle.kts` con módulos `:domain` y `:app`.
- Configuración de Version Catalogs (`gradle/libs.versions.toml`).
- Configuración de plugins de `ktlint` y `detekt`.
- Creación de `.github/workflows/ci.yml` para ejecutar la compilación y pruebas en cada `push` y `pull_request`.

### Fase 2 — Capa de Dominio y Pruebas Unitarias JVM
- Implementación de Objetos de Valor (`TaskTitle`, `TaskId`, `Priority`, `TaskStatus`, `TaskDueDate`).
- Implementación de la entidad `Task` y del ordenador determinista `TaskSorter`.
- Implementación de la abstracción `TimeProvider`.
- Implementación de los 5 casos de uso (`CreateTaskUseCase`, `UpdateTaskUseCase`, `ToggleTaskStatusUseCase`, `DeleteTaskUseCase`, `GetTasksUseCase`).
- **Pruebas Unitarias:** Cobertura de todos los Criterios de Aceptación del dominio (CA-01 a CA-23, CA-25, CA-26) mediante pruebas unitarias puras en la JVM sin emulador.

### Fase 3 — Capa de Datos y Persistencia Local (Room)
- Creación de `TaskEntity`, `TaskDao` y `TaskDatabase` con Room.
- Implementación de `TaskLocalDataSource` y `TaskRepositoryImpl`.
- **Pruebas de Integración:** Verificación de operaciones CRUD y reactividad de `Flow` con base de datos Room en memoria (CA-24, CA-29).

### Fase 4 — Capa de Presentación y UI con Jetpack Compose
- Creación de `AppContainer` para inyección de dependencias manual.
- Implementación de `TaskListViewModel` y `TaskFormViewModel`.
- Implementación de la interfaz gráfica con Jetpack Compose (lista de tareas, selectores de filtro y orden, modal/pantalla de creación y edición).
- **Pruebas de ViewModel/UI:** Verificación de estados visuales y manejo de eventos (CA-30).

### Fase 5 — Verificación Global y Documentación
- Ejecución completa del flujo de CI.
- Redacción de `README.md` con instrucciones precisas de compilación y ejecución desde un clon limpio (CA-28).

---

## 4. Estrategia de Verificación y Comandos

Un clon limpio del repositorio se podrá compilar y verificar localmente mediante los siguientes comandos Gradle:

| Tarea | Comando Gradle | Descripción |
| :--- | :--- | :--- |
| **Compilación completa** | `./gradlew assembleDebug` | Compila todos los módulos del proyecto. |
| **Pruebas de Dominio** | `./gradlew :domain:test` | Ejecuta las pruebas unitarias puras en la JVM. |
| **Pruebas de Integración** | `./gradlew :app:test` | Ejecuta las pruebas de repositorio, Room y ViewModels. |
| **Análisis Estático** | `./gradlew ktlintCheck detekt` | Verifica el estilo y busca potenciales code smells. |
| **Verificación Global (CI)** | `./gradlew check` | Ejecuta todas las pruebas y análisis estáticos configurados. |

---

## 5. Próximos Pasos

1. Registrar las decisiones técnicas confirmadas en los registros de decisiones arquitectónicas en `docs/adr/`.
2. Iniciar la **Fase 1** creando la configuración base de Gradle, módulos `:domain` y `:app`, linters y el flujo de GitHub Actions.
