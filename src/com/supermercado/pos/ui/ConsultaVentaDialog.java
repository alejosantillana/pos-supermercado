package com.supermercado.pos.ui;

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

public class ConsultaVentaDialog extends JDialog {
    private final VentaService ventaService = new VentaService();
    private final ReversionService reversionService = new ReversionService();

    private final JTextField txtVentaId = new JTextField(8);
    private final JButton btnBuscar = new JButton("Buscar Venta");
    private final JButton btnRevertirItem = new JButton("Revertir Ítem Seleccionado");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID Detalle", "Producto", "Precio", "Cant. Comprada", "Reversada", "Disponible"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private Venta ventaCargada;

    public ConsultaVentaDialog(Frame parent) {
        super(parent, "Consultar Ventas y Reversiones", true);
        initUI();
    }

    private void initUI() {
        setSize(750, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlNorte.add(new JLabel("No. de Ticket / Venta ID:"));
        pnlNorte.add(txtVentaId);
        pnlNorte.add(btnBuscar);
        add(pnlNorte, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlSur.add(btnRevertirItem);
        add(pnlSur, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        txtVentaId.addActionListener(e -> buscar());
        btnRevertirItem.addActionListener(e -> revertir());
    }

    private void buscar() {
        try {
            long id = Long.parseLong(txtVentaId.getText().trim());
            ventaCargada = ventaService.consultarVenta(id);
            modelo.setRowCount(0);

            for (VentaDetalle d : ventaCargada.getDetalles()) {
                int disp = d.getCantidad() - d.getCantidadReversada();
                modelo.addRow(new Object[]{
                        d.getId(),
                        d.getProducto().getNombre(),
                        "Q " + d.getPrecioUnitario(),
                        d.getCantidad(),
                        d.getCantidadReversada(),
                        disp
                });
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número de venta válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void revertir() {
        int fila = tabla.getSelectedRow();
        if (fila == -1 || ventaCargada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VentaDetalle det = ventaCargada.getDetalles().get(fila);
        int disponible = det.getCantidad() - det.getCantidadReversada();
        if (disponible <= 0) {
            JOptionPane.showMessageDialog(this, "Este producto ya fue revertido en su totalidad.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AutorizacionSupervisorDialog authDialog = new AutorizacionSupervisorDialog(null, Sesion.getUsuario());
        authDialog.setVisible(true);

        Usuario supervisor = authDialog.getSupervisorAutorizado();
        if (supervisor != null) {
            String cantStr = JOptionPane.showInputDialog(this, "¿Cuántas unidades desea revertir? (Máximo " + disponible + "):", "1");
            if (cantStr == null || cantStr.trim().isEmpty()) return;

            try {
                int cant = Integer.parseInt(cantStr.trim());
                reversionService.procesarReversion(det, cant, authDialog.getMotivo(), Sesion.getUsuario(), supervisor, Sesion.getTurno());

                JOptionPane.showMessageDialog(this, "Reversión autorizada con éxito por " + supervisor.getNombreCompleto(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                buscar(); // Refresca la tabla
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}