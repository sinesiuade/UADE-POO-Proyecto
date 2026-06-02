package com.tpo.view.panels;

import com.tpo.view.dialogs.ImpuestoDialog;
import com.tpo.view.dialogs.ItemDialog;
import com.tpo.view.dialogs.OrdenDeCompraDialog;
import com.tpo.view.dialogs.OrdenDePagoDialog;
import com.tpo.view.dialogs.ProveedorDialog;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel extends JPanel {

    private final JTabbedPane tabbedPane;

    public MainPanel() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Proveedores", crearTabPlaceholder("proveedor"));
        tabbedPane.addTab("Items", crearTabPlaceholder("item"));
        tabbedPane.addTab("Órdenes de Compra", crearTabPlaceholder("orden de compra"));
        tabbedPane.addTab("Órdenes de Pago", crearTabPlaceholder("orden de pago"));
        tabbedPane.addTab("Impuestos", crearTabPlaceholder("impuesto"));

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                if (index >= 0) {
                    abrirDialogo(index);
                }
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearTabPlaceholder(String entidad) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(
                "<html><center>Haga clic en la pestaña para agregar un nuevo " + entidad + "</center></html>",
                SwingConstants.CENTER
        );
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void abrirDialogo(int index) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = (parent instanceof Frame f) ? f : null;

        switch (index) {
            case 0 -> new ProveedorDialog(frame).setVisible(true);
            case 1 -> new ItemDialog(frame).setVisible(true);
            case 2 -> new OrdenDeCompraDialog(frame).setVisible(true);
            case 3 -> new OrdenDePagoDialog(frame).setVisible(true);
            case 4 -> new ImpuestoDialog(frame).setVisible(true);
        }
    }
}
