package com.tpo.model.entities.order;

import com.tpo.model.entities.catalog.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleItemOC {

    private Item item;
    private int cantidad;
    private double precioAcordado;
}
