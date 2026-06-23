package com.tpo.model.dto;

import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.TipoImpuesto;
import lombok.AllArgsConstructor;
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
public class OrdenDePagoDTO {
    private Proveedor proveedor;
    private Date fechaEmision;
    private float totalBrutoPagado;
    private float totalRetenido;
    private float totalNetoAPagar;
    private Map<TipoImpuesto, Double> retencionesPorImpuesto = new LinkedHashMap<>();
    private String medioDePago;
    /** Desglose: monto aplicado a cada documento cancelado. */
    private List<LineaPago> lineasPago = new ArrayList<>();

    public OrdenDePagoDTO(Proveedor proveedor, Date fechaEmision, float totalBrutoPagado,
                          float totalRetenido, float totalNetoAPagar,
                          Map<TipoImpuesto, Double> retencionesPorImpuesto, String medioDePago) {
        this.proveedor = proveedor;
        this.fechaEmision = fechaEmision;
        this.totalBrutoPagado = totalBrutoPagado;
        this.totalRetenido = totalRetenido;
        this.totalNetoAPagar = totalNetoAPagar;
        this.retencionesPorImpuesto = retencionesPorImpuesto != null ? retencionesPorImpuesto : new LinkedHashMap<>();
        this.medioDePago = medioDePago;
    }

    /** Monto aplicado a un documento dentro de la OP. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaPago {
        private String documento;
        private double montoAplicado;
    }
}
