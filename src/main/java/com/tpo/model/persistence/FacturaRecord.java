package com.tpo.model.persistence;

import java.util.ArrayList;
import java.util.List;

/** Registro plano (JSON) de una Factura. */
public class FacturaRecord {
    public String proveedor;
    public long fecha;
    public float importeTotal;
    public int cantidadDetalles;
    public boolean observada;
    public double montoPagado;
    public List<IvaLinea> ivaPorAlicuota = new ArrayList<>();

    /** Neto gravado e IVA por alícuota (para el Libro IVA Compras). */
    public static class IvaLinea {
        public double alicuota;
        public double neto;
        public double iva;
    }
}
