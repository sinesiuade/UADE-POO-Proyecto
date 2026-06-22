package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.document.DetalleItemDocComercial;
import com.tpo.model.entities.document.Factura;
import com.tpo.model.entities.order.DetalleItemOC;
import com.tpo.model.entities.order.OrdenDeCompra;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.persistence.FacturaRecord;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Controlador Singleton de Facturas.
 * Implementa el Diagrama de Secuencia "Generación de Factura": valida el origen (OC previa o
 * autorización), arma el detalle a partir de las OC relacionadas verificando que el proveedor
 * pertenezca al rubro y provea el ítem, y aplica el control de precios (marca "Observada" si el
 * precio facturado difiere del precio acordado en la OC). Persiste en {@code data/facturas.json}.
 */
public class FacturaController {

    private static final String ARCHIVO = "facturas.json";

    private final List<FacturaDTO> facturas = new ArrayList<>();

    private FacturaController() {
        List<FacturaRecord> registros = JsonStore.load(ARCHIVO, new TypeToken<List<FacturaRecord>>() {}.getType());
        for (FacturaRecord r : registros) {
            facturas.add(fromRecord(r));
        }
    }

    private static FacturaController INSTANCE;

    public static FacturaController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FacturaController();
        }
        return INSTANCE;
    }

    /**
     * Genera una factura a partir de las órdenes de compra relacionadas.
     *
     * @param proveedor               proveedor emisor
     * @param ordenesRelacionadas     OC que amparan los conceptos facturados
     * @param autorizadoSupervisor    autorización del supervisor (requerida si no hay OC previa)
     * @param precioFacturadoUnitario precio unitario que llega en la factura; si es &gt; 0 se usa
     *                                para el control de precios contra el precio de la OC, si es
     *                                &le; 0 se toma el precio acordado de la OC (sin diferencia)
     */
    public FacturaDTO generarFactura(Proveedor proveedor, List<OrdenDeCompra> ordenesRelacionadas,
                                     boolean autorizadoSupervisor, double precioFacturadoUnitario) {
        FacturaDTO dto = armar(proveedor, ordenesRelacionadas, autorizadoSupervisor, precioFacturadoUnitario);
        facturas.add(dto);
        persistir();
        return dto;
    }

    /** Regenera una factura existente y la reemplaza en la posición dada. */
    public FacturaDTO editarFactura(int index, Proveedor proveedor, List<OrdenDeCompra> ordenesRelacionadas,
                                    boolean autorizadoSupervisor, double precioFacturadoUnitario) {
        FacturaDTO dto = armar(proveedor, ordenesRelacionadas, autorizadoSupervisor, precioFacturadoUnitario);
        facturas.set(index, dto);
        persistir();
        return dto;
    }

    public List<FacturaDTO> getFacturas() {
        return Collections.unmodifiableList(facturas);
    }

    private FacturaDTO armar(Proveedor proveedor, List<OrdenDeCompra> ordenesRelacionadas,
                             boolean autorizadoSupervisor, double precioFacturadoUnitario) {
        // Validación de origen: debe existir OC previa o autorización de un supervisor.
        boolean existeOC = ordenesRelacionadas != null && !ordenesRelacionadas.isEmpty();
        if (!existeOC && !autorizadoSupervisor) {
            throw new IllegalStateException("No existen OC previas: se requiere autorización de un supervisor.");
        }

        Factura factura = new Factura(proveedor);

        if (existeOC) {
            for (OrdenDeCompra oc : ordenesRelacionadas) {
                for (DetalleItemOC detalleOC : oc.getDetalleItems()) {
                    DetalleItemDocComercial datos = detalleOC.getDatosParaItemFactura();
                    Item item = detalleOC.getItem();
                    Rubro rubro = item != null ? item.getRubro() : null;

                    // Coherencia de conceptos: el proveedor pertenece al rubro y provee el ítem.
                    if (proveedor.perteneceARubro(rubro) && proveedor.proveeItem(item)) {
                        double precioFacturado = precioFacturadoUnitario > 0
                                ? precioFacturadoUnitario
                                : detalleOC.getPrecioAcordado();

                        factura.crearDetalle(datos.getConcepto(), datos.getCantidad(), precioFacturado, datos.getIva());

                        // Control de precios: si difiere del acordado, la factura queda Observada.
                        if (precioFacturado != detalleOC.getPrecioAcordado()) {
                            factura.setObservada(true);
                        }
                    }
                }
            }
        }

        return new FacturaDTO(
                factura.getProveedor(), factura.getFechaEmision(),
                factura.getTotalBruto(), factura.getDetalleItems().size(), factura.isObservada(), 0.0);
    }

    /**
     * Aplica un pago a las facturas pendientes del proveedor (las más antiguas primero),
     * actualizando su monto pagado. Lo invoca el flujo de Orden de Pago para mantener
     * la cuenta corriente y el estado de cancelación de cada documento.
     */
    public void aplicarPago(String proveedor, double monto) {
        if (proveedor == null || monto <= 0) {
            return;
        }
        double restante = monto;
        List<FacturaDTO> pendientes = new ArrayList<>();
        for (FacturaDTO f : facturas) {
            if (f.getProveedor() != null && proveedor.equals(f.getProveedor().getRazonSocial()) && f.getSaldo() > 0) {
                pendientes.add(f);
            }
        }
        pendientes.sort(java.util.Comparator.comparing(FacturaDTO::getFechaEmision,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        for (FacturaDTO f : pendientes) {
            if (restante <= 0) {
                break;
            }
            double aplicar = Math.min(f.getSaldo(), restante);
            f.setMontoPagado(f.getMontoPagado() + aplicar);
            restante -= aplicar;
        }
        persistir();
    }

    private void persistir() {
        List<FacturaRecord> registros = new ArrayList<>();
        for (FacturaDTO dto : facturas) {
            registros.add(toRecord(dto));
        }
        JsonStore.save(ARCHIVO, registros);
    }

    private FacturaRecord toRecord(FacturaDTO dto) {
        FacturaRecord r = new FacturaRecord();
        r.proveedor = dto.getProveedor() != null ? dto.getProveedor().getRazonSocial() : null;
        r.fecha = dto.getFechaEmision() != null ? dto.getFechaEmision().getTime() : 0;
        r.importeTotal = dto.getImporteTotal();
        r.cantidadDetalles = dto.getCantidadDetalles();
        r.observada = dto.isObservada();
        r.montoPagado = dto.getMontoPagado();
        return r;
    }

    private FacturaDTO fromRecord(FacturaRecord r) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(r.proveedor);
        return new FacturaDTO(p, new Date(r.fecha), r.importeTotal, r.cantidadDetalles, r.observada, r.montoPagado);
    }
}
