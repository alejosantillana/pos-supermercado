package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.excepcion.AutorizacionException;
import com.supermercado.pos.servicio.AuthService;
import com.supermercado.pos.servicio.TurnoService;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final JTextField txtUsuario = new JTextField(15);
    private final JPasswordField txtClave = new JPasswordField(15);
    private final JLabel lblError = new JLabel(" ");
    private final JButton btnIngresar = new JButton("Ingresar");

    public LoginFrame() {
        super("Inicio de Sesión - Sistema POS");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Acceso al Sistema POS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(txtClave, gbc);

        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(lblError, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        add(panel);
        getRootPane().setDefaultButton(btnIngresar);

        btnIngresar.addActionListener(e -> ejecutarLogin());
    }

    private void ejecutarLogin() {
        char[] clave = txtClave.getPassword();
        try {
            Usuario u = authService.autenticar(txtUsuario.getText().trim(), clave);
            Sesion.setUsuario(u);
            dispose();

            // Abre el Menú Principal según permisos
            new MenuPrincipalFrame().setVisible(true);

        } catch (AutorizacionException ex) {
            lblError.setText(ex.getMessage());
        } finally {
            Arrays.fill(clave, '\0');
            txtClave.setText("");
        }
    }
}