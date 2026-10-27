package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Caja;
import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.servicio.TurnoService;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AperturaTurnoDialog extends JDialog {
    private final TurnoService turnoService = new TurnoService();
    private final JComboBox<CajaItem> cmbCajas = new JComboBox<>();
    private final JTextField txtFondoInicial = new JTextField("100.00", 10);
    private final JButton btnAbrir = new JButton("Abrir Turno");
    private final JButton btnCancelar = new JButton("Cancelar");
    private boolean turnoAbierto = false;

    // Helper para mostrar el nombre de la caja en el combo
    private record CajaItem(Caja caja) {
        @Override
        public String toString() {
            return "Caja " + caja.getNumero() + " - " + caja.getNombre();
        }
    }

    public AperturaTurnoDialog(Frame parent) {
        super(parent, "Apertura de Turno", true);
        initUI();
        cargarCajas();
    }

    private void initUI() {
        setSize(360, 220);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 12));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        panelForm.add(new JLabel("Seleccionar Caja:"));
        panelForm.add(cmbCajas);

        panelForm.add(new JLabel("Fondo Inicial (Q):"));
        panelForm.add(txtFondoInicial);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAbrir);

        add(panelForm, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        btnAbrir.addActionListener(e -> procesarApertura());
        btnCancelar.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(btnAbrir);
    }

    private void cargarCajas() {
        List<Caja> disponibles = turnoService.obtenerCajasDisponibles();
        cmbCajas.removeAllItems();
        for (Caja c : disponibles) {
            cmbCajas.addItem(new CajaItem(c));
        }

        if (disponibles.isEmpty()) {
            btnAbrir.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                    "No hay cajas disponibles para apertura en este momento.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void procesarApertura() {
        CajaItem item = (CajaItem) cmbCajas.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una caja.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal fondo = new BigDecimal(txtFondoInicial.getText().trim());
            Turno turno = turnoService.abrirTurno(item.caja().getId(), Sesion.getUsuario(), fondo);
            Sesion.setTurno(turno);
            turnoAbierto = true;

            JOptionPane.showMessageDialog(this,
                    "Turno #" + turno.getId() + " abierto exitosamente en Caja " + item.caja().getNumero(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El fondo inicial debe ser un valor numérico válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación de Turno", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isTurnoAbierto() {
        return turnoAbierto;
    }
}