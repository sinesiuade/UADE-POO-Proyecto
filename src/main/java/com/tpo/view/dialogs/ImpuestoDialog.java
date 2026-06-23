package com.tpo.view.dialogs;

import com.tpo.controller.ImpuestoController;
import com.tpo.model.entities.tax.Impuesto;
import com.tpo.model.entities.tax.TipoImpuesto;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ImpuestoDialog extends JDialog {

    private final JComboBox<TipoImpuesto> comboTipo;
    private final JTextField txtPorcentaje;
    private final JTextField txtMinimoNoImponible;
    private final ImpuestoController controller;
    private final int editIndex;

    /** Modo creación */
    public ImpuestoDialog(Frame parent) {
        this(parent, null, -1);
    }

    /** Modo edición */
    public ImpuestoDialog(Frame parent, Impuesto existing, int editIndex) {
        super(parent, existing == null ? "Agregar Impuesto" : "Editar Impuesto", true);
        this.editIndex = editIndex;
        setSize(390, 290);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = ImpuestoController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nuevo Impuesto" : "Editar Impuesto", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        comboTipo = new JComboBox<>(TipoImpuesto.values());
        txtPorcentaje = new JTextField(20);
        txtMinimoNoImponible = new JTextField(20);

        if (existing != null) {
            comboTipo.setSelectedItem(existing.getNombre());
            txtPorcentaje.setText(String.valueOf(existing.getPorcentaje()));
            txtMinimoNoImponible.setText(String.valueOf(existing.getMinimoNoImponible()));
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        form.add(new JLabel("Tipo de Impuesto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        form.add(comboTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        form.add(new JLabel("Porcentaje (%):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        form.add(txtPorcentaje, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
        form.add(new JLabel("Mínimo No Imponible:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        form.add(txtMinimoNoImponible, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }

    private void guardar() {
        double porcentaje;
        double minimoNoImponible;
        try {
            porcentaje = Double.parseDouble(txtPorcentaje.getText().trim());
            String mni = txtMinimoNoImponible.getText().trim();
            minimoNoImponible = mni.isEmpty() ? 0.0 : Double.parseDouble(mni);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El porcentaje y el mínimo no imponible deben ser valores numéricos.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Impuesto impuesto = new Impuesto((TipoImpuesto) comboTipo.getSelectedItem(), porcentaje, minimoNoImponible);
        if (editIndex >= 0) {
            controller.editarImpuesto(editIndex, impuesto);
        } else {
            controller.agregarImpuesto(impuesto);
        }
        dispose();
    }
}
