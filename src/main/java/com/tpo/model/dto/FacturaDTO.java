package com.tpo.model.dto;

import com.tpo.model.Enums.EstadoCancelacionDocumentoComercial;
import com.tpo.model.entities.supplier.Proveedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FacturaDTO {
    private Proveedor proveedor;
    private Date fechaEmision;
    private float importeTotal;
    private int cantidadDetalles;
    private boolean observada;
    private double montoPagado;
    /** Desglose del IVA por alícuota (para el Libro IVA Compras). */
    private List<IvaAlicuota> ivaPorAlicuota = new ArrayList<>();

    public FacturaDTO(Proveedor proveedor, Date fechaEmision, float importeTotal,
                      int cantidadDetalles, boolean observada, double montoPagado) {
        this.proveedor = proveedor;
        this.fechaEmision = fechaEmision;
        this.importeTotal = importeTotal;
        this.cantidadDetalles = cantidadDetalles;
        this.observada = observada;
        this.montoPagado = montoPagado;
    }

    /** Saldo pendiente del documento. */
    public double getSaldo() {
        return importeTotal - montoPagado;
    }

    /** Estado de cancelación según el monto pagado. */
    public EstadoCancelacionDocumentoComercial getEstadoCancelacion() {
        if (montoPagado <= 0) {
            return EstadoCancelacionDocumentoComercial.PENDIENTE;
        }
        if (getSaldo() <= 0.0001) {
            return EstadoCancelacionDocumentoComercial.CANCELADO;
        }
        return EstadoCancelacionDocumentoComercial.PARCIAL;
    }

    /** Neto gravado e IVA acumulados para una alícuota concreta. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IvaAlicuota {
        private double alicuota;
        private double neto;
        private double iva;
    }
}
