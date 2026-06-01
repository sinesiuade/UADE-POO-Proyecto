package com.tpo.model.entities.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class MedioDePago {

    private float importe;

    protected MedioDePago(float importe) {
        this.importe = importe;
    }

    public abstract double calcularDescuento();
}
