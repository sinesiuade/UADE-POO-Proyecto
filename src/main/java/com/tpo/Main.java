package com.tpo;

import com.tpo.controller.EjemploController;
import com.tpo.model.repositories.EjemploRepository;
import com.tpo.model.services.EjemploService;
import com.tpo.view.frames.MainFrame;
import com.tpo.view.panels.EjemploPanel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            EjemploPanel panel = new EjemploPanel();
            frame.setPanelPrincipal(panel);

            EjemploService servicio = new EjemploService(new EjemploRepository());
            new EjemploController(panel, servicio);

            frame.setVisible(true);
        });
    }

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se usa el Look and Feel por defecto de Swing
        }
    }
}
