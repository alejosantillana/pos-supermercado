package com.supermercado.pos.ui;

import com.supermercado.pos.dao.RolDAO;
import com.supermercado.pos.dao.UsuarioDAO;
import com.supermercado.pos.dominio.Rol;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.util.Conexion;
import com.supermercado.pos.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsignacionPermisosDialog extends JDialog {
    private final long usuarioDestinoId;
    private final RolDAO rolDao = new RolDAO();
    private final UsuarioDAO usuarioDao = new UsuarioDAO();

    private final JPanel pnlChecks = new JPanel();
    private final List<JCheckBox> checks = new ArrayList<>();
    private final List<Rol> rolesDisponibles = new ArrayList<>();

    public AsignacionPermisosDialog(Frame parent, long usuarioDestinoId, String username) {
        super(parent, "Asignar Roles a: " + username, true);
        this.usuarioDestinoId = usuarioDestinoId;
        initUI();
        cargarRoles();
    }

    private void initUI() {
        setSize(400, 320);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        pnlChecks.setLayout(new BoxLayout(pnlChecks, BoxLayout.Y_AXIS));
        pnlChecks.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        add(new JScrollPane(pnlChecks), BorderLayout.CENTER);

        JButton btnGuardar = new JButton("Guardar Cambios");
        JButton btnCancelar = new JButton("Cancelar");
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.add(btnCancelar);
        pnlSur.add(btnGuardar);
        add(pnlSur, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> guardarAsignacion());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cargarRoles() {
        try (Connection con = Conexion.obtener()) {
            rolesDisponibles.addAll(rolDao.listarTodos(con));
            for (Rol r : rolesDisponibles) {
                JCheckBox chk = new JCheckBox(r.getNombre() + " (Nivel " + r.getNivel() + ")");
                chk.putClientProperty("rolId", r.getId());
                chk.putClientProperty("nivel", r.getNivel());
                checks.add(chk);
                pnlChecks.add(chk);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarAsignacion() {
        Usuario actor = Sesion.getUsuario();

        // Regla 9.3: No modificar sus propios roles (anti-escalamiento)
        if (usuarioDestinoId == actor.getId()) {
            JOptionPane.showMessageDialog(this, "RN: No puedes modificar tus propios roles.", "Seguridad", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<Long> rolesElegidos = new HashSet<>();
        for (JCheckBox chk : checks) {
            if (chk.isSelected()) {
                int nivelRol = (Integer) chk.getClientProperty("nivel");
                // Regla 9.3: No asignar roles de nivel >= al del actor
                if (nivelRol >= actor.nivelMaximo()) {
                    JOptionPane.showMessageDialog(this, "RN: No puedes asignar un rol de nivel igual o superior al tuyo.", "Seguridad", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                rolesElegidos.add((Long) chk.getClientProperty("rolId"));
            }
        }

        try (Connection con = Conexion.obtener()) {
            usuarioDao.reemplazarRoles(con, usuarioDestinoId, rolesElegidos);
            JOptionPane.showMessageDialog(this, "Roles actualizados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}