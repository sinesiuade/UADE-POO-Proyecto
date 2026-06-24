package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.ItemProveedor;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.CertificadoDeExclusion;
import com.tpo.model.entities.tax.TipoImpuesto;
import com.tpo.model.persistence.CertificadoRecord;
import com.tpo.model.persistence.ItemProveedorRecord;
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

    /** Devuelve el proveedor con esa razón social, o null si no existe. */
    public Proveedor getProveedorPorRazonSocial(String razonSocial) {
        if (razonSocial == null) {
            return null;
        }
        for (Proveedor p : proveedores) {
            if (razonSocial.equals(p.getRazonSocial())) {
                return p;
            }
        }
        return null;
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
        for (ItemProveedor ip : p.getItemsProvistos()) {
            if (ip.getItemBase() != null) {
                ItemProveedorRecord ipr = new ItemProveedorRecord();
                ipr.codigoItem = ip.getItemBase().getCodigo();
                ipr.precio = ip.getPrecio();
                r.itemsProvistos.add(ipr);
            }
        }
        for (CertificadoDeExclusion c : p.getCertificadosDeExclusion()) {
            CertificadoRecord cr = new CertificadoRecord();
            cr.tipoImpuesto = c.getTipoImpuesto() != null ? c.getTipoImpuesto().name() : null;
            cr.fechaDesde = c.getFechaDesde() != null ? c.getFechaDesde().getTime() : 0;
            cr.fechaHasta = c.getFechaHasta() != null ? c.getFechaHasta().getTime() : 0;
            cr.numeroCertificado = c.getNumeroCertificado();
            r.certificados.add(cr);
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
        if (r.itemsProvistos != null) {
            for (ItemProveedorRecord ipr : r.itemsProvistos) {
                // Resolvemos la misma instancia del catálogo para que proveeItem() (compara por
                // referencia) funcione tras recargar.
                Item item = ItemController.getInstance().getItemPorCodigo(ipr.codigoItem);
                if (item != null) {
                    p.getItemsProvistos().add(new ItemProveedor(ipr.precio, item, p));
                }
            }
        }
        if (r.certificados != null) {
            for (CertificadoRecord cr : r.certificados) {
                CertificadoDeExclusion c = new CertificadoDeExclusion();
                if (cr.tipoImpuesto != null) {
                    c.setTipoImpuesto(TipoImpuesto.valueOf(cr.tipoImpuesto));
                }
                if (cr.fechaDesde > 0) {
                    c.setFechaDesde(new Date(cr.fechaDesde));
                }
                if (cr.fechaHasta > 0) {
                    c.setFechaHasta(new Date(cr.fechaHasta));
                }
                c.setNumeroCertificado(cr.numeroCertificado);
                p.getCertificadosDeExclusion().add(c);
            }
        }
        return p;
    }
}
