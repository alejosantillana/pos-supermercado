package com.supermercado.pos.ui;

import com.supermercado.pos.dao.ProductoDAO;
import com.supermercado.pos.dominio.Producto;
import com.supermercado.pos.util.Conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class ProductosFrame extends JFrame {
    private final ProductoDAO productoDao = new ProductoDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Código de Barras", "Nombre / Descripción", "Precio (Q)", "Stock Actual", "Estado"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton btnRegresar = new JButton("← Regresar al Menú");
    private final JButton btnActualizar = new JButton("Recargar Inventario");
    private final JButton btnModificarStock = new JButton("Ajustar Stock");
    private final JButton btnNuevo = new JButton("Registrar Nuevo Producto");

    public ProductosFrame() {
        super("Catálogo de Productos e Inventario - Administración");
        initUI();
        cargarDatos();
    }

    private void initUI() {
        setSize(900, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRegresar.setFont(new Font("Arial", Font.BOLD, 12));
        pnlNorte.add(btnRegresar);
        pnlNorte.add(new JSeparator(JSeparator.VERTICAL));
        pnlNorte.add(btnActualizar);
        pnlNorte.add(btnModificarStock);
        pnlNorte.add(btnNuevo);
        add(pnlNorte, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnRegresar.addActionListener(e -> regresarMenu());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                regresarMenu();
            }
        });

        btnActualizar.addActionListener(e -> cargarDatos());
        btnModificarStock.addActionListener(e -> ajustarStock());
        btnNuevo.addActionListener(e -> registrarNuevo());
    }

    private void regresarMenu() {
        dispose();
        new MenuPrincipalFrame().setVisible(true);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.obtener()) {
            List<Producto> lista = productoDao.listarTodos(con);
            for (Producto p : lista) {
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getCodigoBarras(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.isActivo() ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar catálogo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajustarStock() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para modificar existencias.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id = (Long) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 2);
        int stockActual = (Integer) modelo.getValueAt(fila, 4);

        String entrada = JOptionPane.showInputDialog(this,
                "Producto: " + nombre + "\nStock actual: " + stockActual + "\nIngrese la nueva cantidad de stock:",
                String.valueOf(stockActual));

        if (entrada == null || entrada.trim().isEmpty()) return;

        try {
            int nuevo = Integer.parseInt(entrada.trim());
            if (nuevo < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = Conexion.obtener()) {
                productoDao.actualizarStockDirecto(con, id, nuevo);
                JOptionPane.showMessageDialog(this, "Stock actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Número no válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarNuevo() {
        JTextField txtCodigo = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtPrecio = new JTextField();
        JTextField txtStock = new JTextField();

        Object[] formulario = {
                "Código de Barras:", txtCodigo,
                "Nombre del Producto:", txtNombre,
                "Precio de Venta (Q):", txtPrecio,
                "Stock Inicial:", txtStock
        };

        int res = JOptionPane.showConfirmDialog(this, formulario, "Nuevo Producto", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Producto p = new Producto();
                p.setCodigoBarras(txtCodigo.getText().trim());
                p.setNombre(txtNombre.getText().trim());
                p.setPrecio(new BigDecimal(txtPrecio.getText().trim()));
                p.setStock(Integer.parseInt(txtStock.getText().trim()));

                if (p.getCodigoBarras().isEmpty() || p.getNombre().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Código y nombre son obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try (Connection con = Conexion.obtener()) {
                    productoDao.guardar(con, p);
                    JOptionPane.showMessageDialog(this, "Producto registrado correctamente con ID #" + p.getId(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatos();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}