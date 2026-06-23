package com.tpo.view.components;

import com.tpo.controller.ProveedorController;
import com.tpo.model.entities.supplier.Proveedor;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.util.List;

/** Combo para elegir un proveedor del catálogo; si no hay, muestra un placeholder y queda deshabilitado. */
public class ProveedorSelector extends JComboBox<Proveedor> {

    private static final String PLACEHOLDER = "(no hay proveedores cargados)";

    private final boolean hayProveedores;

    public ProveedorSelector() {
        List<Proveedor> proveedores = ProveedorController.getInstance().getProveedores();
        hayProveedores = !proveedores.isEmpty();

        if (hayProveedores) {
            for (Proveedor p : proveedores) {
                addItem(p);
            }
        } else {
            addItem(null);
            setEnabled(false);
        }

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof Proveedor p ? p.getRazonSocial() : PLACEHOLDER);
                return this;
            }
        });
    }

    public Proveedor getSeleccionado() {
        return (Proveedor) getSelectedItem();
    }

    public boolean hayProveedores() {
        return hayProveedores;
    }

    /** Selecciona el proveedor cuya razón social coincide (para modo edición). */
    public void seleccionarPorRazonSocial(String razonSocial) {
        if (razonSocial == null) {
            return;
        }
        for (int i = 0; i < getItemCount(); i++) {
            Proveedor p = getItemAt(i);
            if (p != null && razonSocial.equals(p.getRazonSocial())) {
                setSelectedIndex(i);
                return;
            }
        }
    }
}
