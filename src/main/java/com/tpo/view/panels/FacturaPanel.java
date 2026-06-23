package com.tpo.view.panels;

import com.tpo.controller.FacturaController;
import com.tpo.controller.SesionController;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.view.dialogs.FacturaDialog;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.util.List;

public class FacturaPanel extends BaseListPanel {

    private final FacturaController controller;

    public FacturaPanel() {
        super("+ Nueva Factura", new String[]{"Proveedor", "Fecha de Emisión", "Importe Total", "Detalles", "Estado"});
        controller = FacturaController.getInstance();

        // Acción exclusiva del supervisor: aprobar facturas observadas.
        if (SesionController.getInstance().esSupervisor()) {
            JButton btnAprobar = new JButton("✓ Aprobar observada");
            btnAprobar.addActionListener(e -> aprobarSeleccionada());
            barraSuperior.add(btnAprobar);
        }

        refrescar();
    }

    private void aprobarSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná una factura.",
                    "Aprobar factura", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            controller.aprobarFactura(row);
            JOptionPane.showMessageDialog(this, "Factura aprobada (observación levantada).",
                    "Aprobar factura", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aprobar factura", JOptionPane.ERROR_MESSAGE);
        }
        refrescar();
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
