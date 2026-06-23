package com.tpo.view.dialogs;

import com.tpo.controller.ItemController;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.Producto;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.catalog.Servicio;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

/** Diálogo de Ítem del catálogo (Producto o Servicio, con herencia). */
public class ItemDialog extends JDialog {

    private final JComboBox<String> comboTipo;
    private final JTextField txtCodigo;
    private final JTextField txtDescripcion;
    private final JTextField txtRubro;
    private final JTextField txtUnidad;
    private final JTextField txtPrecio;
    private final JTextField txtIva;
    // Producto
    private final JTextField txtLote;
    private final JTextField txtVencimiento;
    private final JTextField txtStock;
    // Servicio
    private final JTextField txtTipoPrestacion;

    private final JLabel lblLote;
    private final JLabel lblVencimiento;
    private final JLabel lblStock;
    private final JLabel lblTipoPrestacion;

    private final ItemController controller;
    private final int editIndex;

    /** Modo creación */
    public ItemDialog(Frame parent) {
        this(parent, null, -1);
    }

    /** Modo edición */
    public ItemDialog(Frame parent, Item existing, int editIndex) {
        super(parent, existing == null ? "Agregar Item" : "Editar Item", true);
        this.editIndex = editIndex;
        setSize(440, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = ItemController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nuevo Item" : "Editar Item", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        comboTipo = new JComboBox<>(new String[]{ItemController.PRODUCTO, ItemController.SERVICIO});
        txtCodigo = new JTextField(20);
        txtDescripcion = new JTextField(20);
        txtRubro = new JTextField(20);
        txtUnidad = new JTextField(20);
        txtPrecio = new JTextField(20);
        txtIva = new JTextField(20);
        txtLote = new JTextField(20);
        txtVencimiento = new JTextField(20);
        txtStock = new JTextField(20);
        txtTipoPrestacion = new JTextField(20);

        lblLote = new JLabel("Lote:");
        lblVencimiento = new JLabel("Vencimiento (dd/MM/yyyy):");
        lblStock = new JLabel("Stock:");
        lblTipoPrestacion = new JLabel("Tipo de Prestación:");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 5, 4, 5);

        int row = 0;
        agregarFila(form, gbc, row++, new JLabel("Tipo:"), comboTipo);
        agregarFila(form, gbc, row++, new JLabel("Código:"), txtCodigo);
        agregarFila(form, gbc, row++, new JLabel("Descripción:"), txtDescripcion);
        agregarFila(form, gbc, row++, new JLabel("Rubro:"), txtRubro);
        agregarFila(form, gbc, row++, new JLabel("Unidad de Medida:"), txtUnidad);
        agregarFila(form, gbc, row++, new JLabel("Precio Unitario:"), txtPrecio);
        agregarFila(form, gbc, row++, new JLabel("% IVA:"), txtIva);
        agregarFila(form, gbc, row++, lblLote, txtLote);
        agregarFila(form, gbc, row++, lblVencimiento, txtVencimiento);
        agregarFila(form, gbc, row++, lblStock, txtStock);
        agregarFila(form, gbc, row++, lblTipoPrestacion, txtTipoPrestacion);

        add(form, BorderLayout.CENTER);

        if (existing != null) {
            cargar(existing);
        }
        comboTipo.addActionListener(e -> actualizarVisibilidad());
        actualizarVisibilidad();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }

    private void agregarFila(JPanel form, GridBagConstraints gbc, int row, JLabel label, java.awt.Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.45;
        form.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.55;
        form.add(field, gbc);
    }

    /** Muestra los campos específicos según el tipo elegido. */
    private void actualizarVisibilidad() {
        boolean esProducto = ItemController.PRODUCTO.equals(comboTipo.getSelectedItem());
        lblLote.setVisible(esProducto); txtLote.setVisible(esProducto);
        lblVencimiento.setVisible(esProducto); txtVencimiento.setVisible(esProducto);
        lblStock.setVisible(esProducto); txtStock.setVisible(esProducto);
        lblTipoPrestacion.setVisible(!esProducto); txtTipoPrestacion.setVisible(!esProducto);
    }

    private void cargar(Item item) {
        txtCodigo.setText(String.valueOf(item.getCodigo()));
        txtDescripcion.setText(item.getDescripcion());
        if (item.getRubro() != null) txtRubro.setText(item.getRubro().getNombre());
        txtUnidad.setText(item.getUnidadMedida());
        txtPrecio.setText(String.valueOf(item.getPrecioUnitario()));
        txtIva.setText(String.valueOf(item.getPorcentajeIva()));
        if (item instanceof Producto p) {
            comboTipo.setSelectedItem(ItemController.PRODUCTO);
            txtLote.setText(p.getLote());
            if (p.getVencimiento() != null) {
                txtVencimiento.setText(new SimpleDateFormat("dd/MM/yyyy").format(p.getVencimiento()));
            }
            txtStock.setText(p.getStock());
        } else if (item instanceof Servicio s) {
            comboTipo.setSelectedItem(ItemController.SERVICIO);
            txtTipoPrestacion.setText(s.getTipoDePrestacion());
        }
    }

    private void guardar() {
        String descripcion = txtDescripcion.getText().trim();
        if (descripcion.isEmpty()) {
            error("La descripción es obligatoria.");
            return;
        }
        int codigo;
        double precio;
        float iva;
        try {
            codigo = Integer.parseInt(txtCodigo.getText().trim());
            precio = Double.parseDouble(txtPrecio.getText().trim());
            iva = Float.parseFloat(txtIva.getText().trim());
        } catch (NumberFormatException e) {
            error("Código, precio unitario e IVA deben ser valores numéricos.");
            return;
        }

        boolean esProducto = ItemController.PRODUCTO.equals(comboTipo.getSelectedItem());
        Item item;
        if (esProducto) {
            Producto p = new Producto();
            p.setLote(txtLote.getText().trim());
            p.setStock(txtStock.getText().trim());
            String venc = txtVencimiento.getText().trim();
            if (!venc.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    sdf.setLenient(false);
                    p.setVencimiento(sdf.parse(venc));
                } catch (ParseException e) {
                    error("El vencimiento debe tener el formato dd/MM/yyyy.");
                    return;
                }
            }
            item = p;
        } else {
            Servicio s = new Servicio();
            s.setTipoDePrestacion(txtTipoPrestacion.getText().trim());
            item = s;
        }

        item.setCodigo(codigo);
        item.setDescripcion(descripcion);
        String rubro = txtRubro.getText().trim();
        if (!rubro.isEmpty()) {
            item.setRubro(new Rubro(rubro));
        }
        item.setUnidadMedida(txtUnidad.getText().trim());
        item.setPrecioUnitario(precio);
        item.setPorcentajeIva(iva);

        if (editIndex >= 0) {
            controller.editarItem(editIndex, item);
        } else {
            controller.agregarItem(item);
        }
        dispose();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
