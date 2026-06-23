package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.Enums.EstadoOrdenDeCompra;
import com.tpo.model.dto.OrdenDeCompraDTO;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.order.DetalleItemOC;
import com.tpo.model.entities.order.OrdenDeCompra;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.persistence.OcRecord;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Controlador Singleton de Órdenes de Compra. */
public class OrdenDeCompraController {

    private static final String ARCHIVO = "ordenes_compra.json";

    private final List<OrdenDeCompraDTO> ordenes = new ArrayList<>();

    private OrdenDeCompraController() {
        List<OcRecord> registros = JsonStore.load(ARCHIVO, new TypeToken<List<OcRecord>>() {}.getType());
        for (OcRecord r : registros) {
            ordenes.add(fromRecord(r));
        }
    }

    private static OrdenDeCompraController INSTANCE;

    public static OrdenDeCompraController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrdenDeCompraController();
        }
        return INSTANCE;
    }

    /** Crea la OC validando el límite de crédito. */
    public OrdenDeCompraDTO crearOrdenDeCompra(OrdenDeCompra oc) {
        OrdenDeCompraDTO dto = validarYMapear(oc);
        ordenes.add(dto);
        persistir();
        return dto;
    }

    /** Reemplaza una OC existente recalculando su estado. */
    public OrdenDeCompraDTO editarOrdenDeCompra(int index, OrdenDeCompra oc) {
        OrdenDeCompraDTO dto = validarYMapear(oc);
        ordenes.set(index, dto);
        persistir();
        return dto;
    }

    public List<OrdenDeCompraDTO> getOrdenes() {
        return Collections.unmodifiableList(ordenes);
    }

    /** Aprobación de supervisor: pasa una OC en Pendiente de Aprobación a Emitida. */
    public OrdenDeCompraDTO aprobarOrdenDeCompra(int index) {
        if (index < 0 || index >= ordenes.size()) {
            throw new IllegalArgumentException("Orden de compra inexistente.");
        }
        OrdenDeCompraDTO dto = ordenes.get(index);
        if (dto.getEstado() != EstadoOrdenDeCompra.PENDIENTE_APROBACION) {
            throw new IllegalStateException("Solo se pueden aprobar órdenes en estado Pendiente de Aprobación.");
        }
        dto.setEstado(EstadoOrdenDeCompra.EMITIDA);
        persistir();
        return dto;
    }

    /** Recalcula el bruto, define el estado según el límite y mapea a DTO. */
    private OrdenDeCompraDTO validarYMapear(OrdenDeCompra oc) {
        oc.recalcularTotalBruto();
        boolean dentroDelLimite = oc.verificarLimiteDeCredito();
        oc.setEstado(dentroDelLimite ? EstadoOrdenDeCompra.EMITIDA : EstadoOrdenDeCompra.PENDIENTE_APROBACION);

        OrdenDeCompraDTO dto = new OrdenDeCompraDTO(
                oc.getNumero(), oc.getFecha(), oc.getProveedor(), oc.getEstado());
        dto.setDetalleItems(oc.getDetalleItems());
        dto.setTotalBruto(oc.getTotalBruto());
        return dto;
    }

    private void persistir() {
        List<OcRecord> registros = new ArrayList<>();
        for (OrdenDeCompraDTO dto : ordenes) {
            registros.add(toRecord(dto));
        }
        JsonStore.save(ARCHIVO, registros);
    }

    private OcRecord toRecord(OrdenDeCompraDTO dto) {
        OcRecord r = new OcRecord();
        r.numero = dto.getNumero();
        r.fecha = dto.getFecha() != null ? dto.getFecha().getTime() : 0;
        Proveedor p = dto.getProveedor();
        r.proveedor = p != null ? p.getRazonSocial() : null;
        r.limite = p != null ? p.getLimiteDeudaAutorizado() : 0;
        r.deuda = p != null ? p.getDeudaActual() : 0;
        r.totalBruto = dto.getTotalBruto();
        r.estado = dto.getEstado() != null ? dto.getEstado().name() : null;
        if (dto.getDetalleItems() != null) {
            for (DetalleItemOC d : dto.getDetalleItems()) {
                OcRecord.Linea linea = new OcRecord.Linea();
                linea.descripcion = d.getItem() != null ? d.getItem().getDescripcion() : null;
                linea.rubro = d.getItem() != null && d.getItem().getRubro() != null ? d.getItem().getRubro().getNombre() : null;
                linea.cantidad = d.getCantidad();
                linea.precioAcordado = d.getPrecioAcordado();
                r.lineas.add(linea);
            }
        }
        return r;
    }

    private OrdenDeCompraDTO fromRecord(OcRecord r) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(r.proveedor);
        p.setLimiteDeudaAutorizado(r.limite);
        p.setDeudaActual(r.deuda);

        EstadoOrdenDeCompra estado = r.estado != null ? EstadoOrdenDeCompra.valueOf(r.estado) : null;
        OrdenDeCompraDTO dto = new OrdenDeCompraDTO(r.numero, new Date(r.fecha), p, estado);
        dto.setTotalBruto(r.totalBruto);

        List<DetalleItemOC> detalles = new ArrayList<>();
        for (OcRecord.Linea linea : r.lineas) {
            Producto item = new Producto();
            item.setDescripcion(linea.descripcion);
            if (linea.rubro != null) {
                item.setRubro(new Rubro(linea.rubro));
            }
            detalles.add(new DetalleItemOC(item, linea.cantidad, linea.precioAcordado));
        }
        dto.setDetalleItems(detalles);
        return dto;
    }
}
