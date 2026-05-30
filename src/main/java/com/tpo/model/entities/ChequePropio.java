package com.tpo.model.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ChequePropio extends MedioDePago {

    private float numeroCheque;
    private String banco;
    private Date fechaEmision;
    private Date fechaVencimiento;
    private String firmante;

    public ChequePropio(float importe, float numeroCheque, String banco, Date fechaEmision,
                        Date fechaVencimiento, String firmante) {
        super(importe);
        this.numeroCheque = numeroCheque;
        this.banco = banco;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.firmante = firmante;
    }

    @Override
    public double calcularDescuento() {
        return 0.0;
    }
}
