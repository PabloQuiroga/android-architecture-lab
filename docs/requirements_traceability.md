# Matriz de trazabilidad de requisitos — Android Architecture Lab

**Documento:** `docs/requirements_traceability.md`  
**Estado:** Borrador inicial consolidado  
**Versión:** 1.0  
**Idioma:** Español  
**Documento fuente:** `docs/specification.md` (v1.0)

---

## 1. Propósito

Este documento establece la trazabilidad entre los requisitos funcionales (RF), reglas de negocio (RN), requisitos no funcionales (RNF) y criterios de aceptación (CA) definidos en `docs/specification.md`, relacionándolos con los componentes lógicos de arquitectura (casos de uso, dominio, persistencia, UI) y las estrategias de prueba y verificación automatizada.

---

## 2. Convenciones y Estados

### Niveles de arquitectura
- **Domain:** Entidades, Objetos de Valor (*Value Objects*), Casos de Uso e Interfaces de Repositorio. Independiente de Android.
- **Data:** Implementación de Repositorios, Fuentes de Datos Locales (*Data Sources*), Mapeadores y Persistencia.
- **Presentation:** ViewModels, Estados de UI (`StateFlow`), Pantallas y Componentes Jetpack Compose.
- **Infra / CI:** Configuración de Gradle, GitHub Actions y herramientas de análisis estático.

### Estrategias de prueba
- **UT-Domain:** Pruebas unitarias de reglas de negocio y casos de uso en la JVM (sin dependencias de Android).
- **IT-Data:** Pruebas de integración de persistencia local y repositorios.
- **UI-Test:** Pruebas de interfaz de usuario y ViewModel.
- **CI-Check:** Verificaciones de compilación, análisis estático y pipeline automatizado.

### Estado de implementación
- `[ESPECIFICADO]` Definido conceptualmente en la especificación; pendiente de implementación en código.
- `[EN_PROCESO]` En desarrollo activo.
- `[VERIFICADO]` Código implementado y probado automatizadamente.

---

## 3. Trazabilidad de Requisitos Funcionales y Reglas de Negocio

| ID RF / RN | Descripción Sintética | Componente / Caso de Uso Lógico | Estrategia de Prueba | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **RF-01** | Crear tarea con título obligatorio y campos opcionales | `CreateTaskUseCase`, `Task` | UT-Domain | `[ESPECIFICADO]` |
| **RF-02 / RN-01 / RN-02** | Validar y normalizar título (trim, no vacío, máx. 36 code points Unicode) | `TaskTitle` (Value Object) | UT-Domain | `[ESPECIFICADO]` |
| **RF-03** | Consultar y listar tareas respetando filtros y orden | `GetTasksUseCase` | UT-Domain, UI-Test | `[ESPECIFICADO]` |
| **RF-04** | Editar tarea y sus campos opcionales (incluyendo quitar vencimiento) | `UpdateTaskUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **RF-05** | Completar y reabrir tareas (eliminar vencimiento al reabrir si venció) | `ToggleTaskStatusUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **RF-06** | Eliminar tarea guardada | `DeleteTaskUseCase` | UT-Domain, IT-Data | `[ESPECIFICADO]` |
| **RF-07 / RN-06** | Filtrar tareas por vencimiento (Todas, Vencidas, Vencen hoy, Próximas, Sin vencimiento, Fecha específica) | `GetTasksUseCase`, `TaskFilter.DueDate` | UT-Domain (Clock controlado) | `[ESPECIFICADO]` |
| **RF-08** | Filtrar tareas por prioridad (Baja, Media, Alta) | `GetTasksUseCase`, `TaskFilter.Priority` | UT-Domain | `[ESPECIFICADO]` |
| **RF-09 / RN-08** | Combinar filtros de vencimiento y prioridad (AND lógico) | `GetTasksUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **RF-10 / RN-07** | Ordenar tareas con desempates deterministas (vencimiento, prioridad, título NFC case-insensitive, ID) | `GetTasksUseCase`, `TaskSorter` | UT-Domain | `[ESPECIFICADO]` |
| **RF-11** | Persistir datos localmente entre reinicios de la app | `TaskRepository`, `TaskLocalDataSource` | IT-Data | `[ESPECIFICADO]` |
| **RF-12 / RN-06** | Identificar tareas vencidas (pendiente, con vencimiento, vencimiento <= ahora) | `Task.isOverdue()`, `Clock` | UT-Domain (Clock controlado) | `[ESPECIFICADO]` |
| **RN-03** | Prioridad predeterminada `MEDIUM` | `Task` | UT-Domain | `[ESPECIFICADO]` |
| **RN-04** | Fecha y hora de vencimiento opcionales | `Task` | UT-Domain | `[ESPECIFICADO]` |
| **RN-05** | Hora predeterminada `00:00` si se indica solo fecha | `TaskDueDate` / Mapper | UT-Domain | `[ESPECIFICADO]` |
| **RN-09** | Independencia del dominio respecto a Android UI y Framework | Módulo/Capa `domain` | CI-Check (Gradle/ArchUnit) | `[ESPECIFICADO]` |

---

## 4. Trazabilidad de Criterios de Aceptación (CA)

| ID CA | Criterio de Aceptación Resumido | Componentes Relacionados | Verificación Principal | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **CA-01** | Crear tarea con campos obligatorios u opcionales | `CreateTaskUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **CA-02** | Rechazar título vacío o de solo espacios | `TaskTitle` | UT-Domain | `[ESPECIFICADO]` |
| **CA-03** | Normalización de espacios iniciales/finales | `TaskTitle` | UT-Domain | `[ESPECIFICADO]` |
| **CA-04** | Aceptar título de hasta 36 puntos de código Unicode | `TaskTitle` | UT-Domain | `[ESPECIFICADO]` |
| **CA-05** | Rechazar título que supere 36 puntos de código Unicode | `TaskTitle` | UT-Domain | `[ESPECIFICADO]` |
| **CA-06** | Prioridad predeterminada media | `Task` | UT-Domain | `[ESPECIFICADO]` |
| **CA-07** | Crear tarea sin fecha de vencimiento | `CreateTaskUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **CA-08** | Asignar `00:00` si se especifica solo fecha | `TaskDueDate` | UT-Domain | `[ESPECIFICADO]` |
| **CA-09** | Formato de 24 horas (`00:00` a `23:59`) | Presentation / `TaskDueDate` | UT-Domain, UI-Test | `[ESPECIFICADO]` |
| **CA-10** | Editar campos y permitir remover vencimiento | `UpdateTaskUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **CA-11** | Cambiar estado y eliminar vencimiento al reabrir si ya venció | `ToggleTaskStatusUseCase` | UT-Domain (Clock) | `[ESPECIFICADO]` |
| **CA-12** | Eliminar tarea y excluirla de consultas | `DeleteTaskUseCase` | UT-Domain, IT-Data | `[ESPECIFICADO]` |
| **CA-13** | Tarea pendiente con vencimiento <= ahora es vencida (incluye coincidencia exacta) | `Task.isOverdue()` | UT-Domain (Clock) | `[ESPECIFICADO]` |
| **CA-14** | Tarea completada no se considera vencida | `Task.isOverdue()` | UT-Domain | `[ESPECIFICADO]` |
| **CA-15** | Tarea sin vencimiento no se considera vencida | `Task.isOverdue()` | UT-Domain | `[ESPECIFICADO]` |
| **CA-16** | Correcta clasificación de los 6 filtros de vencimiento | `GetTasksUseCase` | UT-Domain (Clock) | `[ESPECIFICADO]` |
| **CA-17** | Filtrado por prioridad e inclusión de tareas con/sin vencimiento | `GetTasksUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **CA-18** | Combinación de filtros de vencimiento y prioridad (AND) | `GetTasksUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **CA-19** | Ordenamiento por vencimiento asc/desc (predeterminado asc) | `GetTasksUseCase` | UT-Domain | `[ESPECIFICADO]` |
| **CA-20** | Tareas sin vencimiento siempre al final al ordenar por fecha | `TaskSorter` | UT-Domain | `[ESPECIFICADO]` |
| **CA-21** | Ordenamiento por prioridad con desempates por título NFC e ID | `TaskSorter` | UT-Domain | `[ESPECIFICADO]` |
| **CA-22** | Ordenamiento por vencimiento con desempate por prioridad, título NFC e ID | `TaskSorter` | UT-Domain | `[ESPECIFICADO]` |
| **CA-23** | Determinismo del orden independiente del orden de almacenamiento | `TaskSorter` | UT-Domain | `[ESPECIFICADO]` |
| **CA-24** | Persistencia local de operaciones y cambios al reabrir | `TaskRepository`, `LocalDataSource` | IT-Data | `[ESPECIFICADO]` |
| **CA-25** | Dominio ejecutable en pruebas unitarias sin emulador | Módulo `domain` | UT-Domain | `[ESPECIFICADO]` |
| **CA-26** | Determinismo temporal en tests con reloj/referencia controlable | `Clock` / `TimeProvider` | UT-Domain | `[ESPECIFICADO]` |
| **CA-27** | Ejecución de CI en GitHub Actions ante push y PR | `.github/workflows/ci.yml` | CI-Check | `[ESPECIFICADO]` |
| **CA-28** | Instrucciones de compilación y ejecución documentadas | `README.md` | CI-Check | `[ESPECIFICADO]` |
| **CA-29** | Operación 100% offline para el MVP | Persistencia Local | IT-Data | `[ESPECIFICADO]` |
| **CA-30** | Consultar tareas con visualización completa de atributos | Presentation / ViewModel | UI-Test | `[ESPECIFICADO]` |
| **CA-31** | Separación clara de capas (Presentation, Domain, Data) | Estructura del proyecto | CI-Check | `[ESPECIFICADO]` |
| **CA-32** | Ausencia de dependencias de Android en la capa Domain | Módulo/Capa `domain` | CI-Check | `[ESPECIFICADO]` |
| **CA-33** | Herramientas de análisis estático y umbrales en CI | CI / Analysis Scripts | CI-Check | `[ESPECIFICADO]` |

---

## 5. Trazabilidad de Requisitos No Funcionales (RNF)

| ID RNF | Descripción Resumida | Mecanismo de Cumplimiento / Control | Verificación | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **RNF-01** | Separación lógica de capas (Presentation, Domain, Data) | Organización en paquetes/módulos independientes | Code Review / ArchUnit | `[ESPECIFICADO]` |
| **RNF-02** | Independencia del dominio respecto a Android UI y persistencia | Restricción de dependencias en build Gradle de `domain` | Gradle Build | `[ESPECIFICADO]` |
| **RNF-03** | Testabilidad unitaria en JVM sin Android | Casos de uso y dominio puros sin framework | JUnit 5 / Kotest | `[ESPECIFICADO]` |
| **RNF-04** | Determinismo temporal | Abstracción de tiempo (`TimeProvider` / `Clock`) | UT-Domain | `[ESPECIFICADO]` |
| **RNF-05** | Funcionamiento offline | Almacenamiento local SQLite / Room | IT-Data | `[ESPECIFICADO]` |
| **RNF-06** | Mantenibilidad y responsabilidades claras | Reglas de Clean Architecture y SOLID | Code Review | `[ESPECIFICADO]` |
| **RNF-07** | Integración continua con GitHub Actions | `.github/workflows/ci.yml` en push/PR | GitHub Actions | `[ESPECIFICADO]` |
| **RNF-08** | Calidad de código y análisis estático | Configuración de linters (detekt / ktlint / Android Lint) | CI Check | `[ESPECIFICADO]` |
| **RNF-09** | Reproducibilidad del entorno | Documentación de versiones de JDK, Gradle y comandos en `README.md` | Manual / CI | `[ESPECIFICADO]` |

---

## 6. Próximos Pasos

1. Diseñar los componentes lógicos, contratos y flujos de datos en `docs/logic_design.md`.
2. Definir las tecnologías, herramientas de calidad y plan de implementación en `docs/implementation_plan.md`.
3. Actualizar el estado de cada ítem de esta matriz a `[EN_PROCESO]` y posteriormente `[VERIFICADO]` a medida que se desarrolle e implemente el código.
