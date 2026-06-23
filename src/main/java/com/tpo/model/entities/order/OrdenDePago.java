package com.tpo.model.entities.order;

import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.payment.MedioDePago;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.Impuesto;
import com.tpo.model.entities.tax.TipoImpuesto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class OrdenDePago {

    private Proveedor proveedor;
    private Date fechaEmision;
    private List<DocumentoComercial> documentosAsociados = new ArrayList<>();
    /** Documentos asociados con el monto que se aplica a cada uno (cancelación total o parcial). */
    private List<DetallePagoDocumento> detallePagos = new ArrayList<>();
    private float totalBrutoPagado;
    private float totalRetenido;
    private float totalNetoAPagar;
    /** Retención discriminada por impuesto. */
    private Map<TipoImpuesto, Double> retencionesPorImpuesto = new LinkedHashMap<>();
    /** Medios de pago de la OP (polimorfismo). */
    private List<MedioDePago> mediosDePago = new ArrayList<>();

    public OrdenDePago(Proveedor proveedor, List<DocumentoComercial> listaDocumentos) {
        this.proveedor = proveedor;
        this.documentosAsociados = listaDocumentos != null ? listaDocumentos : new ArrayList<>();
        this.fechaEmision = new Date();
        this.totalBrutoPagado = calcularTotalBrutoPagado();
    }

    /** Construye una OP a partir del detalle de montos aplicados por documento. */
    public OrdenDePago(Proveedor proveedor, List<DetallePagoDocumento> detallePagos, boolean porDetalle) {
        this.proveedor = proveedor;
        this.detallePagos = detallePagos != null ? detallePagos : new ArrayList<>();
        for (DetallePagoDocumento detalle : this.detallePagos) {
            if (detalle.getDocumento() != null) {
                this.documentosAsociados.add(detalle.getDocumento());
            }
        }
        this.fechaEmision = new Date();
        this.totalBrutoPagado = calcularTotalBrutoPagado();
    }

    /** Calcula bruto, retenciones por impuesto y neto = bruto - retenido. */
    public float calcularTotales(List<Impuesto> listaImpuestos) {
        this.totalBrutoPagado = calcularTotalBrutoPagado();
        this.totalRetenido = 0;
        this.retencionesPorImpuesto.clear();

        if (listaImpuestos != null) {
            for (Impuesto impuesto : listaImpuestos) {
                double montoRetencion = impuesto.calcularRetencion(proveedor, totalBrutoPagado);
                sumarRetencion(montoRetencion);
                if (montoRetencion > 0) {
                    retencionesPorImpuesto.merge(impuesto.getNombre(), montoRetencion, Double::sum);
                }
            }
        }

        this.totalNetoAPagar = totalBrutoPagado - totalRetenido;
        return totalNetoAPagar;
    }

    public float calcularTotalBrutoPagado() {
        // Si hay montos aplicados por documento, el bruto es su suma (permite cancelación parcial).
        if (detallePagos != null && !detallePagos.isEmpty()) {
            return (float) detallePagos.stream()
                    .mapToDouble(DetallePagoDocumento::getMontoAplicado)
                    .sum();
        }
        return (float) documentosAsociados.stream()
                .mapToDouble(DocumentoComercial::getImporteTotal)
                .sum();
    }

    public double sumarRetencion(double montoRetencion) {
        this.totalRetenido += montoRetencion;
        return totalRetenido;
    }

    public void agregarMedioDePago(MedioDePago medio) {
        if (medio != null) {
            mediosDePago.add(medio);
        }
    }
}
