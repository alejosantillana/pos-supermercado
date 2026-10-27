package com.supermercado.pos.ui;

import com.supermercado.pos.dao.RolDAO;
import com.supermercado.pos.dominio.Permiso;
import com.supermercado.pos.dominio.Rol;
import com.supermercado.pos.util.Conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RolesFrame extends JFrame {
    private final RolDAO rolDao = new RolDAO();

    // Tabla izquierda: Roles
    private final DefaultTableModel modRoles = new DefaultTableModel(new Object[]{"ID", "Rol", "Nivel"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tblRoles = new JTable(modRoles);

    // Tabla derecha: Matriz de Permisos interactiva con CheckBox (Fase 9.2)
    private final DefaultTableModel modPermisos = new DefaultTableModel(
            new Object[]{"Asignado", "Código", "Descripción"}, 0
    ) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0; // Solo la casilla del CheckBox se puede editar
        }
    };
    private final JTable tblPermisos = new JTable(modPermisos);

    private final JButton btnRegresar = new JButton("← Regresar al Menú");
    private final JButton btnNuevoRol = new JButton("Nuevo Rol");
    private final JButton btnGuardarPermisos = new JButton("Guardar Cambios de Permisos");
    private final JLabel lblRolSeleccionado = new JLabel("Seleccione un rol de la izquierda para ver su matriz");

    public RolesFrame() {
        super("Administración de Roles y Matriz de Permisos");
        initUI();
        cargarRoles();
    }

    private void initUI() {
        setSize(960, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRegresar.setFont(new Font("Arial", Font.BOLD, 12));
        pnlNorte.add(btnRegresar);
        pnlNorte.add(new JSeparator(JSeparator.VERTICAL));
        pnlNorte.add(btnNuevoRol);
        add(pnlNorte, BorderLayout.NORTH);

        // Panel derecho con título y botón de guardar
        JPanel pnlDerecho = new JPanel(new BorderLayout(5, 5));
        JPanel pnlHeaderDer = new JPanel(new BorderLayout(5, 5));
        lblRolSeleccionado.setFont(new Font("Arial", Font.BOLD, 13));
        lblRolSeleccionado.setForeground(new Color(0, 80, 160));
        lblRolSeleccionado.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        btnGuardarPermisos.setEnabled(false);
        pnlHeaderDer.add(lblRolSeleccionado, BorderLayout.WEST);
        pnlHeaderDer.add(btnGuardarPermisos, BorderLayout.EAST);

        pnlDerecho.add(pnlHeaderDer, BorderLayout.NORTH);
        pnlDerecho.add(new JScrollPane(tblPermisos), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tblRoles), pnlDerecho);
        split.setDividerLocation(320);
        add(split, BorderLayout.CENTER);

        // Navegación limpia de regreso al menú
        btnRegresar.addActionListener(e -> regresarMenu());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { regresarMenu(); }
        });

        btnNuevoRol.addActionListener(e -> crearRol());
        btnGuardarPermisos.addActionListener(e -> guardarPermisosRol());

        // Al hacer clic en un rol, carga sus casillas correspondientes
        tblRoles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarMatrizPermisosParaRol();
            }
        });
    }

    private void regresarMenu() {
        dispose();
        new MenuPrincipalFrame().setVisible(true);
    }

    private void cargarRoles() {
        modRoles.setRowCount(0);
        try (Connection con = Conexion.obtener()) {
            List<Rol> roles = rolDao.listarTodos(con);
            for (Rol r : roles) {
                modRoles.addRow(new Object[]{r.getId(), r.getNombre(), r.getNivel()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarMatrizPermisosParaRol() {
        int fila = tblRoles.getSelectedRow();
        if (fila == -1) {
            btnGuardarPermisos.setEnabled(false);
            lblRolSeleccionado.setText("Seleccione un rol de la izquierda para ver su matriz");
            modPermisos.setRowCount(0);
            return;
        }

        long rolId = (Long) modRoles.getValueAt(fila, 0);
        String rolNombre = (String) modRoles.getValueAt(fila, 1);
        lblRolSeleccionado.setText("Permisos asignados al rol: " + rolNombre);
        btnGuardarPermisos.setEnabled(true);

        modPermisos.setRowCount(0);
        try (Connection con = Conexion.obtener()) {
            Set<String> asignados = rolDao.obtenerPermisosDeRol(con, rolId);
            List<Permiso> todos = rolDao.listarPermisos(con);

            for (Permiso p : todos) {
                boolean tienePermiso = asignados.contains(p.getCodigo());
                modPermisos.addRow(new Object[]{
                        tienePermiso,
                        p.getCodigo(),
                        corregirTildes(p.getDescripcion()) // Limpia los caracteres b¢veda / reversi¢n
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar permisos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarPermisosRol() {
        int fila = tblRoles.getSelectedRow();
        if (fila == -1) return;

        long rolId = (Long) modRoles.getValueAt(fila, 0);
        String rolNombre = (String) modRoles.getValueAt(fila, 1);

        Set<String> seleccionados = new HashSet<>();
        for (int i = 0; i < modPermisos.getRowCount(); i++) {
            Boolean asignado = (Boolean) modPermisos.getValueAt(i, 0);
            if (Boolean.TRUE.equals(asignado)) {
                seleccionados.add((String) modPermisos.getValueAt(i, 1));
            }
        }

        try (Connection con = Conexion.obtener()) {
            rolDao.guardarPermisosDeRol(con, rolId, seleccionados);
            JOptionPane.showMessageDialog(this,
                    "Permisos de " + rolNombre + " guardados exitosamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar permisos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String corregirTildes(String texto) {
        if (texto == null) return "";
        return texto.replace("b¢veda", "bóveda")
                .replace("reversi¢n", "reversión")
                .replace("anulaci¢n", "anulación")
                .replace("Gesti¢n", "Gestión")
                .replace("asignaci¢n", "asignación")
                .replace("Administraci¢n", "Administración");
    }

    private void crearRol() {
        JTextField txtNombre = new JTextField();
        JSpinner spnNivel = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        Object[] form = {
                "Nombre del Rol (ej. AUDITOR):", txtNombre,
                "Nivel Jerárquico:", spnNivel
        };

        int res = JOptionPane.showConfirmDialog(this, form, "Crear Rol", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            int nivel = (Integer) spnNivel.getValue();

            if (nombre.isEmpty()) return;

            try (Connection con = Conexion.obtener()) {
                rolDao.crearRol(con, nombre, nivel);
                JOptionPane.showMessageDialog(this, "Rol creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarRoles();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}