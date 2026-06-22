package com.tpo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Resultado genérico de un reporte: título, columnas y filas. */
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
