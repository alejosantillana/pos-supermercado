package com.supermercado.pos.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class InicializadorDatos {

    private InicializadorDatos() {}

    public static void asegurarUsuariosBase() {
        try (Connection con = Conexion.obtener()) {
            // 1. Asegurar todos los permisos administrativos y asignarlos al rol ADMIN
            String[] permisosAdmin = {"CATALOGO_ADMIN", "USUARIO_ADMIN", "ROL_ADMIN"};
            for (String perm : permisosAdmin) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO permiso (codigo, descripcion) "
                                + "SELECT ?, 'Permiso administrativo' FROM dual "
                                + "WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = ?)")) {
                    ps.setString(1, perm);
                    ps.setString(2, perm);
                    ps.executeUpdate();
                }

                try (PreparedStatement psRolPerm = con.prepareStatement(
                        "INSERT INTO rol_permiso (rol_id, permiso_id) "
                                + "SELECT r.id, p.id FROM rol r, permiso p "
                                + "WHERE r.nombre = 'ADMIN' AND p.codigo = ? "
                                + "AND NOT EXISTS (SELECT 1 FROM rol_permiso WHERE rol_id = r.id AND permiso_id = p.id)")) {
                    psRolPerm.setString(1, perm);
                    psRolPerm.executeUpdate();
                }
            }

            // 2. Usuarios base del sistema
            // Usuario 1: admin / Admin123! (Rol ADMIN, nivel 4)
            crearSiNoExiste(con, "admin", "Admin123!", "Administrador del Sistema", "ADMIN");

            // Usuario 2: carlos / Carlos123! (Rol CAJERO, nivel 1)
            crearSiNoExiste(con, "carlos", "Carlos123!", "Carlos Cajero", "CAJERO");

            // Usuario 3: ana / Ana123! (Rol SUPERVISOR, nivel 2)
            crearSiNoExiste(con, "ana", "Ana123!", "Ana Supervisora", "SUPERVISOR");

        } catch (SQLException e) {
            System.err.println("Error al inicializar usuarios base: " + e.getMessage());
        }
    }

    private static void crearSiNoExiste(Connection con, String username, String clavePlana, String nombreCompleto, String nombreRol) throws SQLException {
        String sqlExiste = "SELECT id FROM usuario WHERE username = ?";
        try (PreparedStatement psExiste = con.prepareStatement(sqlExiste)) {
            psExiste.setString(1, username);
            try (ResultSet rsExiste = psExiste.executeQuery()) {
                if (rsExiste.next()) {
                    long userId = rsExiste.getLong("id");
                    String sqlUpd = "UPDATE usuario SET password_hash = ? WHERE id = ?";
                    try (PreparedStatement psUpd = con.prepareStatement(sqlUpd)) {
                        psUpd.setString(1, Seguridad.hash(clavePlana.toCharArray()));
                        psUpd.setLong(2, userId);
                        psUpd.executeUpdate();
                    }
                    return;
                }
            }
        }

        String sqlInsert = "INSERT INTO usuario (username, password_hash, nombre_completo, activo) VALUES (?, ?, ?, 1)";
        long nuevoId;
        try (PreparedStatement psInsert = con.prepareStatement(sqlInsert, new String[]{"ID"})) {
            psInsert.setString(1, username);
            psInsert.setString(2, Seguridad.hash(clavePlana.toCharArray()));
            psInsert.setString(3, nombreCompleto);
            psInsert.executeUpdate();
            try (ResultSet rsKey = psInsert.getGeneratedKeys()) {
                rsKey.next();
                nuevoId = rsKey.getLong(1);
            }
        }

        String sqlRol = "INSERT INTO usuario_rol (usuario_id, rol_id) "
                + "SELECT ?, id FROM rol WHERE nombre = ?";
        try (PreparedStatement psRol = con.prepareStatement(sqlRol)) {
            psRol.setLong(1, nuevoId);
            psRol.setString(2, nombreRol);
            psRol.executeUpdate();
        }
    }
}