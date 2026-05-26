package com.tpo.view.panels;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class EjemploPanel extends JPanel {

    private final JTextField campoNombre;
    private final JButton btnGuardar;
    private final JTextArea areaResultado;

    public EjemploPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Nombre:"));
        campoNombre = new JTextField(20);
        formulario.add(campoNombre);
        btnGuardar = new JButton("Guardar");
        formulario.add(btnGuardar);

        areaResultado = new JTextArea(10, 40);
        areaResultado.setEditable(false);

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);
    }

    public JTextField getCampoNombre() {
        return campoNombre;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public void mostrarMensaje(String mensaje) {
        areaResultado.setText(mensaje);
    }

    public void limpiarCampoNombre() {
        campoNombre.setText("");
    }
}
