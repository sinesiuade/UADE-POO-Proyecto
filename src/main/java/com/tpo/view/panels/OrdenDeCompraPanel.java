package com.tpo.view.panels;

import com.tpo.controller.OrdenDeCompraController;
import com.tpo.controller.SesionController;
import com.tpo.model.dto.OrdenDeCompraDTO;
import com.tpo.view.dialogs.OrdenDeCompraDialog;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrdenDeCompraPanel extends BaseListPanel {

    private final OrdenDeCompraController controller;

    public OrdenDeCompraPanel() {
        super("+ Nueva Orden de Compra", new String[]{"Número", "Fecha", "Proveedor", "Estado"});
        controller = OrdenDeCompraController.getInstance();

        // Acción exclusiva del supervisor: aprobar OC pendientes de aprobación.
        if (SesionController.getInstance().esSupervisor()) {
            JButton btnAprobar = new JButton("✓ Aprobar seleccionada");
            btnAprobar.addActionListener(e -> aprobarSeleccionada());
            barraSuperior.add(btnAprobar);
        }

        refrescar();
    }

    private void aprobarSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná una orden de compra.",
                    "Aprobar OC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            OrdenDeCompraDTO dto = controller.aprobarOrdenDeCompra(row);
            JOptionPane.showMessageDialog(this,
                    "Orden de compra N° " + dto.getNumero() + " aprobada.\nEstado: " + dto.getEstado(),
                    "Aprobar OC", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aprobar OC", JOptionPane.ERROR_MESSAGE);
        }
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
