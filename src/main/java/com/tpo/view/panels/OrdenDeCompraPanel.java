package com.tpo.view.panels;

import com.tpo.controller.OrdenDeCompraController;
import com.tpo.model.dto.OrdenDeCompraDTO;
import com.tpo.view.dialogs.OrdenDeCompraDialog;

import java.text.SimpleDateFormat;
import java.util.List;

public class OrdenDeCompraPanel extends BaseListPanel {

    private final OrdenDeCompraController controller;

    public OrdenDeCompraPanel() {
        super("+ Nueva Orden de Compra", new String[]{"Número", "Fecha", "Proveedor", "Estado"});
        controller = OrdenDeCompraController.getInstance();
        refrescar();
    }

    @Override
    protected void abrirCreacion() {
        new OrdenDeCompraDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<OrdenDeCompraDTO> ordenes = controller.getOrdenes();
        if (row < 0 || row >= ordenes.size()) return;
        new OrdenDeCompraDialog(getParentFrame(), ordenes.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (OrdenDeCompraDTO dto : controller.getOrdenes()) {
            String fecha = dto.getFecha() != null ? sdf.format(dto.getFecha()) : "";
            String proveedor = dto.getProveedor() != null ? dto.getProveedor().getRazonSocial() : "";
            String estado = dto.getEstado() != null ? dto.getEstado().toString() : "";
            tableModel.addRow(new Object[]{dto.getNumero(), fecha, proveedor, estado});
        }
    }
}
