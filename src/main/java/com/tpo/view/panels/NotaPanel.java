package com.tpo.view.panels;

import com.tpo.controller.NotaController;
import com.tpo.model.dto.NotaDTO;
import com.tpo.view.dialogs.NotaDialog;

import java.text.SimpleDateFormat;
import java.util.List;

public class NotaPanel extends BaseListPanel {

    private final NotaController controller;

    public NotaPanel() {
        super("+ Nueva Nota C/D", new String[]{"Tipo", "Número", "Proveedor", "Fecha", "Importe"});
        controller = NotaController.getInstance();
        refrescar();
    }

    @Override
    protected void abrirCreacion() {
        new NotaDialog(getParentFrame()).setVisible(true);
        refrescar();
    }

    @Override
    protected void abrirEdicion(int row) {
        List<NotaDTO> lista = controller.getNotas();
        if (row < 0 || row >= lista.size()) return;
        new NotaDialog(getParentFrame(), lista.get(row), row).setVisible(true);
        refrescar();
    }

    public void refrescar() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (NotaDTO dto : controller.getNotas()) {
            String fecha = dto.getFechaEmision() != null ? sdf.format(dto.getFechaEmision()) : "";
            String proveedor = dto.getProveedor() != null ? dto.getProveedor().getRazonSocial() : "";
            tableModel.addRow(new Object[]{
                    dto.getTipo(), dto.getNumero(), proveedor, fecha,
                    String.format("%.2f", dto.getImporteTotal())});
        }
    }
}
