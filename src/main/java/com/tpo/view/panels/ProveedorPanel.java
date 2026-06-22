package com.tpo.view.panels;

import com.tpo.view.dialogs.ProveedorDialog;

public class ProveedorPanel extends BaseListPanel {

    public ProveedorPanel() {
        super("+ Nuevo Proveedor", new String[]{"CUIT", "Razón Social", "Nombre Comercial", "Domicilio", "Teléfono", "Email", "Cond. Impositiva"});
    }

    @Override
    protected void abrirCreacion() {
        new ProveedorDialog(getParentFrame()).setVisible(true);
    }

    @Override
    protected void abrirEdicion(int row) {
        new ProveedorDialog(getParentFrame()).setVisible(true);
    }
}
