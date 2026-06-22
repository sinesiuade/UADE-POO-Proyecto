package com.tpo.model.entities.document;

import com.tpo.model.entities.supplier.Proveedor;

import java.util.Date;

/** Nota de Crédito: disminuye la deuda del proveedor (impacto negativo en cuenta corriente). */
public class NotaDeCredito extends DocumentoComercial {

    public NotaDeCredito() {
        super();
    }

    public NotaDeCredito(Proveedor proveedor, int numero, float importe, Date fecha) {
        setProveedor(proveedor);
        setNumero(numero);
        setImporteTotal(importe);
        setFechaEmision(fecha);
    }

    @Override
    public double getImpactoCuentaCorriente() {
        return -getImporteTotal();
    }
}
