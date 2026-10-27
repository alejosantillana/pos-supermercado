package com.supermercado.pos.ui;

import com.supermercado.pos.dao.MovimientoCajaDAO;
import com.supermercado.pos.dao.TurnoDAO;
import com.supermercado.pos.dominio.MovimientoCaja;
import com.supermercado.pos.dominio.TipoMovimiento;
import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.servicio.ReversionService;
import com.supermercado.pos.util.Conexion;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Properties;

public class CorteIntermedioDialog extends JDialog {
    private final TurnoDAO turnoDao = new TurnoDAO();
    private final MovimientoCajaDAO movimientoDao = new MovimientoCajaDAO();
    private final ReversionService reversionService = new ReversionService();

    private final JLabel lblEsperado = new JLabel("Q 0.00");
    private final JLabel lblMinimo = new JLabel("Q 0.00");
    private final JTextField txtMontoRetiro = new JTextField(10);
    private final JButton btnRetirar = new JButton("Entregar a Bóveda");
    private final JButton btnCancelar = new JButton("Cancelar");

    private BigDecimal efectivoEsperadoActual = BigDecimal.ZERO;
    private BigDecimal efectivoMinimo = new BigDecimal("500.00");

    public CorteIntermedioDialog(Frame parent) {
        super(parent, "Corte Intermedio - Retiro a Bóveda", true);
        cargarConfiguracion();
        initUI();
        cargarValoresCaja();
    }

    private void cargarConfiguracion() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(fis);
            String val = props.getProperty("efectivoMinimo", "500.00");
            efectivoMinimo = new BigDecimal(val.trim());
        } catch (Exception e) {
            efectivoMinimo = new BigDecimal("500.00");
        }
    }

    private void initUI() {
        setSize(430, 260);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel pnl = new JPanel(new GridLayout(4, 2, 8, 8));
        pnl.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        pnl.add(new JLabel("Efectivo Actual Esperado:"));
        lblEsperado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEsperado.setForeground(new Color(0, 128, 0));
        pnl.add(lblEsperado);

        pnl.add(new JLabel("Mínimo Operativo en Caja:"));
        lblMinimo.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinimo.setText("Q " + efectivoMinimo);
        pnl.add(lblMinimo);

        pnl.add(new JLabel("Monto a Entregar (Q):"));
        pnl.add(txtMontoRetiro);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtns.add(btnCancelar);
        pnlBtns.add(btnRetirar);

        add(pnl, BorderLayout.CENTER);
        add(pnlBtns, BorderLayout.SOUTH);

        btnRetirar.addActionListener(e -> procesarRetiro());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cargarValoresCaja() {
        try (Connection con = Conexion.obtener()) {
            efectivoEsperadoActual = turnoDao.calcularEfectivoEsperado(con, Sesion.getTurno().getId());
            lblEsperado.setText("Q " + efectivoEsperadoActual);
        } catch (Exception e) {
            lblEsperado.setText("Error");
        }
    }

    private void procesarRetiro() {
        try {
            BigDecimal monto = new BigDecimal(txtMontoRetiro.getText().trim());
            if (monto.signum() <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal remanente = efectivoEsperadoActual.subtract(monto);
            if (remanente.compareTo(efectivoMinimo) < 0) {
                throw new ReglaNegocioException("RN-05: El retiro dejaría la caja con Q " + remanente + ". Debe conservar al menos Q " + efectivoMinimo);
            }

            // Pide credenciales del supervisor que recibe el dinero
            AutorizacionSupervisorDialog auth = new AutorizacionSupervisorDialog(null, Sesion.getUsuario());
            auth.setVisible(true);

            Usuario sup = auth.getSupervisorAutorizado();
            if (sup != null) {
                Turno turno = Sesion.getTurno();
                try (Connection con = Conexion.obtener()) {
                    con.setAutoCommit(false);
                    try {
                        MovimientoCaja m = MovimientoCaja.de(turno, TipoMovimiento.RETIRO_BOVEDA, monto, sup);
                        m.setReferencia("BOVEDA-" + System.currentTimeMillis());
                        movimientoDao.insertar(con, m);
                        con.commit();

                        JOptionPane.showMessageDialog(this,
                                "Recibo de Retiro a Bóveda\nFolio: " + m.getReferencia() + "\nMonto: Q " + monto +
                                        "\nEntregado por: " + Sesion.getUsuario().getNombreCompleto() +
                                        "\nRecibido por: " + sup.getNombreCompleto(),
                                "Comprobante de Retiro", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } catch (Exception ex) {
                        con.rollback();
                        throw ex;
                    }
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regla de Negocio", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}