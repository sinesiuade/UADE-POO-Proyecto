package com.tpo.view.panels;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.function.IntConsumer;

public abstract class BaseListPanel extends JPanel {

    protected final DefaultTableModel tableModel;
    protected final JTable table;
    protected final JPanel barraSuperior;

    protected BaseListPanel(String buttonLabel, String[] columnas) {
        setLayout(new BorderLayout());

        String[] cols = new String[columnas.length + 1];
        System.arraycopy(columnas, 0, cols, 0, columnas.length);
        cols[columnas.length] = "";

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == cols.length - 1;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        int editCol = cols.length - 1;
        table.getColumnModel().getColumn(editCol).setMinWidth(90);
        table.getColumnModel().getColumn(editCol).setMaxWidth(110);
        table.getColumnModel().getColumn(editCol).setCellRenderer(new EditButtonRenderer());
        table.getColumnModel().getColumn(editCol).setCellEditor(new EditButtonEditor(this::abrirEdicion));

        barraSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        barraSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JButton btnNuevo = new JButton(buttonLabel);
        btnNuevo.addActionListener(e -> abrirCreacion());
        barraSuperior.add(btnNuevo);
        add(barraSuperior, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    protected abstract void abrirCreacion();

    protected abstract void abrirEdicion(int row);

    protected Frame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return w instanceof Frame f ? f : null;
    }

    private static class EditButtonRenderer implements TableCellRenderer {
        private final JButton button = new JButton("✏ Editar");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return button;
        }
    }

    private static class EditButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow;

        EditButtonEditor(IntConsumer onClick) {
            super(new JCheckBox());
            button = new JButton("✏ Editar");
            button.addActionListener(e -> {
                fireEditingStopped();
                onClick.accept(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "✏ Editar";
        }
    }
}
