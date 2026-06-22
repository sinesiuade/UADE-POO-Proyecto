package com.tpo.view.dialogs;

import com.tpo.controller.FacturaController;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.model.entities.catalog.ItemProveedor;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.order.OrdenDeCompra;
import com.tpo.model.entities.supplier.Proveedor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Diálogo de Factura. Reproduce, sobre una línea representativa, el Diagrama de Secuencia
 * "Generación de Factura": valida origen (OC previa o autorización), verifica congruencia de
 * rubro/ítem y aplica el control de precios (Observada si el precio facturado difiere del de la OC).
 */
public class FacturaDialog extends JDialog {

    private final JTextField txtProveedor;
    private final JTextField txtRubro;
    private final JTextField txtConcepto;
    private final JTextField txtCantidad;
    private final JTextField txtPrecioOC;
    private final JTextField txtPrecioFactura;
    private final JCheckBox chkExisteOC;
    private final JCheckBox chkAutorizado;

    private final FacturaController controller;
    private final int editIndex;

    /** Modo creación */
    public FacturaDialog(Frame parent) {
        this(parent, null, -1);
    }

    /** Modo edición: editIndex es la posición en la lista del controller */
    public FacturaDialog(Frame parent, FacturaDTO existing, int editIndex) {
        super(parent, existing == null ? "Nueva Factura" : "Editar Factura", true);
        this.editIndex = editIndex;
        setSize(460, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = FacturaController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nueva Factura" : "Editar Factura", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        txtProveedor = new JTextField(20);
        txtRubro = new JTextField(20);
        txtConcepto = new JTextField(20);
        txtCantidad = new JTextField(20);
        txtPrecioOC = new JTextField(20);
        txtPrecioFactura = new JTextField(20);
        chkExisteOC = new JCheckBox("Existe OC previa");
        chkExisteOC.setSelected(true);
        chkAutorizado = new JCheckBox("Autorizado por supervisor");

        if (existing != null && existing.getProveedor() != null) {
            txtProveedor.setText(existing.getProveedor().getRazonSocial());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 5, 4, 5);

        String[] labels = {"Proveedor:", "Rubro:", "Concepto del ítem:", "Cantidad:",
                "Precio OC (acordado):", "Precio Factura:"};
        JTextField[] fields = {txtProveedor, txtRubro, txtConcepto, txtCantidad, txtPrecioOC, txtPrecioFactura};

        int row = 0;
        for (; row < labels.length; row++) {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.45;
            form.add(new JLabel(labels[row]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.55;
            form.add(fields[row], gbc);
        }
        gbc.gridx = 1; gbc.gridy = row++;
        form.add(chkExisteOC, gbc);
        gbc.gridx = 1; gbc.gridy = row;
        form.add(chkAutorizado, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton(existing == null ? "Generar" : "Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> generar());
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }

    private void generar() {
        String proveedorStr = txtProveedor.getText().trim();
        if (proveedorStr.isEmpty()) {
            error("El proveedor es obligatorio.");
            return;
        }
        int cantidad;
        double precioOC;
        double precioFactura;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            precioOC = Double.parseDouble(txtPrecioOC.getText().trim());
            precioFactura = Double.parseDouble(txtPrecioFactura.getText().trim());
        } catch (NumberFormatException e) {
            error("Cantidad y precios deben ser valores numéricos.");
            return;
        }

        // Proveedor habilitado para el rubro y autorizado a proveer el ítem (relación N:M).
        Rubro rubro = new Rubro(txtRubro.getText().trim());
        Producto item = new Producto();
        item.setDescripcion(txtConcepto.getText().trim());
        item.setRubro(rubro);

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(proveedorStr);
        proveedor.getRubros().add(rubro);
        proveedor.getItemsProvistos().add(new ItemProveedor(precioOC, item, proveedor));

        List<OrdenDeCompra> ordenes = new ArrayList<>();
        if (chkExisteOC.isSelected()) {
            OrdenDeCompra oc = new OrdenDeCompra(0, new Date(), proveedor, null);
            oc.agregarItem(item, cantidad, precioOC);
            ordenes.add(oc);
        }

        try {
            FacturaDTO dto = editIndex >= 0
                    ? controller.editarFactura(editIndex, proveedor, ordenes, chkAutorizado.isSelected(), precioFactura)
                    : controller.generarFactura(proveedor, ordenes, chkAutorizado.isSelected(), precioFactura);
            JOptionPane.showMessageDialog(this,
                    String.format("Factura %s.%nImporte total: %.2f%nEstado: %s",
                            editIndex >= 0 ? "actualizada" : "generada",
                            dto.getImporteTotal(), dto.isObservada() ? "Observada" : "Registrada"),
                    "Factura", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
