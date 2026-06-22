package com.tpo.view.panels;

import com.tpo.controller.ConsultaController;
import com.tpo.model.dto.ReporteDTO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

/** Panel de Consultas: elige un reporte y lo muestra en una tabla. */
public class ConsultaPanel extends JPanel {

    private final ConsultaController controller = ConsultaController.getInstance();
    private final JComboBox<String> comboReportes;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblTitulo;

    public ConsultaPanel() {
        setLayout(new BorderLayout());

        comboReportes = new JComboBox<>(controller.getNombresDeReportes().toArray(new String[0]));
        JButton btnEjecutar = new JButton("Ejecutar");
        btnEjecutar.addActionListener(e -> ejecutar());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        barra.add(new JLabel("Consulta:"));
        barra.add(comboReportes);
        barra.add(btnEjecutar);
        add(barra, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(26);

        lblTitulo = new JLabel(" Seleccione una consulta y presione Ejecutar.");
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(lblTitulo, BorderLayout.NORTH);
        centro.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private void ejecutar() {
        String nombre = (String) comboReportes.getSelectedItem();
        if (nombre == null) {
            return;
        }
        ReporteDTO reporte = controller.ejecutar(nombre);
        lblTitulo.setText(" " + reporte.getTitulo() + "  (" + reporte.getFilas().size() + " registros)");
        tableModel.setColumnIdentifiers(reporte.getColumnas());
        tableModel.setRowCount(0);
        for (Object[] fila : reporte.getFilas()) {
            tableModel.addRow(fila);
        }
    }
}
