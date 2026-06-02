package com.tpo;

import com.tpo.view.frames.MainFrame;
import com.tpo.view.panels.MainPanel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setPanelPrincipal(new MainPanel());
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
