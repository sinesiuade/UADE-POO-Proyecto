package com.tpo.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemProveedor {

    private double precio;
    private Item itemBase;
    private Proveedor proveedor;
}
