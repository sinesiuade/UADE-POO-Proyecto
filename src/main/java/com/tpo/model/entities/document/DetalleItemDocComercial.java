package com.tpo.model.entities.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleItemDocComercial {

    private String concepto;
    private int cantidad;
    private double precio;
    private double iva;
}
