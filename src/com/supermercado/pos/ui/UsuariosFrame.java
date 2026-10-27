package com.supermercado.pos.ui;

import com.supermercado.pos.dao.RolDAO;
import com.supermercado.pos.dao.UsuarioDAO;
import com.supermercado.pos.dominio.Rol;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.util.Conexion;
import com.supermercado.pos.util.Seguridad;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.List;

public class UsuariosFrame extends JFrame {
    private final UsuarioDAO usuarioDao = new UsuarioDAO();
    private final RolDAO rolDao = new RolDAO();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Username", "Nombre Completo", "Estado"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
    private final JTextField txtFiltro = new JTextField(12);

    private final JButton btnRegresar = new JButton("← Regresar al Menú");
    private final JButton btnNuevo = new JButton("Nuevo Usuario");
    private final JButton btnToggleEstado = new JButton("Activar / Desactivar");
    private final JButton btnAsignarRoles = new JButton("Asignar Roles / Permisos");

    public UsuariosFrame() {
        super("Administración de Usuarios - Sistema POS");
        initUI();
        cargarUsuarios();
    }

    private void initUI() {
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        tabla.setRowSorter(sorter);

        // Barra superior de navegación y acciones
        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRegresar.setFont(new Font("Arial", Font.BOLD, 12));
        pnlNorte.add(btnRegresar);
        pnlNorte.add(new JSeparator(JSeparator.VERTICAL));
        pnlNorte.add(new JLabel("Filtrar:"));
        pnlNorte.add(txtFiltro);
        pnlNorte.add(new JSeparator(JSeparator.VERTICAL));
        pnlNorte.add(btnNuevo);
        pnlNorte.add(btnToggleEstado);
        pnlNorte.add(btnAsignarRoles);
        add(pnlNorte, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Acciones
        btnRegresar.addActionListener(e -> regresarMenu());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                regresarMenu();
            }
        });

        txtFiltro.addActionListener(e -> filtrar());
        btnNuevo.addActionListener(e -> abrirModalNuevoUsuario());
        btnToggleEstado.addActionListener(e -> alternarEstadoUsuario());
        btnAsignarRoles.addActionListener(e -> abrirModalAsignacion());
    }

    private void regresarMenu() {
        dispose();
        new MenuPrincipalFrame().setVisible(true);
    }

    private void filtrar() {
        String texto = txtFiltro.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }

    private void cargarUsuarios() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.obtener()) {
            List<Usuario> lista = usuarioDao.listarTodos(con);
            for (Usuario u : lista) {
                modelo.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getNombreCompleto(),
                        u.isActivo() ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarEstadoUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id = (Long) tabla.getValueAt(fila, 0);
        String estado = (String) tabla.getValueAt(fila, 3);
        boolean nuevoEstado = !"ACTIVO".equals(estado);

        if (id == Sesion.getUsuario().getId()) {
            JOptionPane.showMessageDialog(this, "No puedes desactivar tu propio usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = Conexion.obtener()) {
            usuarioDao.cambiarEstado(con, id, nuevoEstado);
            JOptionPane.showMessageDialog(this, "Estado del usuario actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalNuevoUsuario() {
        JTextField txtUser = new JTextField();
        JPasswordField txtClave = new JPasswordField();
        JTextField txtNombre = new JTextField();
        JComboBox<Rol> cmbRoles = new JComboBox<>();

        try (Connection con = Conexion.obtener()) {
            for (Rol r : rolDao.listarTodos(con)) {
                cmbRoles.addItem(r);
            }
        } catch (Exception ignored) {}

        Object[] form = {
                "Username (sin espacios):", txtUser,
                "Contraseña (mínimo 8 caracteres):", txtClave,
                "Nombre Completo:", txtNombre,
                "Rol Inicial:", cmbRoles
        };

        int res = JOptionPane.showConfirmDialog(this, form, "Crear Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String username = txtUser.getText().trim();
            String password = new String(txtClave.getPassword());
            String nombre = txtNombre.getText().trim();
            Rol rolSel = (Rol) cmbRoles.getSelectedItem();

            if (username.contains(" ") || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El username no debe contener espacios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 8 caracteres.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = Conexion.obtener()) {
                con.setAutoCommit(false);
                try {
                    String hash = Seguridad.hash(password.toCharArray());
                    String sql = "INSERT INTO usuario (username, password_hash, nombre_completo, activo) VALUES (?, ?, ?, 1)";
                    long nuevoId;
                    try (java.sql.PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
                        ps.setString(1, username);
                        ps.setString(2, hash);
                        ps.setString(3, nombre);
                        ps.executeUpdate();
                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            rs.next();
                            nuevoId = rs.getLong(1);
                        }
                    }

                    if (rolSel != null) {
                        try (java.sql.PreparedStatement psRol = con.prepareStatement("INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)")) {
                            psRol.setLong(1, nuevoId);
                            psRol.setLong(2, rolSel.getId());
                            psRol.executeUpdate();
                        }
                    }

                    con.commit();
                    JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al crear usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalAsignacion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para asignar roles.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id = (Long) tabla.getValueAt(fila, 0);
        String username = (String) tabla.getValueAt(fila, 1);

        new AsignacionPermisosDialog(this, id, username).setVisible(true);
    }
}