package com.tpo.model.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Efectivo extends MedioDePago {

    public Efectivo(float importe) {
        super(importe);
    }

    @Override
    public double calcularDescuento() {
        return 0.0;
    }
}
