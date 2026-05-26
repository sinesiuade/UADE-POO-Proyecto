# TPO - Aplicación Java (MVC + Swing)

Trabajo Práctico Obligatorio de Programación Orientada a Objetos.  
Aplicación de escritorio en **Java** con arquitectura **MVC** e interfaz gráfica con **Swing**.

## Descripción

Proyecto base con separación de responsabilidades en tres capas: modelo (datos y lógica de negocio), vista (interfaz gráfica) y controlador (coordinación entre ambas).

## Tecnologías

- Java 17+
- Swing (`javax.swing`)

## Arquitectura MVC

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| **Model** | `model` | Entidades, reglas de negocio y acceso a datos |
| **View** | `view` | Interfaz gráfica (`JFrame`, `JPanel`, `JDialog`) |
| **Controller** | `controller` | Manejo de eventos y comunicación entre vista y modelo |

### Estructura del proyecto

```
src/main/java/com/tpo/
├── Main.java
├── model/
│   ├── entities/
│   ├── dto/
│   └── services/
├── view/
│   ├── frames/
│   ├── panels/
│   └── dialogs/
├── controller/
└── util/
```

## Requisitos

- JDK 25
- IDE compatible (IntelliJ IDEA, Eclipse, VS Code con extensión Java)

## Cómo ejecutar

### Desde el IDE

1. Clonar el repositorio.
2. Abrir el proyecto en el IDE.
3. Marcar `src/main/java` como **Sources Root** (IntelliJ: clic derecho → Mark Directory as → Sources Root).
4. Ejecutar la clase `com.tpo.Main`.

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
