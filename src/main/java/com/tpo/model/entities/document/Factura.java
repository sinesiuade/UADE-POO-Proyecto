package com.tpo.model.entities.document;

import com.tpo.model.Enums.EstadoCancelacionDocumentoComercial;
import com.tpo.model.entities.supplier.Proveedor;

import java.util.Date;

public class Factura extends DocumentoComercial {

    public Factura() {
        super();
    }

    /** Factura para un proveedor (estado inicial PENDIENTE). */
    public Factura(Proveedor proveedor) {
        setProveedor(proveedor);
        setFechaEmision(new Date());
        setEstadoCancelacion(EstadoCancelacionDocumentoComercial.PENDIENTE);
    }

    /** Agrega una línea de detalle y recalcula el importe. */
    public DetalleItemDocComercial crearDetalle(String concepto, int cantidad, double precio, double iva) {
        DetalleItemDocComercial detalle = new DetalleItemDocComercial(concepto, cantidad, precio, iva);
        agregarDetalle(detalle);
        return detalle;
    }
}
