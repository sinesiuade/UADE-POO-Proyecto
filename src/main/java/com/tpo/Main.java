package com.tpo;

import com.tpo.view.dialogs.SeleccionRolDialog;
import com.tpo.view.frames.MainFrame;
import com.tpo.view.panels.MainPanel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            // Selección de rol (Operador / Supervisor) antes de mostrar el sistema.
            new SeleccionRolDialog(frame).setVisible(true);
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
