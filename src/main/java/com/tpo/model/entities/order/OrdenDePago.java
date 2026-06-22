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
