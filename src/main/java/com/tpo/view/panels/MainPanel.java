package com.tpo.view.panels;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class MainPanel extends JPanel {

    private final JTabbedPane tabbedPane;

    public MainPanel() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Proveedores", new ProveedorPanel());
        tabbedPane.addTab("Items", new ItemPanel());
        tabbedPane.addTab("Órdenes de Compra", new OrdenDeCompraPanel());
        tabbedPane.addTab("Facturas", new FacturaPanel());
        tabbedPane.addTab("Notas C/D", new NotaPanel());
        tabbedPane.addTab("Órdenes de Pago", new OrdenDePagoPanel());
        tabbedPane.addTab("Impuestos", new ImpuestoPanel());
        tabbedPane.addTab("Consultas", new ConsultaPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
