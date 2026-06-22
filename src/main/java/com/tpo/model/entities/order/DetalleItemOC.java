package com.tpo.model.entities.order;

import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.document.DetalleItemDocComercial;
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

    /** Subtotal = cantidad x precio acordado. */
    public double getPrecioTotal() {
        return cantidad * precioAcordado;
    }

    /** Traduce la línea de OC a un detalle de documento comercial. */
    public DetalleItemDocComercial getDatosParaItemFactura() {
        String concepto = item != null ? item.getDescripcion() : null;
        double iva = item != null ? item.getPorcentajeIva() : 0.0;
        return new DetalleItemDocComercial(concepto, cantidad, precioAcordado, iva);
    }
}
