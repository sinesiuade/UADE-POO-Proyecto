package com.tpo.view.dialogs;

import com.tpo.controller.FacturaController;
import com.tpo.controller.OrdenDePagoController;
import com.tpo.controller.OrdenDePagoController.ImputacionFactura;
import com.tpo.model.dto.FacturaDTO;
import com.tpo.model.dto.OrdenDePagoDTO;
import com.tpo.model.entities.payment.ChequePropio;
import com.tpo.model.entities.payment.ChequeTerceros;
import com.tpo.model.entities.payment.Efectivo;
import com.tpo.model.entities.payment.MedioDePago;
import com.tpo.model.entities.payment.Transferencia;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.view.components.ProveedorSelector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Diálogo de Orden de Pago: se elige cuánto se aplica a cada factura pendiente del proveedor. */
public class OrdenDePagoDialog extends JDialog {

    private static final String[] MEDIOS = {"Efectivo", "Transferencia", "Cheque Propio", "Cheque de Terceros"};
    private static final String[] COLUMNAS = {"Fecha", "Importe", "Saldo", "Monto a aplicar"};

    private final ProveedorSelector comboProveedor;
    private final JComboBox<String> comboMedio;
    private final DefaultTableModel tablaModel;
    private final JTable tabla;

    private final OrdenDePagoController controller;
    private final int editIndex;

    /** Facturas pendientes mostradas, en el mismo orden que las filas de la tabla. */
    private List<FacturaDTO> pendientes = new ArrayList<>();

    /** Modo creación */
    public OrdenDePagoDialog(Frame parent) {
        this(parent, null, -1);
    }

    /** Modo edición */
    public OrdenDePagoDialog(Frame parent, OrdenDePagoDTO existing, int editIndex) {
        super(parent, existing == null ? "Nueva Orden de Pago" : "Editar Orden de Pago", true);
        this.editIndex = editIndex;
        setSize(560, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = OrdenDePagoController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nueva Orden de Pago" : "Editar Orden de Pago", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        comboProveedor = new ProveedorSelector();
        comboMedio = new JComboBox<>(MEDIOS);

        // Solo la columna "Monto a aplicar" es editable.
        tablaModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        tabla = new JTable(tablaModel);
        tabla.setRowHeight(26);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Proveedor:"));
        top.add(comboProveedor);
        top.add(new JLabel("Medio de pago:"));
        top.add(comboMedio);

        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.NORTH);
        north.add(top, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        center.add(new JLabel("Facturas pendientes (ingresá el monto a aplicar a cada una):"), BorderLayout.NORTH);
        center.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        comboProveedor.addActionListener(e -> cargarPendientes());

        if (existing != null && existing.getProveedor() != null) {
            comboProveedor.seleccionarPorRazonSocial(existing.getProveedor().getRazonSocial());
            if (existing.getMedioDePago() != null) {
                comboMedio.setSelectedItem(existing.getMedioDePago());
            }
        }
        cargarPendientes();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton(existing == null ? "Generar" : "Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> generar());
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }

    /** Recarga la tabla con las facturas pendientes del proveedor seleccionado. */
    private void cargarPendientes() {
        tablaModel.setRowCount(0);
        pendientes = new ArrayList<>();
        Proveedor proveedor = comboProveedor.getSeleccionado();
        if (proveedor == null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        pendientes = FacturaController.getInstance().getPendientesPorProveedor(proveedor.getRazonSocial());
        for (FacturaDTO f : pendientes) {
            String fecha = f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "";
            tablaModel.addRow(new Object[]{
                    fecha,
                    String.format("%.2f", f.getImporteTotal()),
                    String.format("%.2f", f.getSaldo()),
                    String.format("%.2f", f.getSaldo())}); // default: cancelar el saldo total
        }
    }

    private void generar() {
        if (!comboProveedor.hayProveedores()) {
            error("No hay proveedores cargados. Cargá un proveedor primero.");
            return;
        }
        Proveedor proveedor = comboProveedor.getSeleccionado();
        if (proveedor == null) {
            error("Seleccioná un proveedor.");
            return;
        }
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
        if (pendientes.isEmpty()) {
            error("El proveedor no tiene facturas pendientes de pago.");
            return;
        }

        List<ImputacionFactura> imputaciones = new ArrayList<>();
        for (int i = 0; i < pendientes.size(); i++) {
            FacturaDTO factura = pendientes.get(i);
            double monto;
            try {
                monto = parseMonto(String.valueOf(tablaModel.getValueAt(i, 3)));
            } catch (NumberFormatException e) {
                error("El monto a aplicar de la fila " + (i + 1) + " debe ser numérico.");
                return;
            }
            if (monto < 0) {
                error("Los montos no pueden ser negativos (fila " + (i + 1) + ").");
                return;
            }
            if (monto > factura.getSaldo() + 0.0001) {
                error("El monto de la fila " + (i + 1) + " supera el saldo de la factura.");
                return;
            }
            if (monto > 0) {
                imputaciones.add(new ImputacionFactura(factura, monto));
            }
        }

        if (imputaciones.isEmpty()) {
            error("Ingresá al menos un monto mayor a cero.");
            return;
        }

        MedioDePago medio = crearMedio((String) comboMedio.getSelectedItem());

        OrdenDePagoDTO dto = editIndex >= 0
                ? controller.editarOrdenDePagoPorFacturas(editIndex, proveedor, imputaciones, medio)
                : controller.generarOrdenDePagoPorFacturas(proveedor, imputaciones, medio);

        JOptionPane.showMessageDialog(this,
                String.format("Orden de pago %s.%nDocumentos: %d%nBruto: %.2f%nRetenido: %.2f%nNeto a pagar: %.2f",
                        editIndex >= 0 ? "actualizada" : "generada",
                        imputaciones.size(),
                        dto.getTotalBrutoPagado(), dto.getTotalRetenido(), dto.getTotalNetoAPagar()),
                "Orden de Pago", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /** Acepta coma o punto como separador decimal. */
    private double parseMonto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(texto.trim().replace(",", "."));
    }

    /** Crea el medio de pago según la opción elegida. */
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
