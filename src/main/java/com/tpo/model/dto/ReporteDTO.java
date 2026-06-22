package com.tpo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado genérico de una consulta/reporte: título, encabezados de columna y filas.
 * Permite que el ConsultaController devuelva cualquier reporte y la vista lo muestre en una tabla.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDTO {
    private String titulo;
    private String[] columnas;
    private List<Object[]> filas = new ArrayList<>();

    public ReporteDTO(String titulo, String[] columnas) {
        this.titulo = titulo;
        this.columnas = columnas;
        this.filas = new ArrayList<>();
    }

    public void agregarFila(Object... valores) {
        filas.add(valores);
    }
}
