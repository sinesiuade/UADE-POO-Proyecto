package com.tpo.model.dto;

import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.TipoImpuesto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDePagoDTO {
    private Proveedor proveedor;
    private Date fechaEmision;
    private float totalBrutoPagado;
    private float totalRetenido;
    private float totalNetoAPagar;
    private Map<TipoImpuesto, Double> retencionesPorImpuesto = new LinkedHashMap<>();
    private String medioDePago;
}
