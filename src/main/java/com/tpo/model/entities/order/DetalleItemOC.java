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

    /** Subtotal de la línea: cantidad por el precio unitario acordado. */
    public double getPrecioTotal() {
        return cantidad * precioAcordado;
    }

    /** Traduce esta línea de la OC a un detalle de documento comercial (para la Factura). */
    public DetalleItemDocComercial getDatosParaItemFactura() {
        String concepto = item != null ? item.getDescripcion() : null;
        double iva = item != null ? item.getPorcentajeIva() : 0.0;
        return new DetalleItemDocComercial(concepto, cantidad, precioAcordado, iva);
    }
}
