package com.tpo.model.entities.document;

import com.tpo.model.Enums.EstadoCancelacionDocumentoComercial;
import com.tpo.model.entities.supplier.Proveedor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class DocumentoComercial {

    private int numero;
    private float importeTotal;
    private Date fechaEmision;
    private Proveedor proveedor;
    private EstadoCancelacionDocumentoComercial estadoCancelacion;
    private List<DetalleItemDocComercial> detalleItems = new ArrayList<>();
    /**
     * Marca el documento como "Observado" (ver consigna: diferencia de precios con la OC).
     * Es una dimensión distinta del estado de cancelación, por eso se modela aparte.
     */
    private boolean observada;

    protected DocumentoComercial(int numero, float importeTotal, Date fechaEmision,
                                 Proveedor proveedor, EstadoCancelacionDocumentoComercial estadoCancelacion) {
        this.numero = numero;
        this.importeTotal = importeTotal;
        this.fechaEmision = fechaEmision;
        this.proveedor = proveedor;
        this.estadoCancelacion = estadoCancelacion;
    }

    public void agregarDetalle(DetalleItemDocComercial detalle) {
        detalleItems.add(detalle);
        recalcularImporteTotal();
    }

    public float recalcularImporteTotal() {
        double total = detalleItems.stream()
                .mapToDouble(d -> d.getCantidad() * d.getPrecio())
                .sum();
        this.importeTotal = (float) total;
        return importeTotal;
    }

    /** Total bruto del documento (suma de los subtotales de sus detalles). */
    public float getTotalBruto() {
        return recalcularImporteTotal();
    }
}
