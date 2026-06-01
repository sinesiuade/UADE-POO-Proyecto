package com.tpo.model.entities.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ChequeTerceros extends MedioDePago {

    private float numeroCheque;
    private String banco;
    private Date fechaEmision;
    private Date fechaVencimiento;
    private String firmanteOriginal;

    public ChequeTerceros(float importe, float numeroCheque, String banco, Date fechaEmision,
                          Date fechaVencimiento, String firmanteOriginal) {
        super(importe);
        this.numeroCheque = numeroCheque;
        this.banco = banco;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.firmanteOriginal = firmanteOriginal;
    }

    @Override
    public double calcularDescuento() {
        return 0.0;
    }
}
