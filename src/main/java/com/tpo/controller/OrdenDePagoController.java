package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.order.OrdenDePago;
import com.tpo.model.entities.payment.ChequePropio;
import com.tpo.model.entities.payment.ChequeTerceros;
import com.tpo.model.entities.payment.Efectivo;
import com.tpo.model.entities.payment.MedioDePago;
import com.tpo.model.entities.payment.Transferencia;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.TipoImpuesto;
import com.tpo.model.persistence.OpRecord;
import com.tpo.util.JsonStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Controlador Singleton de Órdenes de Pago.
 * Implementa el Diagrama de Secuencia "Generación de Orden de Pago": crea la OP, calcula
 * sus totales recorriendo los impuestos del sistema (retenciones) y persiste su DTO.
 * Persiste en {@code data/ordenes_pago.json}.
 */
public class OrdenDePagoController {

    private static final String ARCHIVO = "ordenes_pago.json";

    private final List<OrdenDePagoDTO> pagos = new ArrayList<>();

    private OrdenDePagoController() {
        List<OpRecord> registros = JsonStore.load(ARCHIVO, new TypeToken<List<OpRecord>>() {}.getType());
        for (OpRecord r : registros) {
            pagos.add(fromRecord(r));
        }
    }

    private static OrdenDePagoController INSTANCE;

    public static OrdenDePagoController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrdenDePagoController();
        }
        return INSTANCE;
    }

    public OrdenDePagoDTO generarOrdenDePago(Proveedor proveedor, List<DocumentoComercial> documentos) {
        return generarOrdenDePago(proveedor, documentos, null);
    }

    public OrdenDePagoDTO generarOrdenDePago(Proveedor proveedor, List<DocumentoComercial> documentos, MedioDePago medio) {
        OrdenDePagoDTO dto = calcular(proveedor, documentos, medio);
        pagos.add(dto);
        // Imputa el pago a las facturas pendientes del proveedor (actualiza su estado de cancelación).
        if (proveedor != null) {
            FacturaController.getInstance().aplicarPago(proveedor.getRazonSocial(), dto.getTotalBrutoPagado());
        }
        persistir();
        return dto;
    }

    /** Regenera una OP existente (recalcula retenciones) y la reemplaza en la posición dada. */
    public OrdenDePagoDTO editarOrdenDePago(int index, Proveedor proveedor, List<DocumentoComercial> documentos, MedioDePago medio) {
        OrdenDePagoDTO dto = calcular(proveedor, documentos, medio);
        pagos.set(index, dto);
        persistir();
        return dto;
    }

    public List<OrdenDePagoDTO> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    private OrdenDePagoDTO calcular(Proveedor proveedor, List<DocumentoComercial> documentos, MedioDePago medio) {
        OrdenDePago op = new OrdenDePago(proveedor, documentos);
        op.calcularTotales(ImpuestoController.getInstance().getImpuestos());

        String medioNombre = null;
        if (medio != null) {
            medio.setImporte(op.getTotalNetoAPagar()); // se paga el neto
            op.agregarMedioDePago(medio);
            medioNombre = etiquetaMedio(medio);
        }

        OrdenDePagoDTO dto = new OrdenDePagoDTO(
                op.getProveedor(), op.getFechaEmision(),
                op.getTotalBrutoPagado(), op.getTotalRetenido(), op.getTotalNetoAPagar(),
                op.getRetencionesPorImpuesto(), medioNombre);
        return dto;
    }

    /** Etiqueta legible del medio de pago (polimorfismo) para mostrar y agrupar en consultas. */
    static String etiquetaMedio(MedioDePago medio) {
        if (medio instanceof Efectivo) return "Efectivo";
        if (medio instanceof Transferencia) return "Transferencia";
        if (medio instanceof ChequePropio) return "Cheque Propio";
        if (medio instanceof ChequeTerceros) return "Cheque de Terceros";
        return "(sin medio)";
    }

    private void persistir() {
        List<OpRecord> registros = new ArrayList<>();
        for (OrdenDePagoDTO dto : pagos) {
            registros.add(toRecord(dto));
        }
        JsonStore.save(ARCHIVO, registros);
    }

    private OpRecord toRecord(OrdenDePagoDTO dto) {
        OpRecord r = new OpRecord();
        Proveedor p = dto.getProveedor();
        r.proveedor = p != null ? p.getRazonSocial() : null;
        r.condicion = p != null && p.getCondicionImpositiva() != null ? p.getCondicionImpositiva().name() : null;
        r.fecha = dto.getFechaEmision() != null ? dto.getFechaEmision().getTime() : 0;
        r.bruto = dto.getTotalBrutoPagado();
        r.retenido = dto.getTotalRetenido();
        r.neto = dto.getTotalNetoAPagar();
        r.medioDePago = dto.getMedioDePago();
        if (dto.getRetencionesPorImpuesto() != null) {
            dto.getRetencionesPorImpuesto().forEach((tipo, monto) -> r.retenciones.put(tipo.name(), monto));
        }
        return r;
    }

    private OrdenDePagoDTO fromRecord(OpRecord r) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(r.proveedor);
        if (r.condicion != null) {
            p.setCondicionImpositiva(CondicionImpositiva.valueOf(r.condicion));
        }
        OrdenDePagoDTO dto = new OrdenDePagoDTO(p, new Date(r.fecha), r.bruto, r.retenido, r.neto, null, r.medioDePago);
        Map<TipoImpuesto, Double> retenciones = new java.util.LinkedHashMap<>();
        if (r.retenciones != null) {
            r.retenciones.forEach((tipo, monto) -> retenciones.put(TipoImpuesto.valueOf(tipo), monto));
        }
        dto.setRetencionesPorImpuesto(retenciones);
        return dto;
    }
}
