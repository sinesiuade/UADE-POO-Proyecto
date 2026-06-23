package com.tpo.controller;

import com.google.gson.reflect.TypeToken;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.document.Factura;
import com.tpo.model.entities.order.DetallePagoDocumento;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Controlador Singleton de Órdenes de Pago. */
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
        // Imputa el pago a las facturas pendientes del proveedor.
        if (proveedor != null) {
            FacturaController.getInstance().aplicarPago(proveedor.getRazonSocial(), dto.getTotalBrutoPagado());
        }
        persistir();
        return dto;
    }

    /** Regenera una OP y la reemplaza en la posición dada. */
    public OrdenDePagoDTO editarOrdenDePago(int index, Proveedor proveedor, List<DocumentoComercial> documentos, MedioDePago medio) {
        OrdenDePagoDTO dto = calcular(proveedor, documentos, medio);
        pagos.set(index, dto);
        persistir();
        return dto;
    }

    public List<OrdenDePagoDTO> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    /** Imputación de la OP a una factura concreta (documento + monto aplicado). */
    public static class ImputacionFactura {
        public final FacturaDTO factura;
        public final double monto;

        public ImputacionFactura(FacturaDTO factura, double monto) {
            this.factura = factura;
            this.monto = monto;
        }
    }

    /** Genera una OP imputando el monto indicado a cada factura seleccionada. */
    public OrdenDePagoDTO generarOrdenDePagoPorFacturas(Proveedor proveedor,
                                                        List<ImputacionFactura> imputaciones, MedioDePago medio) {
        OrdenDePagoDTO dto = calcularPorFacturas(proveedor, imputaciones, medio);
        pagos.add(dto);
        // Imputa cada monto a su factura (cancelación total o parcial por documento).
        for (ImputacionFactura imp : imputaciones) {
            FacturaController.getInstance().aplicarPagoExacto(imp.factura, imp.monto);
        }
        persistir();
        return dto;
    }

    /** Regenera una OP por facturas y la reemplaza (no re-imputa pagos para no duplicar saldos). */
    public OrdenDePagoDTO editarOrdenDePagoPorFacturas(int index, Proveedor proveedor,
                                                       List<ImputacionFactura> imputaciones, MedioDePago medio) {
        OrdenDePagoDTO dto = calcularPorFacturas(proveedor, imputaciones, medio);
        pagos.set(index, dto);
        persistir();
        return dto;
    }

    private OrdenDePagoDTO calcularPorFacturas(Proveedor proveedor,
                                               List<ImputacionFactura> imputaciones, MedioDePago medio) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<DetallePagoDocumento> detallePagos = new ArrayList<>();
        List<OrdenDePagoDTO.LineaPago> lineas = new ArrayList<>();

        for (ImputacionFactura imp : imputaciones) {
            // Documento de trabajo cuyo importe es el monto aplicado (para el cálculo de totales).
            Factura doc = new Factura(proveedor);
            doc.setImporteTotal((float) imp.monto);
            detallePagos.add(new DetallePagoDocumento(doc, imp.monto));

            String fecha = imp.factura.getFechaEmision() != null ? sdf.format(imp.factura.getFechaEmision()) : "";
            lineas.add(new OrdenDePagoDTO.LineaPago(
                    "Factura " + fecha + " (imp. " + String.format("%.2f", imp.factura.getImporteTotal()) + ")",
                    imp.monto));
        }

        OrdenDePago op = new OrdenDePago(proveedor, detallePagos, true);
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
        dto.setLineasPago(lineas);
        return dto;
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

    /** Etiqueta legible del medio de pago. */
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
        if (dto.getLineasPago() != null) {
            for (OrdenDePagoDTO.LineaPago lp : dto.getLineasPago()) {
                OpRecord.Linea linea = new OpRecord.Linea();
                linea.documento = lp.getDocumento();
                linea.montoAplicado = lp.getMontoAplicado();
                r.lineas.add(linea);
            }
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
        if (r.lineas != null) {
            for (OpRecord.Linea linea : r.lineas) {
                dto.getLineasPago().add(new OrdenDePagoDTO.LineaPago(linea.documento, linea.montoAplicado));
            }
        }
        return dto;
    }
}
