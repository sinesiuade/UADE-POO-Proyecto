package com.tpo.view.dialogs;

import com.tpo.controller.ProveedorController;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.entities.supplier.Proveedor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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

/** Diálogo de alta/edición de Proveedor. */
public class ProveedorDialog extends JDialog {

    private final JTextField txtCuit;
    private final JTextField txtRazonSocial;
    private final JTextField txtNombreComercial;
    private final JTextField txtDomicilio;
    private final JTextField txtTelefono;
    private final JTextField txtEmail;
    private final JComboBox<CondicionImpositiva> comboCondicion;
    private final JTextField txtLimite;

    private final ProveedorController controller;
    private final int editIndex;

    public ProveedorDialog(Frame parent) {
        this(parent, null, -1);
    }

    public ProveedorDialog(Frame parent, Proveedor existing, int editIndex) {
        super(parent, existing == null ? "Nuevo Proveedor" : "Editar Proveedor", true);
        this.editIndex = editIndex;
        setSize(460, 460);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = ProveedorController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nuevo Proveedor" : "Editar Proveedor", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        txtCuit = new JTextField(20);
        txtRazonSocial = new JTextField(20);
        txtNombreComercial = new JTextField(20);
        txtDomicilio = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtEmail = new JTextField(20);
        comboCondicion = new JComboBox<>(CondicionImpositiva.values());
        txtLimite = new JTextField(20);

        if (existing != null) {
            if (existing.getCuit() > 0) txtCuit.setText(String.valueOf(existing.getCuit()));
            txtRazonSocial.setText(existing.getRazonSocial());
            txtNombreComercial.setText(existing.getNombreComercial());
            txtDomicilio.setText(existing.getDomicilio());
            if (existing.getTelefono() > 0) txtTelefono.setText(String.valueOf(existing.getTelefono()));
            txtEmail.setText(existing.getEmail());
            if (existing.getCondicionImpositiva() != null) comboCondicion.setSelectedItem(existing.getCondicionImpositiva());
            txtLimite.setText(String.valueOf(existing.getLimiteDeudaAutorizado()));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"CUIT:", "Razón Social:", "Nombre Comercial:", "Domicilio:", "Teléfono:", "Email:", "Condición Impositiva:", "Límite de Deuda:"};
        JComponent[] fields = {txtCuit, txtRazonSocial, txtNombreComercial, txtDomicilio, txtTelefono, txtEmail, comboCondicion, txtLimite};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

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
        String razonSocial = txtRazonSocial.getText().trim();
        if (razonSocial.isEmpty()) {
            error("La razón social es obligatoria.");
            return;
        }
        long cuit = 0;
        long telefono = 0;
        int limite = 0;
        try {
            if (!txtCuit.getText().trim().isEmpty()) cuit = Long.parseLong(txtCuit.getText().trim());
            if (!txtTelefono.getText().trim().isEmpty()) telefono = Long.parseLong(txtTelefono.getText().trim());
            if (!txtLimite.getText().trim().isEmpty()) limite = Integer.parseInt(txtLimite.getText().trim());
        } catch (NumberFormatException e) {
            error("CUIT, teléfono y límite deben ser numéricos (sin guiones ni puntos).");
            return;
        }

        if (controller.existeProveedor(cuit, razonSocial, editIndex)) {
            error(cuit > 0
                    ? "Ya existe un proveedor con el CUIT " + cuit + "."
                    : "Ya existe un proveedor con esa razón social.");
            return;
        }

        Proveedor p = new Proveedor();
        p.setCuit(cuit);
        p.setRazonSocial(razonSocial);
        p.setNombreComercial(txtNombreComercial.getText().trim());
        p.setDomicilio(txtDomicilio.getText().trim());
        p.setTelefono(telefono);
        p.setEmail(txtEmail.getText().trim());
        p.setCondicionImpositiva((CondicionImpositiva) comboCondicion.getSelectedItem());
        p.setLimiteDeudaAutorizado(limite);

        if (editIndex >= 0) {
            controller.editarProveedor(editIndex, p);
        } else {
            controller.agregarProveedor(p);
        }
        dispose();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
