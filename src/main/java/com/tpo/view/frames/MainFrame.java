package com.tpo.view.frames;

import com.tpo.util.AppConstants;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle(AppConstants.APP_TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(AppConstants.VENTANA_ANCHO, AppConstants.VENTANA_ALTO);
        setLocationRelativeTo(null);
    }

    public void setPanelPrincipal(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
