package com.tpo.view.panels;

import com.tpo.controller.OrdenDePagoController;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.view.dialogs.OrdenDePagoDialog;

import java.text.SimpleDateFormat;
import java.util.List;

public class OrdenDePagoPanel extends BaseListPanel {

    private final OrdenDePagoController controller;

    public OrdenDePagoPanel() {
        super("+ Nueva Orden de Pago", new String[]{"Proveedor", "Fecha de Emisión", "Total Bruto", "Total Retenido", "Total Neto", "Medio de Pago"});
        controller = OrdenDePagoController.getInstance();
    }

    @Override
    protected void abrirCreacion() {
        new OrdenDePagoDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<OrdenDePagoDTO> pagos = controller.getPagos();
        if (row < 0 || row >= pagos.size()) return;
        new OrdenDePagoDialog(getParentFrame(), pagos.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (OrdenDePagoDTO dto : controller.getPagos()) {
            String fecha = dto.getFechaEmision() != null ? sdf.format(dto.getFechaEmision()) : "";
            String proveedor = dto.getProveedor() != null ? dto.getProveedor().getRazonSocial() : "";
            tableModel.addRow(new Object[]{
                    proveedor, fecha,
                    String.format("%.2f", dto.getTotalBrutoPagado()),
                    String.format("%.2f", dto.getTotalRetenido()),
                    String.format("%.2f", dto.getTotalNetoAPagar()),
                    dto.getMedioDePago() != null ? dto.getMedioDePago() : "-"});
        }
    }
}
