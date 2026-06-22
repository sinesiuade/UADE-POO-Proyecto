package com.tpo.model.dto;

import com.tpo.model.Enums.EstadoOrdenDeCompra;
import com.tpo.model.entities.order.DetalleItemOC;
import com.tpo.model.entities.supplier.Proveedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDeCompraDTO {
    private int numero;
    private Date fecha;
    private Proveedor proveedor;
    private List<DetalleItemOC> detalleItems = new ArrayList<>();
    private double totalBruto;
    private EstadoOrdenDeCompra estado;

    public OrdenDeCompraDTO(int numero, Date fecha, Proveedor proveedor, EstadoOrdenDeCompra estado) {
        this.numero = numero;
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.estado = estado;
    }
}
