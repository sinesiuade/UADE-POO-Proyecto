package com.tpo.model.persistence;

import java.util.ArrayList;
import java.util.List;

/** Registro plano (JSON) de una Orden de Compra. */
public class OcRecord {
    public int numero;
    public long fecha;
    public String proveedor;
    public int limite;
    public double deuda;
    public double totalBruto;
    public String estado;
    public List<Linea> lineas = new ArrayList<>();

    public static class Linea {
        public String descripcion;
        public String rubro;
        public int cantidad;
        public double precioAcordado;
    }
}
