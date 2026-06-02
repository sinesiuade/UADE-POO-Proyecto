package com.tpo.view.dialogs;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public class ItemDialog extends JDialog {

    public ItemDialog(Frame parent) {
        super(parent, "Agregar Item", true);
        setSize(420, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("Nuevo Item", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"Código:", "Descripción:", "Rubro:", "Unidad de Medida:", "Precio Unitario:", "% IVA:"};
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new JTextField(20);
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.4;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.6;
            form.add(fields[i], gbc);
        }

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Item guardado correctamente.");
            dispose();
        });
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }
}
