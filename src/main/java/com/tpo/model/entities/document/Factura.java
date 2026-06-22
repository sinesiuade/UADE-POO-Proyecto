package com.tpo.model.entities.document;

import com.tpo.model.Enums.EstadoCancelacionDocumentoComercial;
import com.tpo.model.entities.supplier.Proveedor;

import java.util.Date;

public class Factura extends DocumentoComercial {

    public Factura() {
        super();
    }

    /** Constructor del Diagrama de Secuencia "Generación de Factura": new Factura(proveedor). */
    public Factura(Proveedor proveedor) {
        setProveedor(proveedor);
        setFechaEmision(new Date());
        setEstadoCancelacion(EstadoCancelacionDocumentoComercial.PENDIENTE);
    }

    /** Crea y agrega una línea de detalle a la factura, recalculando el importe total. */
    public DetalleItemDocComercial crearDetalle(String concepto, int cantidad, double precio, double iva) {
        DetalleItemDocComercial detalle = new DetalleItemDocComercial(concepto, cantidad, precio, iva);
        agregarDetalle(detalle);
        return detalle;
    }
}
