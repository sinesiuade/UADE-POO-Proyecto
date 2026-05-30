package com.tpo.model.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrdenDePago {

    private Proveedor proveedor;
    private Date fechaEmision;
    private List<DocumentoComercial> documentosAsociados = new ArrayList<>();
    private float totalBrutoPagado;
    private float totalRetenido;
    private float totalNetoAPagar;

    public OrdenDePago(Proveedor proveedor, List<DocumentoComercial> listaDocumentos) {
        this.proveedor = proveedor;
        this.documentosAsociados = listaDocumentos != null ? listaDocumentos : new ArrayList<>();
        this.fechaEmision = new Date();
        this.totalBrutoPagado = calcularTotalBrutoPagado();
    }

    public float calcularTotales(List<Impuesto> listaImpuestos) {
        this.totalBrutoPagado = calcularTotalBrutoPagado();
        this.totalNetoAPagar = totalBrutoPagado - totalRetenido;
        return totalNetoAPagar;
    }

    public float calcularTotalBrutoPagado() {
        return (float) documentosAsociados.stream()
                .mapToDouble(DocumentoComercial::getImporteTotal)
                .sum();
    }

    public double sumarRetencion(double montoRetencion) {
        this.totalRetenido += montoRetencion;
        return totalRetenido;
    }
}
