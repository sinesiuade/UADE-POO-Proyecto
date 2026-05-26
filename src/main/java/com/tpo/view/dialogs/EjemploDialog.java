package com.tpo.view.dialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Frame;

public class EjemploDialog extends JDialog {

    public EjemploDialog(Frame parent, String titulo, String mensaje) {
        super(parent, titulo, true);
        setSize(350, 150);
        setLocationRelativeTo(parent);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.add(new JLabel(mensaje), BorderLayout.CENTER);
        add(contenido);
    }
}
