package com.tpo.model.persistence;

import java.util.ArrayList;
import java.util.List;

/** Registro plano (JSON) de un Proveedor. */
public class ProveedorRecord {
    public long cuit;
    public String razonSocial;
    public String nombreComercial;
    public String domicilio;
    public long telefono;
    public String email;
    public String condicionImpositiva;
    public long nroInscripcionFiscal;
    public long fechaInicioAct;
    public int limiteDeudaAutorizado;
    public double deudaActual;
    public List<String> rubros = new ArrayList<>();
    public List<ItemProveedorRecord> itemsProvistos = new ArrayList<>();
    public List<CertificadoRecord> certificados = new ArrayList<>();
}
