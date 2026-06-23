package com.tpo.model.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registro plano (JSON) de una Orden de Pago. */
public class OpRecord {
    public String proveedor;
    public String condicion;
    public long fecha;
    public float bruto;
    public float retenido;
    public float neto;
    public String medioDePago;
    public Map<String, Double> retenciones = new LinkedHashMap<>();
    public List<Linea> lineas = new ArrayList<>();

    /** Monto aplicado a un documento dentro de la OP. */
    public static class Linea {
        public String documento;
        public double montoAplicado;
    }
}
