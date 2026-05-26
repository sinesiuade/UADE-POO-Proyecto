package com.tpo.controller;

import com.tpo.model.dto.EjemploDTO;
import com.tpo.model.services.EjemploService;
import com.tpo.view.panels.EjemploPanel;

import java.util.List;
import java.util.stream.Collectors;

public class EjemploController {

    private final EjemploPanel vista;
    private final EjemploService servicio;

    public EjemploController(EjemploPanel vista, EjemploService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        initListeners();
        actualizarListado();
    }

    private void initListeners() {
        vista.getBtnGuardar().addActionListener(e -> guardar());
    }

    private void guardar() {
        try {
            String nombre = vista.getCampoNombre().getText();
            EjemploDTO creado = servicio.crear(nombre);
            vista.limpiarCampoNombre();
            vista.mostrarMensaje("Guardado correctamente: " + creado + "\n\n" + formatearListado());
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje("Error: " + ex.getMessage());
        }
    }

    private void actualizarListado() {
        vista.mostrarMensaje(formatearListado());
    }

    private String formatearListado() {
        List<EjemploDTO> ejemplos = servicio.listarTodos();
        if (ejemplos.isEmpty()) {
            return "No hay registros.";
        }
        return ejemplos.stream()
                .map(EjemploDTO::toString)
                .collect(Collectors.joining("\n"));
    }
}
