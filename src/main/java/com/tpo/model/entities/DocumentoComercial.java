package com.tpo.model.entities;

import com.tpo.model.Enums.EstadoCancelacionDocumentoComercial;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public abstract class DocumentoComercial {

    private int numero;
    private float importeTotal;
    private Date fechaEmision;
    private Proveedor proveedor;
    private EstadoCancelacionDocumentoComercial estadoCancelacion;

    protected DocumentoComercial(int numero, float importeTotal, Date fechaEmision,
                                 Proveedor proveedor, EstadoCancelacionDocumentoComercial estadoCancelacion) {
        this.numero = numero;
        this.importeTotal = importeTotal;
        this.fechaEmision = fechaEmision;
        this.proveedor = proveedor;
        this.estadoCancelacion = estadoCancelacion;
    }

    public void checkearRubro() {
    }
}
