package com.tpo.model.persistence;

/** Registro plano (JSON) de una Factura. */
public class FacturaRecord {
    public String proveedor;
    public long fecha;
    public float importeTotal;
    public int cantidadDetalles;
    public boolean observada;
    public double montoPagado;
}
