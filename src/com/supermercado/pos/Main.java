package com.supermercado.pos;

import com.supermercado.pos.ui.LoginFrame;
import com.supermercado.pos.util.InicializadorDatos;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Garantiza usuarios y contraseñas fijas sin cálculos manuales
        InicializadorDatos.asegurarUsuariosBase();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}