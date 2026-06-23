package com.tpo.model.persistence;

/** Registro plano (JSON) de un Ítem del catálogo (Producto o Servicio). */
public class ItemRecord {
    public String tipo;            // "Producto" / "Servicio"
    public int codigo;
    public String descripcion;
    public String rubro;
    public String unidadMedida;
    public double precioUnitario;
    public double porcentajeIva;

    // Producto
    public String lote;
    public long vencimiento;       // epoch; 0 = sin fecha
    public String stock;

    // Servicio
    public String tipoDePrestacion;
}
