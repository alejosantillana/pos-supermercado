package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.servicio.TurnoService;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class MenuPrincipalFrame extends JFrame {
    private final TurnoService turnoService = new TurnoService();

    private final JButton btnPos = new JButton("1. Punto de Venta / Caja");
    private final JButton btnProductos = new JButton("2. Catálogo de Productos y Stock");
    private final JButton btnCajas = new JButton("3. Catálogo de Cajas");
    private final JButton btnUsuarios = new JButton("4. Administración de Usuarios");
    private final JButton btnRoles = new JButton("5. Roles y Permisos");
    private final JButton btnCerrarSesion = new JButton("Cerrar Sesión");

    public MenuPrincipalFrame() {
        super("Menú Principal - Sistema POS");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(15, 15));

        Usuario u = Sesion.getUsuario();

        // 1. Encabezado con información del usuario
        JPanel pnlNorte = new JPanel(new GridLayout(2, 1));
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        JLabel lblBienvenida = new JLabel("Bienvenido, " + u.getNombreCompleto(), SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel lblRol = new JLabel("Nivel Jerárquico: " + u.nivelMaximo(), SwingConstants.CENTER);
        lblRol.setForeground(Color.GRAY);
        pnlNorte.add(lblBienvenida);
        pnlNorte.add(lblRol);
        add(pnlNorte, BorderLayout.NORTH);

        // 2. Botones del menú
        JPanel pnlCentro = new JPanel(new GridLayout(5, 1, 8, 8));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        pnlCentro.add(btnPos);
        pnlCentro.add(btnProductos);
        pnlCentro.add(btnCajas);
        pnlCentro.add(btnUsuarios);
        pnlCentro.add(btnRoles);
        add(pnlCentro, BorderLayout.CENTER);

        // 3. Botón inferior de cerrar sesión
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.add(btnCerrarSesion);
        add(pnlSur, BorderLayout.SOUTH);

        // 4. Habilitar botones según permisos (Fase 3.4)[cite: 25]
        btnPos.setEnabled(u.tienePermiso("VENTA_COBRAR"));
        btnProductos.setEnabled(u.tienePermiso("CATALOGO_ADMIN"));
        btnCajas.setEnabled(u.tienePermiso("CATALOGO_ADMIN"));
        btnUsuarios.setEnabled(u.tienePermiso("USUARIO_ADMIN"));
        btnRoles.setEnabled(u.tienePermiso("ROL_ADMIN"));

        // ==========================================
        // AQUÍ VAN LOS LISTENERS (UN SOLO BLOQUE)
        // ==========================================
        btnPos.addActionListener(e -> abrirModuloCaja());

        btnProductos.addActionListener(e -> {
            dispose();
            new ProductosFrame().setVisible(true);
        });

        btnCajas.addActionListener(e -> {
            dispose();
            new CajasFrame().setVisible(true);
        });

        btnUsuarios.addActionListener(e -> {
            dispose();
            new UsuariosFrame().setVisible(true);
        });

        btnRoles.addActionListener(e -> {
            dispose();
            new RolesFrame().setVisible(true);
        });

        btnCerrarSesion.addActionListener(e -> {
            Sesion.cerrar();
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void abrirModuloCaja() {
        Usuario u = Sesion.getUsuario();
        Optional<Turno> turnoActivo = turnoService.obtenerTurnoActivo(u);

        if (turnoActivo.isPresent()) {
            Sesion.setTurno(turnoActivo.get());
            dispose();
            new PosFrame().setVisible(true);
        } else {
            AperturaTurnoDialog dialog = new AperturaTurnoDialog(this);
            dialog.setVisible(true);
            if (dialog.isTurnoAbierto()) {
                dispose();
                new PosFrame().setVisible(true);
            }
        }
    }
}