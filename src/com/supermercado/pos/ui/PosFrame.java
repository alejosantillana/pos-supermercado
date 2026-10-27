package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Producto;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.dominio.Venta;
import com.supermercado.pos.dominio.VentaDetalle;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.servicio.ReversionService;
import com.supermercado.pos.servicio.VentaService;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PosFrame extends JFrame {
    private final VentaService ventaService = new VentaService();
    private final ReversionService reversionService = new ReversionService();
    private final List<VentaDetalle> listaDetalles = new ArrayList<>();

    private final JTextField txtCodigo = new JTextField(12);
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnRevertir = new JButton("Revertir Selección");
    private final JButton btnConsultar = new JButton("Consultar Ventas");
    private final JButton btnCorteIntermedio = new JButton("Corte Intermedio");
    private final JButton btnCerrarTurno = new JButton("Cerrar Turno");

    private final JLabel lblTotal = new JLabel("Q 0.00", SwingConstants.RIGHT);
    private final JButton btnCobrar = new JButton("Cobrar [F12]");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Código", "Producto", "Precio", "Cant.", "Subtotal"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PosFrame() {
        super("Punto de Venta - " + Sesion.getUsuario().getNombreCompleto()
                + " | Caja: " + Sesion.getTurno().getCaja().getNumero());
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        pnlNorte.add(new JLabel("Código:"));
        pnlNorte.add(txtCodigo);
        pnlNorte.add(new JLabel("Cant:"));
        pnlNorte.add(spnCantidad);
        pnlNorte.add(btnAgregar);
        pnlNorte.add(new JSeparator(JSeparator.VERTICAL));
        pnlNorte.add(btnRevertir);
        pnlNorte.add(btnConsultar);
        pnlNorte.add(btnCorteIntermedio);
        pnlNorte.add(new JSeparator(JSeparator.VERTICAL));

        btnCerrarTurno.setForeground(Color.RED);
        pnlNorte.add(btnCerrarTurno);

        add(pnlNorte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new BorderLayout(15, 15));
        pnlSur.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        lblTotal.setFont(new Font("Arial", Font.BOLD, 32));
        lblTotal.setForeground(new Color(0, 128, 0));

        btnCobrar.setFont(new Font("Arial", Font.BOLD, 16));
        btnCobrar.setPreferredSize(new Dimension(180, 50));

        pnlSur.add(lblTotal, BorderLayout.CENTER);
        pnlSur.add(btnCobrar, BorderLayout.EAST);
        add(pnlSur, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarProducto());
        txtCodigo.addActionListener(e -> agregarProducto());
        btnCobrar.addActionListener(e -> abrirModalCobro());
        btnRevertir.addActionListener(e -> abrirModalReversion());
        btnConsultar.addActionListener(e -> new ConsultaVentaDialog(this).setVisible(true));
        btnCorteIntermedio.addActionListener(e -> new CorteIntermedioDialog(this).setVisible(true));

        btnCerrarTurno.addActionListener(e -> {
            CierreTurnoDialog dlg = new CierreTurnoDialog(this);
            dlg.setVisible(true);
            if (dlg.isTurnoCerrado()) {
                dispose();
                new MenuPrincipalFrame().setVisible(true); // Vuelve al menú principal limpio
            }

        });

        getRootPane().registerKeyboardAction(e -> abrirModalCobro(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void agregarProducto() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) return;

        try {
            Producto p = ventaService.buscarPorCodigo(codigo);
            int cant = (Integer) spnCantidad.getValue();

            BigDecimal precio = p.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cant)).setScale(2, RoundingMode.HALF_UP);

            VentaDetalle det = new VentaDetalle();
            det.setProducto(p);
            det.setCantidad(cant);
            det.setPrecioUnitario(precio);
            det.setSubtotal(subtotal);

            listaDetalles.add(det);
            modeloTabla.addRow(new Object[]{p.getId(), p.getCodigoBarras(), p.getNombre(), "Q " + precio, cant, "Q " + subtotal});

            actualizarGranTotal();
            txtCodigo.setText("");
            spnCantidad.setValue(1);
            txtCodigo.requestFocus();
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarGranTotal() {
        BigDecimal total = listaDetalles.stream()
                .map(VentaDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText("Q " + total.setScale(2, RoundingMode.HALF_UP));
    }

    private void abrirModalCobro() {
        if (listaDetalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal total = listaDetalles.stream()
                .map(VentaDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PagoDialog pagoDlg = new PagoDialog(this, total, ventaService);
        pagoDlg.setVisible(true);

        if (pagoDlg.getVentaRealizada() != null) {
            listaDetalles.clear();
            modeloTabla.setRowCount(0);
            actualizarGranTotal();
            txtCodigo.requestFocus();
        }
    }

    private void abrirModalReversion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para quitar o revertir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VentaDetalle detalle = listaDetalles.get(fila);
        if (detalle.getId() == null) {
            listaDetalles.remove(fila);
            modeloTabla.removeRow(fila);
            actualizarGranTotal();
        }
    }

    public List<VentaDetalle> getListaDetalles() {
        return listaDetalles;
    }
}