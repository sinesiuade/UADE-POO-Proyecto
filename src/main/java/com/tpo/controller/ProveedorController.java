package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.persistence.ProveedorRecord;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Controlador Singleton de Proveedores (catálogo). */
public class ProveedorController {

    private static final String ARCHIVO = "proveedores.json";

    private final List<Proveedor> proveedores = new ArrayList<>();

    private ProveedorController() {
        List<ProveedorRecord> registros = JsonStore.load(ARCHIVO, new TypeToken<List<ProveedorRecord>>() {}.getType());
        for (ProveedorRecord r : registros) {
            proveedores.add(fromRecord(r));
        }
    }

    private static ProveedorController INSTANCE;

    public static ProveedorController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProveedorController();
        }
        return INSTANCE;
    }

    public void agregarProveedor(Proveedor proveedor) {
        proveedores.add(proveedor);
        persistir();
    }

    public void editarProveedor(int index, Proveedor proveedor) {
        proveedores.set(index, proveedor);
        persistir();
    }

    public List<Proveedor> getProveedores() {
        return Collections.unmodifiableList(proveedores);
    }

    /** Indica si ya hay un proveedor con ese CUIT (o razón social si no hay CUIT), ignorando una posición. */
    public boolean existeProveedor(long cuit, String razonSocial, int ignorarIndex) {
        for (int i = 0; i < proveedores.size(); i++) {
            if (i == ignorarIndex) {
                continue;
            }
            Proveedor p = proveedores.get(i);
            if (cuit > 0 && p.getCuit() == cuit) {
                return true;
            }
            if (cuit <= 0 && razonSocial != null && razonSocial.equalsIgnoreCase(p.getRazonSocial())) {
                return true;
            }
        }
        return false;
    }

    private void persistir() {
        List<ProveedorRecord> registros = new ArrayList<>();
        for (Proveedor p : proveedores) {
            registros.add(toRecord(p));
        }
        JsonStore.save(ARCHIVO, registros);
    }

    private ProveedorRecord toRecord(Proveedor p) {
        ProveedorRecord r = new ProveedorRecord();
        r.cuit = p.getCuit();
        r.razonSocial = p.getRazonSocial();
        r.nombreComercial = p.getNombreComercial();
        r.domicilio = p.getDomicilio();
        r.telefono = p.getTelefono();
        r.email = p.getEmail();
        r.condicionImpositiva = p.getCondicionImpositiva() != null ? p.getCondicionImpositiva().name() : null;
        r.nroInscripcionFiscal = p.getNroInscripcionFiscal();
        r.fechaInicioAct = p.getFechaInicioAct() != null ? p.getFechaInicioAct().getTime() : 0;
        r.limiteDeudaAutorizado = p.getLimiteDeudaAutorizado();
        r.deudaActual = p.getDeudaActual();
        for (Rubro rubro : p.getRubros()) {
            if (rubro.getNombre() != null) {
                r.rubros.add(rubro.getNombre());
            }
        }
        return r;
    }

    private Proveedor fromRecord(ProveedorRecord r) {
        Proveedor p = new Proveedor();
        p.setCuit(r.cuit);
        p.setRazonSocial(r.razonSocial);
        p.setNombreComercial(r.nombreComercial);
        p.setDomicilio(r.domicilio);
        p.setTelefono(r.telefono);
        p.setEmail(r.email);
        if (r.condicionImpositiva != null) {
            p.setCondicionImpositiva(CondicionImpositiva.valueOf(r.condicionImpositiva));
        }
        p.setNroInscripcionFiscal(r.nroInscripcionFiscal);
        if (r.fechaInicioAct > 0) {
            p.setFechaInicioAct(new Date(r.fechaInicioAct));
        }
        p.setLimiteDeudaAutorizado(r.limiteDeudaAutorizado);
        p.setDeudaActual(r.deudaActual);
        if (r.rubros != null) {
            for (String nombre : r.rubros) {
                p.getRubros().add(new Rubro(nombre));
            }
        }
        return p;
    }
}
