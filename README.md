# Android Architecture Lab — Gestión de Tareas

[![Android CI Pipeline](https://github.com/pabloquiroga/android-architecture-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/pabloquiroga/android-architecture-lab/actions/workflows/ci.yml)

Repositorio técnico y portfolio de ingeniería de software orientado a demostrar buenas prácticas de desarrollo Android mediante una aplicación de gestión de tareas basada en **Clean Architecture**, **Specification-Driven Development (SDD)**, **TDD**, **Jetpack Compose** y **Room Database**.

---

## 🏛️ Arquitectura y Diseño

El proyecto aplica los principios de **Clean Architecture** mediante una separación estricta de responsabilidades a nivel de módulos Gradle:

```
android-architecture-lab/
├── :domain (Módulo Kotlin JVM puro, 100% independiente de Android)
│   ├── model/         # Value Objects (TaskTitle, TaskId, TaskDueDate, Priority, TaskStatus) y Entidad Task
│   ├── repository/    # Interfaz TaskRepository
│   ├── usecase/       # Casos de Uso (Create, Update, ToggleStatus, Delete, GetTasks)
│   └── time/          # Abstracción temporal TimeProvider (para determinismo en tests)
└── :app (Módulo Android Application)
    ├── data/          # Room Entity, DAO, Mappers, LocalDataSource, TaskRepositoryImpl
    ├── di/            # AppContainer (Inyección de dependencias manual explícita)
    └── presentation/  # ViewModels (StateFlow/MVI), Jetpack Compose Screens, MainActivity
```

### Principios Fundamentales
- **Independencia del Dominio:** El módulo `:domain` es Kotlin JVM puro (sin plugins ni librerías de Android), garantizando en tiempo de compilación que las reglas de negocio permanezcan libres de dependencias externas.
- **Determinismo Temporal:** Las comparaciones de vencimiento y filtros temporales utilizan la abstracción `TimeProvider`, lo que permite probar escenarios pasados, presentes y futuros de forma reproducible mediante un reloj fijo (`Clock.fixed`).
- **Ordenamiento Determinista:** Algoritmo de comparación con desempates explícitos (vencimiento -> prioridad desc -> título NFC case-insensitive asc -> ID asc) garantizando resultados predecibles independientemente del orden de almacenamiento.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología / Librería | Justificación |
| :--- | :--- | :--- |
| **Lenguaje** | Kotlin 2.0.21 | Lenguaje oficial con Coroutines, Flow y Value Classes. |
| **SDK Mínimo** | Android 8.0 (minSdk 26) | Soporte nativo de `java.time.*` sin desugaring pesado. |
| **UI** | Jetpack Compose (BOM 2024.10.01) | Interfaz de usuario declarativa moderna. |
| **Persistencia** | Room 2.6.1 + KSP | Persistencia relacional local con verificación SQL en compilación. |
| **Inyección de Dependencias** | Manual (`AppContainer`) | Contenedor liviano, explícito y transparente sin sobrecarga de código generado. |
| **Calidad de Código** | ktlint & detekt & Android Lint | Análisis estático y formateo automático de código. |
| **CI/CD** | GitHub Actions | Pipeline automatizado de compilación, linters y tests en cada push/PR. |

---

## 🚀 Entorno y Requisitos de Reproducibilidad

Para clonar, compilar y ejecutar este proyecto desde un entorno limpio se requiere:

- **JDK:** Java 17 o Java 21 (Temurin / Corretto / Android Studio Embedded JDK).
- **Android Studio:** Ladybug (2024.2.1) o posterior.
- **Gradle:** Wrapper 8.10.2 (incluido en el repositorio).
- **Conexión a Internet:** Requerida únicamente para descargar las dependencias en la primera compilación. La aplicación opera 100% offline.

---

## 🧪 Comandos de Compilación, Pruebas y Calidad

Desde la raíz del proyecto se pueden ejecutar las siguientes tareas Gradle mediante el wrapper:

### 1. Compilación del APK de Desarrollo
```bash
./gradlew assembleDebug
```

### 2. Ejecución de Pruebas Unitarias de Dominio (JVM)
```bash
./gradlew :domain:test
```

### 3. Ejecución de Pruebas de Integración y ViewModels
```bash
./gradlew :app:testDebugUnitTest
```

### 4. Formateo y Análisis Estático de Código
```bash
./gradlew ktlintFormat
./gradlew detekt ktlintCheck
```

### 5. Verificación Global (Pipeline de CI)
```bash
./gradlew check
```

---

## 📚 Documentación del Proyecto

Toda la documentación técnica del proyecto se encuentra redactada en español en la carpeta `docs/`:

- [`docs/specification.md`](docs/specification.md): Especificación de Requisitos Funcionales, Reglas de Negocio y Criterios de Aceptación.
- [`docs/requirements_traceability.md`](docs/requirements_traceability.md): Matriz de Trazabilidad entre requisitos, arquitectura y pruebas.
- [`docs/logic_design.md`](docs/logic_design.md): Diseño Lógico, contratos de capas, casos de uso y diagramas de flujo.
- [`docs/implementation_plan.md`](docs/implementation_plan.md): Plan de implementación técnica por fases y metodología TDD.
- [`docs/adr/ADR-001.md`](docs/adr/ADR-001.md): Registro de Decisiones Arquitectónicas (Clean Architecture y Stack).
