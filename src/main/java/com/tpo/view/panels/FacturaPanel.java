package com.tpo.view.panels;

import com.tpo.controller.FacturaController;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.view.dialogs.FacturaDialog;

import java.text.SimpleDateFormat;
import java.util.List;

public class FacturaPanel extends BaseListPanel {

    private final FacturaController controller;

    public FacturaPanel() {
        super("+ Nueva Factura", new String[]{"Proveedor", "Fecha de Emisión", "Importe Total", "Detalles", "Estado"});
        controller = FacturaController.getInstance();
    }

    @Override
    protected void abrirCreacion() {
        new FacturaDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<FacturaDTO> facturas = controller.getFacturas();
        if (row < 0 || row >= facturas.size()) return;
        new FacturaDialog(getParentFrame(), facturas.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (FacturaDTO dto : controller.getFacturas()) {
            String fecha = dto.getFechaEmision() != null ? sdf.format(dto.getFechaEmision()) : "";
            String proveedor = dto.getProveedor() != null ? dto.getProveedor().getRazonSocial() : "";
            String estado = dto.isObservada() ? "Observada" : "Registrada";
            tableModel.addRow(new Object[]{
                    proveedor, fecha, String.format("%.2f", dto.getImporteTotal()),
                    dto.getCantidadDetalles(), estado});
        }
    }
}
