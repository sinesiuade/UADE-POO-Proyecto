package com.tpo.view.dialogs;

import com.tpo.controller.OrdenDeCompraController;
import com.tpo.model.dto.OrdenDeCompraDTO;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.order.OrdenDeCompra;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.view.components.ProveedorSelector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Diálogo de Orden de Compra (el estado lo calcula el controlador por límite de crédito). */
public class OrdenDeCompraDialog extends JDialog {

    private final JTextField txtNumero;
    private final JTextField txtFecha;
    private final ProveedorSelector comboProveedor;
    private final JTextField txtRubro;
    private final JTextField txtMonto;

    private final OrdenDeCompraController controller;
    private final int editIndex;

    /** Modo creación */
    public OrdenDeCompraDialog(Frame parent) {
        this(parent, null, -1);
    }

    /** Modo edición */
    public OrdenDeCompraDialog(Frame parent, OrdenDeCompraDTO existing, int editIndex) {
        super(parent, existing == null ? "Nueva Orden de Compra" : "Editar Orden de Compra", true);
        this.editIndex = editIndex;
        setSize(440, 340);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = OrdenDeCompraController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nueva Orden de Compra" : "Editar Orden de Compra", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        txtNumero = new JTextField(20);
        txtFecha = new JTextField(20);
        comboProveedor = new ProveedorSelector();
        txtRubro = new JTextField(20);
        txtMonto = new JTextField(20);

        if (existing != null) {
            txtNumero.setText(String.valueOf(existing.getNumero()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            if (existing.getFecha() != null) txtFecha.setText(sdf.format(existing.getFecha()));
            if (existing.getProveedor() != null) comboProveedor.seleccionarPorRazonSocial(existing.getProveedor().getRazonSocial());
            if (existing.getDetalleItems() != null && !existing.getDetalleItems().isEmpty()) {
                var primera = existing.getDetalleItems().get(0);
                if (primera.getItem() != null && primera.getItem().getRubro() != null) {
                    txtRubro.setText(primera.getItem().getRubro().getNombre());
                }
            }
            txtMonto.setText(String.valueOf(existing.getTotalBruto()));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"Número:", "Fecha (dd/MM/yyyy):", "Proveedor:", "Rubro:", "Monto de la orden:"};
        JComponent[] fields = {txtNumero, txtFecha, comboProveedor, txtRubro, txtMonto};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.45;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.55;
            form.add(fields[i], gbc);
        }

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }

    private void guardar() {
        if (!comboProveedor.hayProveedores()) {
            error("No hay proveedores cargados. Cargá un proveedor primero.");
            return;
        }
        Proveedor proveedor = comboProveedor.getSeleccionado();
        if (proveedor == null) {
            error("Seleccioná un proveedor.");
            return;
        }

        String numeroStr = txtNumero.getText().trim();
        String fechaStr = txtFecha.getText().trim();
        if (numeroStr.isEmpty() || fechaStr.isEmpty()) {
            error("Número y fecha son obligatorios.");
            return;
        }

        int numero;
        double monto;
        try {
            numero = Integer.parseInt(numeroStr);
            monto = Double.parseDouble(txtMonto.getText().trim());
        } catch (NumberFormatException e) {
            error("Número y monto deben ser valores numéricos.");
            return;
        }

        Date fecha;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            fecha = sdf.parse(fechaStr);
        } catch (ParseException e) {
            error("La fecha debe tener el formato dd/MM/yyyy.");
            return;
        }

        OrdenDeCompra oc = new OrdenDeCompra(numero, fecha, proveedor, null);
        Producto item = new Producto();
        item.setDescripcion("Ítems de la orden");
        String rubroStr = txtRubro.getText().trim();
        if (!rubroStr.isEmpty()) {
            item.setRubro(new Rubro(rubroStr));
        }
        oc.agregarItem(item, 1, monto);

        OrdenDeCompraDTO dto = editIndex >= 0
                ? controller.editarOrdenDeCompra(editIndex, oc)
                : controller.crearOrdenDeCompra(oc);

        JOptionPane.showMessageDialog(this,
                "Orden registrada.\nEstado calculado: " + dto.getEstado(),
                "Orden de Compra", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
