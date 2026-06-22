package com.tpo.view.panels;

import com.tpo.controller.ImpuestoController;
import com.tpo.model.entities.tax.Impuesto;
import com.tpo.view.dialogs.ImpuestoDialog;

import java.util.List;

public class ImpuestoPanel extends BaseListPanel {

    private final ImpuestoController controller;

    public ImpuestoPanel() {
        super("+ Nuevo Impuesto", new String[]{"Tipo de Impuesto", "Porcentaje (%)"});
        controller = ImpuestoController.getInstance();
        refrescar();
    }

    @Override
    protected void abrirCreacion() {
        new ImpuestoDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<Impuesto> impuestos = controller.getImpuestos();
        if (row < 0 || row >= impuestos.size()) return;
        new ImpuestoDialog(getParentFrame(), impuestos.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        for (Impuesto impuesto : controller.getImpuestos()) {
            tableModel.addRow(new Object[]{impuesto.getNombre(), String.format("%.2f", impuesto.getPorcentaje())});
        }
    }
}
