package com.tpo.model.entities.tax;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDeExclusion {

    private TipoImpuesto tipoImpuesto;
    private Date fechaDesde;
    private Date fechaHasta;
    private int numeroCertificado;

    /** El certificado excluye al impuesto indicado. */
    public boolean aplicaA(TipoImpuesto tipo) {
        return this.tipoImpuesto == tipo;
    }

    /** El certificado está vigente si la fecha dada está dentro del rango [desde, hasta]. */
    public boolean estaVigente(Date fecha) {
        if (fechaDesde == null || fechaHasta == null || fecha == null) {
            return false;
        }
        return !fecha.before(fechaDesde) && !fecha.after(fechaHasta);
    }
}
