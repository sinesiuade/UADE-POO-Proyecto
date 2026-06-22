package com.tpo.model.entities.catalog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Item {

    private int codigo;
    private String descripcion;
    private Rubro rubro;
    private String unidadMedida;
    private double precioUnitario;
    private float porcentajeIva;
}
