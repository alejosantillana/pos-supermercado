package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.servicio.TurnoService;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class CierreTurnoDialog extends JDialog {
    private final TurnoService turnoService = new TurnoService();
    private final JTextField txtDeclarado = new JTextField(10);
    private final JButton btnCerrar = new JButton("Confirmar Cierre de Turno");
    private final JButton btnCancelar = new JButton("Cancelar");
    private boolean turnoCerrado = false;

    public CierreTurnoDialog(Frame parent) {
        super(parent, "Arqueo de Caja - Cierre de Turno", true);
        initUI();
    }

    private void initUI() {
        setSize(400, 210);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel pnl = new JPanel(new GridLayout(2, 1, 8, 8));
        pnl.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel lblInfo = new JLabel("<html><b>Arqueo Ciego:</b> Ingrese el total en efectivo contado físicamente en gaveta:</html>");
        pnl.add(lblInfo);

        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlInput.add(new JLabel("Efectivo Físico (Q):"));
        pnlInput.add(txtDeclarado);
        pnl.add(pnlInput);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtns.add(btnCancelar);
        pnlBtns.add(btnCerrar);

        add(pnl, BorderLayout.CENTER);
        add(pnlBtns, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> ejecutarCierre());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnCerrar);
    }

    private void ejecutarCierre() {
        String val = txtDeclarado.getText().trim();
        if (val.isEmpty()) return;

        try {
            BigDecimal declarado = new BigDecimal(val);
            Turno cerrado = turnoService.cerrarTurno(Sesion.getTurno(), declarado);
            turnoCerrado = true;

            String detalle = String.format(
                    "Resumen de Cierre Turno #%d\n--------------------------------\n" +
                            "Efectivo Declarado: Q %.2f\n" +
                            "Efectivo Esperado:  Q %.2f\n" +
                            "Diferencia:         Q %.2f (%s)\n--------------------------------",
                    cerrado.getId(),
                    cerrado.getEfectivoDeclarado(),
                    cerrado.getEfectivoEsperado(),
                    cerrado.getDiferencia(),
                    cerrado.getDiferencia().compareTo(BigDecimal.ZERO) == 0 ? "CUADRADO" :
                            cerrado.getDiferencia().compareTo(BigDecimal.ZERO) > 0 ? "SOBRANTE" : "FALTANTE"
            );

            JOptionPane.showMessageDialog(this, detalle, "Arqueo Finalizado", JOptionPane.INFORMATION_MESSAGE);
            Sesion.setTurno(null);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto numérico no válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isTurnoCerrado() {
        return turnoCerrado;
    }
}