package com.tpo.view.panels;

import com.tpo.controller.ItemController;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.view.dialogs.ItemDialog;

import java.util.List;

public class ItemPanel extends BaseListPanel {

    private final ItemController controller;

    public ItemPanel() {
        super("+ Nuevo Item", new String[]{"Tipo", "Código", "Descripción", "Rubro", "Unidad de Medida", "Precio Unitario", "% IVA"});
        controller = ItemController.getInstance();
        refrescar();
    }

    @Override
    protected void abrirCreacion() {
        new ItemDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<Item> items = controller.getItems();
        if (row < 0 || row >= items.size()) return;
        new ItemDialog(getParentFrame(), items.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        for (Item item : controller.getItems()) {
            String tipo = item instanceof Producto ? "Producto" : "Servicio";
            String rubro = item.getRubro() != null ? item.getRubro().getNombre() : "";
            tableModel.addRow(new Object[]{
                    tipo,
                    item.getCodigo(),
                    item.getDescripcion(),
                    rubro,
                    item.getUnidadMedida(),
                    String.format("%.2f", item.getPrecioUnitario()),
                    String.format("%.2f", item.getPorcentajeIva())});
        }
    }
}
