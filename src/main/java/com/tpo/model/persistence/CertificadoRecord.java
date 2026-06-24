package com.tpo.model.persistence;

/** Registro plano (JSON) de un Certificado de Exclusión de un proveedor. */
public class CertificadoRecord {
    public String tipoImpuesto;   // IVA / GANANCIAS / IIBB
    public long fechaDesde;       // epoch; 0 = sin fecha
    public long fechaHasta;       // epoch; 0 = sin fecha
    public int numeroCertificado;
}
