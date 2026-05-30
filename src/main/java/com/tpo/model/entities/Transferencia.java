package com.tpo.model.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Transferencia extends MedioDePago {

    private float numeroDeReferencia;
    private String cuentaDeOrigen;

    public Transferencia(float importe, float numeroDeReferencia, String cuentaDeOrigen) {
        super(importe);
        this.numeroDeReferencia = numeroDeReferencia;
        this.cuentaDeOrigen = cuentaDeOrigen;
    }

    @Override
    public double calcularDescuento() {
        return 0.0;
    }
}
