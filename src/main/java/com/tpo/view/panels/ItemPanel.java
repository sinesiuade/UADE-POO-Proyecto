package com.tpo.view.panels;

import com.tpo.view.dialogs.ItemDialog;

public class ItemPanel extends BaseListPanel {

    public ItemPanel() {
        super("+ Nuevo Item", new String[]{"Código", "Descripción", "Rubro", "Unidad de Medida", "Precio Unitario", "% IVA"});
    }

    @Override
    protected void abrirCreacion() {
        new ItemDialog(getParentFrame()).setVisible(true);
    }

    @Override
    protected void abrirEdicion(int row) {
        new ItemDialog(getParentFrame()).setVisible(true);
    }
}
