package com.tpo.view.panels;

import com.tpo.controller.ProveedorController;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.view.dialogs.ProveedorDialog;

import java.util.List;

public class ProveedorPanel extends BaseListPanel {

    private final ProveedorController controller;

    public ProveedorPanel() {
        super("+ Nuevo Proveedor", new String[]{"CUIT", "Razón Social", "Nombre Comercial", "Domicilio", "Teléfono", "Email", "Cond. Impositiva"});
        controller = ProveedorController.getInstance();
        refrescar();
    }

    @Override
    protected void abrirCreacion() {
        new ProveedorDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<Proveedor> lista = controller.getProveedores();
        if (row < 0 || row >= lista.size()) return;
        new ProveedorDialog(getParentFrame(), lista.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        for (Proveedor p : controller.getProveedores()) {
            String cuit = p.getCuit() > 0 ? String.valueOf(p.getCuit()) : "";
            String tel = p.getTelefono() > 0 ? String.valueOf(p.getTelefono()) : "";
            String cond = p.getCondicionImpositiva() != null ? p.getCondicionImpositiva().toString() : "";
            tableModel.addRow(new Object[]{cuit, p.getRazonSocial(), p.getNombreComercial(),
                    p.getDomicilio(), tel, p.getEmail(), cond});
        }
    }
}
