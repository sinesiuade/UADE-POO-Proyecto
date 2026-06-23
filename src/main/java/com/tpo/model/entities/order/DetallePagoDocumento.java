package com.tpo.model.entities.order;

import com.tpo.model.entities.document.DocumentoComercial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Monto de la Orden de Pago imputado a un documento concreto (cancelación total o parcial). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePagoDocumento {

    private DocumentoComercial documento;
    private double montoAplicado;
}
