package com.tpo.view.dialogs;

import com.tpo.controller.ItemController;
import com.tpo.controller.ProveedorController;
import com.tpo.controller.RubroController;
import com.tpo.model.Enums.CondicionImpositiva;
import com.tpo.model.entities.catalog.Item;
import com.tpo.model.entities.catalog.ItemProveedor;
import com.tpo.model.entities.catalog.Rubro;
import com.tpo.model.entities.supplier.Proveedor;
import com.tpo.model.entities.tax.CertificadoDeExclusion;
import com.tpo.model.entities.tax.TipoImpuesto;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Diálogo de alta/edición de Proveedor (datos, rubros, ítems provistos y certificados). */
public class ProveedorDialog extends JDialog {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // Datos
    private final JTextField txtCuit;
    private final JTextField txtRazonSocial;
    private final JTextField txtNombreComercial;
    private final JTextField txtDomicilio;
    private final JTextField txtTelefono;
    private final JTextField txtEmail;
    private final JComboBox<CondicionImpositiva> comboCondicion;
    private final JTextField txtLimite;

    // Rubros (N:M con el catálogo)
    private final JList<Rubro> listaRubros;

    // Ítems provistos (N:M con precio)
    private final JComboBox<Item> comboItem;
    private final JTextField txtPrecioItem;
    private final DefaultListModel<ItemProveedor> modeloItems = new DefaultListModel<>();
    private final JList<ItemProveedor> listaItems = new JList<>(modeloItems);

    // Certificados de exclusión
    private final JComboBox<TipoImpuesto> comboCertImpuesto;
    private final JTextField txtCertDesde;
    private final JTextField txtCertHasta;
    private final JTextField txtCertNumero;
    private final DefaultListModel<CertificadoDeExclusion> modeloCerts = new DefaultListModel<>();
    private final JList<CertificadoDeExclusion> listaCerts = new JList<>(modeloCerts);

    private final ProveedorController controller;
    private final Proveedor existing;
    private final int editIndex;

    public ProveedorDialog(Frame parent) {
        this(parent, null, -1);
    }

    public ProveedorDialog(Frame parent, Proveedor existing, int editIndex) {
        super(parent, existing == null ? "Nuevo Proveedor" : "Editar Proveedor", true);
        this.existing = existing;
        this.editIndex = editIndex;
        setSize(560, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        controller = ProveedorController.getInstance();

        JLabel header = new JLabel(existing == null ? "Nuevo Proveedor" : "Editar Proveedor", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(header, BorderLayout.NORTH);

        txtCuit = new JTextField(20);
        txtRazonSocial = new JTextField(20);
        txtNombreComercial = new JTextField(20);
        txtDomicilio = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtEmail = new JTextField(20);
        comboCondicion = new JComboBox<>(CondicionImpositiva.values());
        txtLimite = new JTextField(20);

        listaRubros = new JList<>(construirModeloRubros());
        listaRubros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaRubros.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v instanceof Rubro r ? r.getNombre() : String.valueOf(v));
                return this;
            }
        });

        comboItem = new JComboBox<>();
        for (Item it : ItemController.getInstance().getItems()) {
            comboItem.addItem(it);
        }
        comboItem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v instanceof Item it ? it.getCodigo() + " - " + it.getDescripcion() : "(no hay ítems)");
                return this;
            }
        });
        txtPrecioItem = new JTextField(8);

        comboCertImpuesto = new JComboBox<>(TipoImpuesto.values());
        txtCertDesde = new JTextField(8);
        txtCertHasta = new JTextField(8);
        txtCertNumero = new JTextField(6);

        if (existing != null) {
            cargar(existing);
            preseleccionarRubros();
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Datos", construirPanelDatos());
        tabs.addTab("Rubros", construirPanelRubros());
        tabs.addTab("Ítems provistos", construirPanelItems());
        tabs.addTab("Certificados", construirPanelCertificados());
        add(tabs, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        buttons.add(btnGuardar);
        buttons.add(btnCancelar);
        add(buttons, BorderLayout.SOUTH);
    }

    // --- Construcción de la UI ---------------------------------------------------------------

    private JPanel construirPanelDatos() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] labels = {"CUIT:", "Razón Social:", "Nombre Comercial:", "Domicilio:", "Teléfono:", "Email:", "Condición Impositiva:", "Límite de Deuda:"};
        JComponent[] fields = {txtCuit, txtRazonSocial, txtNombreComercial, txtDomicilio, txtTelefono, txtEmail, comboCondicion, txtLimite};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }
        return form;
    }

    private JPanel construirPanelRubros() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        panel.add(new JLabel("Rubros en los que opera (Ctrl+clic para seleccionar varios):"), BorderLayout.NORTH);
        panel.add(new JScrollPane(listaRubros), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelItems() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel alta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        alta.add(new JLabel("Ítem:"));
        alta.add(comboItem);
        alta.add(new JLabel("Precio:"));
        alta.add(txtPrecioItem);
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarItemProvisto());
        alta.add(btnAgregar);
        panel.add(alta, BorderLayout.NORTH);

        listaItems.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof ItemProveedor ip && ip.getItemBase() != null) {
                    setText(ip.getItemBase().getCodigo() + " - " + ip.getItemBase().getDescripcion()
                            + "   $" + String.format("%.2f", ip.getPrecio()));
                }
                return this;
            }
        });
        panel.add(new JScrollPane(listaItems), BorderLayout.CENTER);

        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> {
            int idx = listaItems.getSelectedIndex();
            if (idx >= 0) {
                modeloItems.remove(idx);
            }
        });
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        sur.add(btnQuitar);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPanelCertificados() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel alta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        alta.add(new JLabel("Impuesto:"));
        alta.add(comboCertImpuesto);
        alta.add(new JLabel("Desde:"));
        alta.add(txtCertDesde);
        alta.add(new JLabel("Hasta:"));
        alta.add(txtCertHasta);
        alta.add(new JLabel("N°:"));
        alta.add(txtCertNumero);
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarCertificado());
        alta.add(btnAgregar);
        panel.add(alta, BorderLayout.NORTH);

        listaCerts.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof CertificadoDeExclusion c) {
                    String desde = c.getFechaDesde() != null ? SDF.format(c.getFechaDesde()) : "?";
                    String hasta = c.getFechaHasta() != null ? SDF.format(c.getFechaHasta()) : "?";
                    setText(c.getTipoImpuesto() + "   " + desde + " a " + hasta + "   (N° " + c.getNumeroCertificado() + ")");
                }
                return this;
            }
        });
        panel.add(new JScrollPane(listaCerts), BorderLayout.CENTER);

        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> {
            int idx = listaCerts.getSelectedIndex();
            if (idx >= 0) {
                modeloCerts.remove(idx);
            }
        });
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        sur.add(btnQuitar);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    /** Modelo de rubros = catálogo ∪ rubros que el proveedor ya tenía (para no perder los no catalogados). */
    private javax.swing.DefaultListModel<Rubro> construirModeloRubros() {
        javax.swing.DefaultListModel<Rubro> modelo = new javax.swing.DefaultListModel<>();
        java.util.LinkedHashSet<String> nombres = new java.util.LinkedHashSet<>();
        for (Rubro r : RubroController.getInstance().getRubros()) {
            if (r.getNombre() != null && nombres.add(r.getNombre())) {
                modelo.addElement(r);
            }
        }
        if (existing != null) {
            for (Rubro r : existing.getRubros()) {
                if (r.getNombre() != null && nombres.add(r.getNombre())) {
                    modelo.addElement(r);
                }
            }
        }
        return modelo;
    }

    // --- Carga de datos en modo edición ------------------------------------------------------

    private void cargar(Proveedor p) {
        if (p.getCuit() > 0) txtCuit.setText(String.valueOf(p.getCuit()));
        txtRazonSocial.setText(p.getRazonSocial());
        txtNombreComercial.setText(p.getNombreComercial());
        txtDomicilio.setText(p.getDomicilio());
        if (p.getTelefono() > 0) txtTelefono.setText(String.valueOf(p.getTelefono()));
        txtEmail.setText(p.getEmail());
        if (p.getCondicionImpositiva() != null) comboCondicion.setSelectedItem(p.getCondicionImpositiva());
        txtLimite.setText(String.valueOf(p.getLimiteDeudaAutorizado()));
        for (ItemProveedor ip : p.getItemsProvistos()) {
            modeloItems.addElement(ip);
        }
        for (CertificadoDeExclusion c : p.getCertificadosDeExclusion()) {
            modeloCerts.addElement(c);
        }
    }

    /** Tras construir la lista de rubros, marca los que el proveedor ya tenía. */
    private void preseleccionarRubros() {
        if (existing == null) {
            return;
        }
        java.util.Set<String> propios = new java.util.HashSet<>();
        for (Rubro r : existing.getRubros()) {
            if (r.getNombre() != null) {
                propios.add(r.getNombre());
            }
        }
        for (int i = 0; i < listaRubros.getModel().getSize(); i++) {
            Rubro r = listaRubros.getModel().getElementAt(i);
            if (r.getNombre() != null && propios.contains(r.getNombre())) {
                listaRubros.addSelectionInterval(i, i);
            }
        }
    }

    // --- Acciones de las sub-listas ----------------------------------------------------------

    private void agregarItemProvisto() {
        Item item = (Item) comboItem.getSelectedItem();
        if (item == null) {
            error("No hay ítems en el catálogo para asociar.");
            return;
        }
        double precio;
        try {
            precio = Double.parseDouble(txtPrecioItem.getText().trim());
        } catch (NumberFormatException e) {
            error("El precio del ítem debe ser numérico.");
            return;
        }
        for (int i = 0; i < modeloItems.size(); i++) {
            if (modeloItems.get(i).getItemBase() == item) {
                error("Ese ítem ya está en la lista.");
                return;
            }
        }
        modeloItems.addElement(new ItemProveedor(precio, item, null));
        txtPrecioItem.setText("");
    }

    private void agregarCertificado() {
        Date desde;
        Date hasta;
        int numero;
        try {
            SDF.setLenient(false);
            desde = SDF.parse(txtCertDesde.getText().trim());
            hasta = SDF.parse(txtCertHasta.getText().trim());
        } catch (ParseException e) {
            error("Las fechas del certificado deben tener el formato dd/MM/yyyy.");
            return;
        }
        if (hasta.before(desde)) {
            error("La fecha 'Hasta' no puede ser anterior a 'Desde'.");
            return;
        }
        try {
            numero = Integer.parseInt(txtCertNumero.getText().trim());
        } catch (NumberFormatException e) {
            error("El número de certificado debe ser numérico.");
            return;
        }
        TipoImpuesto tipo = (TipoImpuesto) comboCertImpuesto.getSelectedItem();
        modeloCerts.addElement(new CertificadoDeExclusion(tipo, desde, hasta, numero));
        txtCertDesde.setText("");
        txtCertHasta.setText("");
        txtCertNumero.setText("");
    }

    // --- Guardado ----------------------------------------------------------------------------

    private void guardar() {
        String razonSocial = txtRazonSocial.getText().trim();
        if (razonSocial.isEmpty()) {
            error("La razón social es obligatoria.");
            return;
        }
        long cuit = 0;
        long telefono = 0;
        int limite = 0;
        try {
            if (!txtCuit.getText().trim().isEmpty()) cuit = Long.parseLong(txtCuit.getText().trim());
            if (!txtTelefono.getText().trim().isEmpty()) telefono = Long.parseLong(txtTelefono.getText().trim());
            if (!txtLimite.getText().trim().isEmpty()) limite = Integer.parseInt(txtLimite.getText().trim());
        } catch (NumberFormatException e) {
            error("CUIT, teléfono y límite deben ser numéricos (sin guiones ni puntos).");
            return;
        }

        if (controller.existeProveedor(cuit, razonSocial, editIndex)) {
            error(cuit > 0
                    ? "Ya existe un proveedor con el CUIT " + cuit + "."
                    : "Ya existe un proveedor con esa razón social.");
            return;
        }

        Proveedor p = new Proveedor();
        p.setCuit(cuit);
        p.setRazonSocial(razonSocial);
        p.setNombreComercial(txtNombreComercial.getText().trim());
        p.setDomicilio(txtDomicilio.getText().trim());
        p.setTelefono(telefono);
        p.setEmail(txtEmail.getText().trim());
        p.setCondicionImpositiva((CondicionImpositiva) comboCondicion.getSelectedItem());
        p.setLimiteDeudaAutorizado(limite);

        // Conserva datos fiscales no editables en este diálogo (no se pierden al editar).
        if (existing != null) {
            p.setNroInscripcionFiscal(existing.getNroInscripcionFiscal());
            p.setFechaInicioAct(existing.getFechaInicioAct());
            p.setDeudaActual(existing.getDeudaActual());
        }

        // Rubros seleccionados (N:M con el catálogo).
        for (Rubro r : listaRubros.getSelectedValuesList()) {
            p.getRubros().add(new Rubro(r.getNombre()));
        }

        // Ítems provistos (N:M, con su precio); fija el proveedor de la relación.
        for (int i = 0; i < modeloItems.size(); i++) {
            ItemProveedor ip = modeloItems.get(i);
            ip.setProveedor(p);
            p.getItemsProvistos().add(ip);
        }

        // Certificados de exclusión.
        for (int i = 0; i < modeloCerts.size(); i++) {
            p.getCertificadosDeExclusion().add(modeloCerts.get(i));
        }

        if (editIndex >= 0) {
            controller.editarProveedor(editIndex, p);
        } else {
            controller.agregarProveedor(p);
        }
        dispose();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
