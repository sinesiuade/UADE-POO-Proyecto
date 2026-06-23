package com.tpo.view.dialogs;

import com.tpo.controller.SesionController;
import com.tpo.model.Enums.Rol;
import com.tpo.model.entities.user.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

/** Selector de rol al iniciar la aplicación (Operador / Supervisor). */
public class SeleccionRolDialog extends JDialog {

    private final JTextField txtNombre;
    private final JComboBox<Rol> comboRol;

    public SeleccionRolDialog(Frame parent) {
        super(parent, "Ingreso al Sistema", true);
        setSize(380, 220);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel header = new JLabel("Sanatorio Integral del Sur", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        txtNombre = new JTextField(20);
        comboRol = new JComboBox<>(Rol.values());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        form.add(new JLabel("Nombre (opcional):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        form.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        form.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        form.add(comboRol, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());
        buttons.add(btnIngresar);
        add(buttons, BorderLayout.SOUTH);
    }

    private void ingresar() {
        Rol rol = (Rol) comboRol.getSelectedItem();
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            nombre = rol == Rol.SUPERVISOR ? "Supervisor" : "Operador";
        }
        SesionController.getInstance().setUsuarioActual(new Usuario(nombre, rol));
        dispose();
    }
}
