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
public class OrdenDeCompra {

    private int numero;
    private Date fecha;
    private Proveedor proveedor;
    private List<DetalleItemOC> detalleItems = new ArrayList<>();
    private double totalBruto;
    private EstadoOrdenDeCompra estado;

    public OrdenDeCompra(int numero, Date fecha, Proveedor proveedor, EstadoOrdenDeCompra estado) {
        this.numero = numero;
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.estado = estado;
    }

    public boolean verificarLimiteDeCredito() {
        if (proveedor == null) {
            return false;
        }
        return proveedor.getDeudaActual() + totalBruto <= proveedor.getLimiteDeudaAutorizado();
    }

    public void agregarItem(Item item, int cantidad, double precioAcordado) {
        detalleItems.add(new DetalleItemOC(item, cantidad, precioAcordado));
        sumarBruto(cantidad * precioAcordado);
    }

    public void confirmarOrdenDeCompra() {
        setEstado(EstadoOrdenDeCompra.CONFIRMADA);
    }

    public void sumarBruto(double precio) {
        this.totalBruto += precio;
    }
}
