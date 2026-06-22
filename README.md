# TPO - Aplicación Java (MVC + Swing)

Trabajo Práctico Obligatorio de Programación Orientada a Objetos.  
Aplicación de escritorio en **Java** con arquitectura **MVC** e interfaz gráfica con **Swing**.

## Descripción

Proyecto base con separación de responsabilidades en tres capas: modelo (datos y lógica de negocio), vista (interfaz gráfica) y controlador (coordinación entre ambas).

**Versión del proyecto:** `1.0.3` — artifact `uade-poo-proyecto`

## Tecnologías

- Java 21
- Maven 3.9+
- Swing (`javax.swing`)
- Gson 2.11.0 (persistencia en archivos JSON)
- SLF4J 2.0.13 + Logback 1.5.6 (logs)
- Lombok 1.18.46
- JUnit 5.10.2 (tests)

## Arquitectura MVC

El proyecto sigue **únicamente** los patrones **MVC** y **Singleton** (un controlador Singleton
por módulo). No hay capa de servicios ni de repositorios: las reglas de negocio viven en las
entidades del modelo y los controladores las coordinan.

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| **Model** | `model` | Entidades (con las reglas de negocio), DTOs y enums |
| **View** | `view` | Interfaz gráfica (`JFrame`, `JPanel`, `JDialog`) |
| **Controller** | `controller` | Controladores Singleton: coordinan vista ↔ modelo y aplican los flujos |

### Controladores Singleton

| Controlador | Diagrama de Secuencia implementado |
|-------------|-------------------------------------|
| `OrdenDeCompraController` | Creación de OC (validación de límite de crédito → estado calculado) |
| `OrdenDePagoController` | Generación de OP (cálculo de retenciones por impuesto) |
| `FacturaController` | Generación de Factura (validación de OC, congruencia rubro/ítem, control de precios) |
| `ImpuestoController` | Catálogo de impuestos del sistema usado por la OP |

### Estructura del proyecto

```
src/main/java/com/tpo/
├── Main.java
├── model/
│   ├── entities/      # Entidades del dominio (reglas de negocio)
│   ├── dto/           # Objetos de transferencia (vista ↔ controlador)
│   └── Enums/         # Enumerados del dominio
├── view/
│   ├── frames/        # Ventanas principales (JFrame)
│   ├── panels/        # Paneles reutilizables (JPanel)
│   └── dialogs/       # Diálogos (JDialog)
├── controller/        # Controladores Singleton (uno por módulo)
└── util/              # Constantes y utilidades (AppConstants, Validator)

resources/
└── images/            # Imágenes y recursos gráficos

data/                  # Archivos JSON de persistencia
target/classes/        # logback.xml y clases compiladas
logs/                  # app.log generado en ejecución
src/test/java/         # Tests con JUnit 5
```

## Requisitos

- JDK 21
- Lombok 1.18.46
- Maven 3.9+
- IDE compatible (IntelliJ IDEA, Eclipse, VS Code con extensión Java)

## Dependencias (Maven)

| Librería | Versión | Uso |
|----------|---------|-----|
| **Gson** | 2.11.0 | Leer y escribir datos en archivos `.json` (carpeta `data/`) |
| **SLF4J** | 2.0.13 | API de logging |
| **Logback** | 1.5.6 | Implementación de logs (`logs/app.log`) |
| **Lombok** | 1.18.46 | Reducir código repetitivo en entidades (`@Getter`, `@Setter`, etc.) |
| **JUnit 5** | 5.10.2 | Tests unitarios en `src/test/java` |

## Persistencia

El estado se mantiene **en memoria** dentro de cada controlador Singleton durante la ejecución
(no se usa base de datos ni archivos). Gson/Logback quedan disponibles como dependencias pero la
persistencia en disco no es parte de esta entrega.

## Cómo ejecutar

### Con Maven (recomendado)

```bash
mvn compile
mvn exec:java
mvn test
```

### Desde el IDE

1. Clonar el repositorio.
2. Abrir el proyecto como **proyecto Maven** (IntelliJ detecta el `pom.xml` automáticamente).
3. Habilitar **annotation processing** para Lombok:  
   Settings → Build → Compiler → Annotation Processors → Enable.
4. Ejecutar la clase `com.tpo.Main` o usar la configuración Maven `exec:java`.

## Convenciones de código

- **Model**: sin dependencias de Swing.
- **View**: no contiene lógica de negocio; solo presenta datos y captura entrada.
- **Controller**: recibe eventos de la vista, delega al modelo y actualiza la vista.
- Eventos Swing en el hilo de UI: `SwingUtilities.invokeLater(...)`.

## Integrantes

| Nombre | Legajo / Rol |
|--------|----------------|
| ...    | ...            |

## Licencia

Proyecto académico.
