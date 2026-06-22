package com.tpo.view.dialogs;

import com.tpo.controller.OrdenDePagoController;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.model.entities.document.DocumentoComercial;
import com.tpo.model.entities.document.Factura;
import com.tpo.model.entities.payment.ChequePropio;
import com.tpo.model.entities.payment.ChequeTerceros;
import com.tpo.model.entities.payment.Efectivo;
import com.tpo.model.entities.payment.MedioDePago;
import com.tpo.model.entities.payment.Transferencia;
import com.tpo.model.entities.supplier.Proveedor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo de Orden de Pago. El total retenido y el neto NO se cargan a mano: los calcula el
 * controlador recorriendo los impuestos del sistema según la condición fiscal del proveedor
 * (Diagrama de Secuencia "Generación de Orden de Pago").
 */
public class OrdenDePagoDialog extends JDialog {

    private static final String[] MEDIOS = {"Efectivo", "Transferencia", "Cheque Propio", "Cheque de Terceros"};

    private final JTextField txtProveedor;
    private final JComboBox<CondicionImpositiva> comboCondicion;
    private final JTextField txtBruto;
    private final JComboBox<String> comboMedio;

    private final OrdenDePagoController controller;
    private final int editIndex;

    /** Modo creación */
    public OrdenDePagoDialog(Frame parent) {
        this(parent, null, -1);
    }

    /** Modo edición: editIndex es la posición en la lista del controller */
    public OrdenDePagoDialog(Frame parent, OrdenDePagoDTO existing, int editIndex) {
        super(parent, existing == null ? "Nueva Orden de Pago" : "Editar Orden de Pago", true);
        this.editIndex = editIndex;
        setSize(440, 320);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = OrdenDePagoController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nueva Orden de Pago" : "Editar Orden de Pago", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        txtProveedor = new JTextField(20);
        comboCondicion = new JComboBox<>(CondicionImpositiva.values());
        txtBruto = new JTextField(20);
        comboMedio = new JComboBox<>(MEDIOS);

        if (existing != null) {
            if (existing.getProveedor() != null) {
                txtProveedor.setText(existing.getProveedor().getRazonSocial());
                if (existing.getProveedor().getCondicionImpositiva() != null) {
                    comboCondicion.setSelectedItem(existing.getProveedor().getCondicionImpositiva());
                }
            }
            txtBruto.setText(String.valueOf(existing.getTotalBrutoPagado()));
            if (existing.getMedioDePago() != null) {
                comboMedio.setSelectedItem(existing.getMedioDePago());
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"Proveedor:", "Condición impositiva:", "Total bruto a pagar:", "Medio de pago:"};
        JComponent[] fields = {txtProveedor, comboCondicion, txtBruto, comboMedio};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.45;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.55;
            form.add(fields[i], gbc);
        }

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
        float bruto;
        try {
            bruto = Float.parseFloat(txtBruto.getText().trim());
        } catch (NumberFormatException e) {
            error("El total bruto debe ser un valor numérico.");
            return;
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(proveedorStr);
        proveedor.setCondicionImpositiva((CondicionImpositiva) comboCondicion.getSelectedItem());

        // Los documentos a cancelar se representan por su importe total agregado.
        Factura documento = new Factura(proveedor);
        documento.setImporteTotal(bruto);
        List<DocumentoComercial> documentos = new ArrayList<>();
        documentos.add(documento);

        MedioDePago medio = crearMedio((String) comboMedio.getSelectedItem());

        OrdenDePagoDTO dto = editIndex >= 0
                ? controller.editarOrdenDePago(editIndex, proveedor, documentos, medio)
                : controller.generarOrdenDePago(proveedor, documentos, medio);

        JOptionPane.showMessageDialog(this,
                String.format("Orden de pago %s.%nBruto: %.2f%nRetenido: %.2f%nNeto a pagar: %.2f",
                        editIndex >= 0 ? "actualizada" : "generada",
                        dto.getTotalBrutoPagado(), dto.getTotalRetenido(), dto.getTotalNetoAPagar()),
                "Orden de Pago", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /** Crea el medio de pago concreto según la opción elegida (el importe lo fija el controlador). */
    private MedioDePago crearMedio(String label) {
        if (label == null) {
            return new Efectivo(0f);
        }
        return switch (label) {
            case "Transferencia" -> new Transferencia(0f, 0f, null);
            case "Cheque Propio" -> new ChequePropio(0f, 0f, null, null, null, null);
            case "Cheque de Terceros" -> new ChequeTerceros(0f, 0f, null, null, null, null);
            default -> new Efectivo(0f);
        };
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
