package com.supermercado.pos.ui;

import com.supermercado.pos.dao.CajaDAO;
import com.supermercado.pos.dominio.Caja;
import com.supermercado.pos.util.Conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.List;

public class CajasFrame extends JFrame {
    private final CajaDAO cajaDao = new CajaDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Número", "Nombre de Ubicación", "Estado"}, 0);
    private final JTable tabla = new JTable(modelo);
    private final JButton btnRegresar = new JButton("← Regresar al Menú");

    public CajasFrame() {
        super("Catálogo de Cajas Registradoras");
        initUI();
        cargarDatos();
    }

    private void initUI() {
        setSize(600, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRegresar.setFont(new Font("Arial", Font.BOLD, 12));
        pnlNorte.add(btnRegresar);
        add(pnlNorte, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnRegresar.addActionListener(e -> regresarMenu());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                regresarMenu();
            }
        });
    }

    private void regresarMenu() {
        dispose();
        new MenuPrincipalFrame().setVisible(true);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.obtener()) {
            List<Caja> lista = cajaDao.listarActivasSinTurno(con);
            for (Caja c : lista) {
                modelo.addRow(new Object[]{c.getId(), c.getNumero(), c.getNombre(), c.isActiva() ? "ACTIVA (DISPONIBLE)" : "INACTIVA"});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}