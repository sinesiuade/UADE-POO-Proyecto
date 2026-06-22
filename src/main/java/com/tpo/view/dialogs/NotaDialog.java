package com.tpo.view.dialogs;

import com.tpo.controller.NotaController;
import com.tpo.model.dto.NotaDTO;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Diálogo de alta/edición de Nota de Crédito o Débito. */
public class NotaDialog extends JDialog {

    private final JComboBox<String> comboTipo;
    private final JTextField txtNumero;
    private final JTextField txtProveedor;
    private final JTextField txtFecha;
    private final JTextField txtImporte;

    private final NotaController controller;
    private final int editIndex;

    public NotaDialog(Frame parent) {
        this(parent, null, -1);
    }

    public NotaDialog(Frame parent, NotaDTO existing, int editIndex) {
        super(parent, existing == null ? "Nueva Nota C/D" : "Editar Nota C/D", true);
        this.editIndex = editIndex;
        setSize(440, 330);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = NotaController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nueva Nota C/D" : "Editar Nota C/D", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        comboTipo = new JComboBox<>(new String[]{NotaDTO.CREDITO, NotaDTO.DEBITO});
        txtNumero = new JTextField(20);
        txtProveedor = new JTextField(20);
        txtFecha = new JTextField(20);
        txtImporte = new JTextField(20);

        if (existing != null) {
            comboTipo.setSelectedItem(existing.getTipo());
            txtNumero.setText(String.valueOf(existing.getNumero()));
            if (existing.getProveedor() != null) txtProveedor.setText(existing.getProveedor().getRazonSocial());
            if (existing.getFechaEmision() != null) {
                txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(existing.getFechaEmision()));
            }
            txtImporte.setText(String.valueOf(existing.getImporteTotal()));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"Tipo:", "Número:", "Proveedor:", "Fecha (dd/MM/yyyy):", "Importe:"};
        JComponent[] fields = {comboTipo, txtNumero, txtProveedor, txtFecha, txtImporte};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.45;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.55;
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
        String proveedorStr = txtProveedor.getText().trim();
        if (proveedorStr.isEmpty()) {
            error("El proveedor es obligatorio.");
            return;
        }
        int numero;
        float importe;
        try {
            numero = Integer.parseInt(txtNumero.getText().trim());
            importe = Float.parseFloat(txtImporte.getText().trim());
        } catch (NumberFormatException e) {
            error("Número e importe deben ser valores numéricos.");
            return;
        }
        Date fecha;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            fecha = sdf.parse(txtFecha.getText().trim());
        } catch (ParseException e) {
            error("La fecha debe tener el formato dd/MM/yyyy.");
            return;
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(proveedorStr);
        String tipo = (String) comboTipo.getSelectedItem();

        if (editIndex >= 0) {
            controller.editarNota(editIndex, tipo, proveedor, numero, importe, fecha);
        } else {
            controller.registrarNota(tipo, proveedor, numero, importe, fecha);
        }
        dispose();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
