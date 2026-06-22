package com.tpo.model.dto;

import com.tpo.model.Enums.EstadoCancelacionDocumentoComercial;
import com.tpo.model.entities.supplier.Proveedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDTO {
    private Proveedor proveedor;
    private Date fechaEmision;
    private float importeTotal;
    private int cantidadDetalles;
    private boolean observada;
    private double montoPagado;

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
}
