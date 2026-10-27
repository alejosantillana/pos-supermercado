package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.excepcion.AutorizacionException;
import com.supermercado.pos.servicio.ReversionService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class AutorizacionSupervisorDialog extends JDialog {
    private final ReversionService reversionService = new ReversionService();
    private final Usuario cajeroActual;
    private Usuario supervisorAutorizado = null;

    private final JTextField txtUsuario = new JTextField(12);
    private final JPasswordField txtClave = new JPasswordField(12);
    private final JTextArea txtMotivo = new JTextArea(3, 15);
    private final JButton btnAutorizar = new JButton("Autorizar");
    private final JButton btnCancelar = new JButton("Cancelar");

    public AutorizacionSupervisorDialog(Frame parent, Usuario cajeroActual) {
        super(parent, "Autorización de Supervisor Requerida", true);
        this.cajeroActual = cajeroActual;
        initUI();
    }

    private void initUI() {
        setSize(360, 280);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Supervisor:"), g);
        g.gridx = 1; panel.add(txtUsuario, g);

        g.gridx = 0; g.gridy = 1; panel.add(new JLabel("Contraseña:"), g);
        g.gridx = 1; panel.add(txtClave, g);

        g.gridx = 0; g.gridy = 2; panel.add(new JLabel("Motivo:"), g);
        txtMotivo.setLineWrap(true);
        g.gridx = 1; panel.add(new JScrollPane(txtMotivo), g);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtns.add(btnCancelar);
        pnlBtns.add(btnAutorizar);

        add(panel, BorderLayout.CENTER);
        add(pnlBtns, BorderLayout.SOUTH);

        btnAutorizar.addActionListener(e -> autorizar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnAutorizar);
    }

    private void autorizar() {
        String motivo = txtMotivo.getText().trim();
        if (motivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el motivo de la reversión.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        char[] pass = txtClave.getPassword();
        try {
            supervisorAutorizado = reversionService.validarSupervisor(txtUsuario.getText().trim(), pass, cajeroActual);
            dispose();
        } catch (AutorizacionException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(pass, '\0');
            txtClave.setText("");
        }
    }

    public Usuario getSupervisorAutorizado() {
        return supervisorAutorizado;
    }

    public String getMotivo() {
        return txtMotivo.getText().trim();
    }
}