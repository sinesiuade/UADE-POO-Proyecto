# TPO - Aplicación Java (MVC + Swing)

Trabajo Práctico Obligatorio de Programación Orientada a Objetos.  
Aplicación de escritorio en **Java** con arquitectura **MVC** e interfaz gráfica con **Swing**.

## Descripción

Proyecto base con separación de responsabilidades en tres capas: modelo (datos y lógica de negocio), vista (interfaz gráfica) y controlador (coordinación entre ambas).

## Tecnologías

- Java 25
- Maven
- Swing (`javax.swing`)
- Gson (persistencia en archivos JSON)
- SLF4J + Logback (logs)
- Lombok
- JUnit 5 (tests)

## Arquitectura MVC

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| **Model** | `model` | Entidades, DTOs, repositorios y reglas de negocio |
| **View** | `view` | Interfaz gráfica (`JFrame`, `JPanel`, `JDialog`) |
| **Controller** | `controller` | Manejo de eventos y comunicación entre vista y modelo |

### Estructura del proyecto

```
src/main/java/com/tpo/
├── Main.java
├── model/
│   ├── entities/      # Entidades del dominio
│   ├── dto/           # Objetos de transferencia (vista ↔ servicio, JSON)
│   ├── repositories/  # Acceso y persistencia de datos
│   └── services/      # Lógica de negocio
├── view/
│   ├── frames/
│   ├── panels/
│   └── dialogs/
├── controller/
└── util/

data/                    # Archivos JSON de persistencia
src/main/resources/      # logback.xml y otros recursos
src/test/java/           # Tests con JUnit 5
```

## Requisitos

- JDK 25
- Lombok 1.18.40+ (el proyecto usa 1.18.46)
- Maven 3.9+
- IDE compatible (IntelliJ IDEA, Eclipse, VS Code con extensión Java)

## Dependencias (Maven)

| Librería | Uso |
|----------|-----|
| **Gson** | Leer y escribir datos en archivos `.json` (carpeta `data/`) |
| **SLF4J + Logback** | Registro de eventos y errores (`logs/app.log`) |
| **Lombok** | Reducir código repetitivo en entidades (`@Getter`, `@Setter`, etc.) |
| **JUnit 5** | Tests unitarios en `src/test/java` |

## Persistencia (JSON)

Los datos se guardan en archivos JSON dentro de la carpeta `data/` en la raíz del proyecto (no se usa base de datos).

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
