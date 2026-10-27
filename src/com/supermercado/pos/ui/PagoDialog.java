package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.MetodoPago;
import com.supermercado.pos.dominio.PagoDto;
import com.supermercado.pos.dominio.Venta;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.servicio.VentaService;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PagoDialog extends JDialog {
    private final BigDecimal totalAPagar;
    private final VentaService ventaService;
    private final PosFrame parentFrame;

    private final JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"EFECTIVO", "TARJETA", "MIXTO"});
    private final JTextField txtMontoTarjeta = new JTextField("0.00", 10);
    private final JTextField txtEfectivoRecibido = new JTextField(10);
    private final JLabel lblCambio = new JLabel("Q 0.00");
    private final JLabel lblRestante = new JLabel("Q 0.00");

    private final JButton btnConfirmar = new JButton("Confirmar Pago");
    private final JButton btnCancelar = new JButton("Cancelar");

    private Venta ventaRealizada = null;

    public PagoDialog(PosFrame parent, BigDecimal totalAPagar, VentaService ventaService) {
        super(parent, "Procesar Pago", true);
        this.parentFrame = parent;
        this.totalAPagar = totalAPagar.setScale(2, RoundingMode.HALF_UP);
        this.ventaService = ventaService;
        initUI();
    }

    private void initUI() {
        setSize(440, 340);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Total de la Venta:"), g);
        JLabel lblTotal = new JLabel("Q " + totalAPagar);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal.setForeground(new Color(0, 128, 0));
        g.gridx = 1; form.add(lblTotal, g);

        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Método de Pago:"), g);
        g.gridx = 1; form.add(cmbMetodo, g);

        g.gridx = 0; g.gridy = 2; form.add(new JLabel("Monto en Tarjeta:"), g);
        txtMontoTarjeta.setEnabled(false);
        g.gridx = 1; form.add(txtMontoTarjeta, g);

        g.gridx = 0; g.gridy = 3; form.add(new JLabel("Efectivo Entregado:"), g);
        g.gridx = 1; form.add(txtEfectivoRecibido, g);

        g.gridx = 0; g.gridy = 4; form.add(new JLabel("Resta por Pagar:"), g);
        lblRestante.setFont(new Font("Arial", Font.BOLD, 14));
        lblRestante.setForeground(Color.RED);
        g.gridx = 1; form.add(lblRestante, g);

        g.gridx = 0; g.gridy = 5; form.add(new JLabel("Cambio a Entregar:"), g);
        lblCambio.setFont(new Font("Arial", Font.BOLD, 16));
        lblCambio.setForeground(Color.BLUE);
        g.gridx = 1; form.add(lblCambio, g);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtns.add(btnCancelar);
        pnlBtns.add(btnConfirmar);

        add(form, BorderLayout.CENTER);
        add(pnlBtns, BorderLayout.SOUTH);

        // Listeners reactivos
        cmbMetodo.addActionListener(e -> alternarMetodo());

        DocumentListener recalculador = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcular(); }
            public void removeUpdate(DocumentEvent e) { recalcular(); }
            public void changedUpdate(DocumentEvent e) { recalcular(); }
        };
        txtMontoTarjeta.getDocument().addDocumentListener(recalculador);
        txtEfectivoRecibido.getDocument().addDocumentListener(recalculador);

        btnConfirmar.addActionListener(e -> ejecutarCobro());
        btnCancelar.addActionListener(e -> dispose());

        alternarMetodo();
    }

    private void alternarMetodo() {
        String sel = (String) cmbMetodo.getSelectedItem();
        if ("EFECTIVO".equals(sel)) {
            txtMontoTarjeta.setText("0.00");
            txtMontoTarjeta.setEnabled(false);
            txtEfectivoRecibido.setEnabled(true);
            txtEfectivoRecibido.requestFocus();
        } else if ("TARJETA".equals(sel)) {
            txtMontoTarjeta.setText(totalAPagar.toString());
            txtMontoTarjeta.setEnabled(false);
            txtEfectivoRecibido.setText("0.00");
            txtEfectivoRecibido.setEnabled(false);
        } else { // MIXTO
            txtMontoTarjeta.setEnabled(true);
            txtMontoTarjeta.setText("0.00");
            txtEfectivoRecibido.setEnabled(true);
            txtMontoTarjeta.requestFocus();
        }
        recalcular();
    }

    private void recalcular() {
        try {
            BigDecimal tarjeta = new BigDecimal(txtMontoTarjeta.getText().trim().isEmpty() ? "0" : txtMontoTarjeta.getText().trim());
            BigDecimal efectivo = new BigDecimal(txtEfectivoRecibido.getText().trim().isEmpty() ? "0" : txtEfectivoRecibido.getText().trim());

            BigDecimal restante = totalAPagar.subtract(tarjeta);
            if (restante.signum() < 0) restante = BigDecimal.ZERO;

            BigDecimal saldoPendiente = totalAPagar.subtract(tarjeta).subtract(efectivo);
            if (saldoPendiente.signum() < 0) saldoPendiente = BigDecimal.ZERO;
            lblRestante.setText("Q " + saldoPendiente.setScale(2, RoundingMode.HALF_UP));

            BigDecimal cambio = efectivo.subtract(restante);
            if (cambio.signum() < 0) cambio = BigDecimal.ZERO;
            lblCambio.setText("Q " + cambio.setScale(2, RoundingMode.HALF_UP));

        } catch (NumberFormatException ignored) {}
    }

    private void ejecutarCobro() {
        try {
            String metodoSel = (String) cmbMetodo.getSelectedItem();
            List<PagoDto> pagos = new ArrayList<>();

            if ("EFECTIVO".equals(metodoSel)) {
                BigDecimal recibido = new BigDecimal(txtEfectivoRecibido.getText().trim());
                if (recibido.compareTo(totalAPagar) < 0) {
                    JOptionPane.showMessageDialog(this, "Efectivo insuficiente", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                BigDecimal cambio = recibido.subtract(totalAPagar).setScale(2, RoundingMode.HALF_UP);
                pagos.add(new PagoDto(MetodoPago.EFECTIVO, totalAPagar, recibido, cambio));

            } else if ("TARJETA".equals(metodoSel)) {
                pagos.add(new PagoDto(MetodoPago.TARJETA, totalAPagar, totalAPagar, BigDecimal.ZERO));

            } else { // MIXTO
                BigDecimal montoTarj = new BigDecimal(txtMontoTarjeta.getText().trim());
                BigDecimal recEfe = new BigDecimal(txtEfectivoRecibido.getText().trim());

                if (montoTarj.signum() <= 0) {
                    JOptionPane.showMessageDialog(this, "El monto de tarjeta en pago mixto debe ser mayor a 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (montoTarj.compareTo(totalAPagar) >= 0) {
                    JOptionPane.showMessageDialog(this, "Para cobrar todo con tarjeta use la opción TARJETA", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BigDecimal saldoEfectivo = totalAPagar.subtract(montoTarj);
                if (recEfe.compareTo(saldoEfectivo) < 0) {
                    JOptionPane.showMessageDialog(this, "Efectivo insuficiente para cubrir los Q " + saldoEfectivo + " restantes", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BigDecimal cambio = recEfe.subtract(saldoEfectivo).setScale(2, RoundingMode.HALF_UP);
                pagos.add(new PagoDto(MetodoPago.TARJETA, montoTarj, montoTarj, BigDecimal.ZERO));
                pagos.add(new PagoDto(MetodoPago.EFECTIVO, saldoEfectivo, recEfe, cambio));
            }

            ventaRealizada = ventaService.procesarCobroMultiple(Sesion.getTurno(), parentFrame.getListaDetalles(), pagos);
            dispose();

            // Muestra ticket monoespaciado
            new TicketDialog(parentFrame, ventaRealizada).setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Formato numérico inválido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regla de Negocio", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Venta getVentaRealizada() {
        return ventaRealizada;
    }
}