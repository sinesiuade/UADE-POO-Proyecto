package com.tpo.model.entities.document;

import com.tpo.model.entities.supplier.Proveedor;

import java.util.Date;

/** Nota de Débito: incrementa la deuda del proveedor (igual que la Factura). */
public class NotaDeDebito extends DocumentoComercial {

    public NotaDeDebito() {
        super();
    }

    public NotaDeDebito(Proveedor proveedor, int numero, float importe, Date fecha) {
        setProveedor(proveedor);
        setNumero(numero);
        setImporteTotal(importe);
        setFechaEmision(fecha);
    }
}
