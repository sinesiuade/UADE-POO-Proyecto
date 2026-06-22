package com.tpo.model.dto;

import com.tpo.model.entities.supplier.Proveedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/** Nota de Crédito o Débito recibida. El tipo define el impacto en la cuenta corriente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotaDTO {
    public static final String CREDITO = "Nota de Crédito";
    public static final String DEBITO = "Nota de Débito";

    private Proveedor proveedor;
    private Date fechaEmision;
    private int numero;
    private float importeTotal;
    private String tipo;

    /** Impacto en la deuda: la Nota de Crédito resta, la de Débito suma. */
    public double getImpactoDeuda() {
        return CREDITO.equals(tipo) ? -importeTotal : importeTotal;
    }
}
