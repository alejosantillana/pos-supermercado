package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.Permiso;
import com.supermercado.pos.dominio.Rol;
import com.supermercado.pos.dominio.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsuarioDAO {

    public Optional<Usuario> buscarPorUsername(Connection con, String username) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setActivo(rs.getInt("activo") == 1);

                cargarRolesYPermisos(con, u);
                return Optional.of(u);
            }
        }
    }

    private void cargarRolesYPermisos(Connection con, Usuario u) throws SQLException {
        // Cargar roles del usuario
        String sqlRoles = "SELECT r.* FROM rol r JOIN usuario_rol ur ON r.id = ur.rol_id WHERE ur.usuario_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlRoles)) {
            ps.setLong(1, u.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rol r = new Rol();
                    r.setId(rs.getLong("id"));
                    r.setNombre(rs.getString("nombre"));
                    r.setNivel(rs.getInt("nivel"));

                    cargarPermisosDeRol(con, r);
                    u.getRoles().add(r);
                }
            }
        }

        // Cargar permisos extra individuales
        String sqlExtra = "SELECT p.* FROM permiso p JOIN usuario_permiso up ON p.id = up.permiso_id WHERE up.usuario_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlExtra)) {
            ps.setLong(1, u.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Permiso p = new Permiso();
                    p.setId(rs.getLong("id"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setDescripcion(rs.getString("descripcion"));
                    u.getPermisosExtra().add(p);
                }
            }
        }
    }

    private void cargarPermisosDeRol(Connection con, Rol r) throws SQLException {
        String sql = "SELECT p.* FROM permiso p JOIN rol_permiso rp ON p.id = rp.permiso_id WHERE rp.rol_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Permiso p = new Permiso();
                    p.setId(rs.getLong("id"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setDescripcion(rs.getString("descripcion"));
                    r.getPermisos().add(p);
                }
            }
        }
    }

    public List<Usuario> listarTodos(Connection con) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setActivo(rs.getInt("activo") == 1);
                lista.add(u);
            }
        }
        return lista;
    }

    public void cambiarEstado(Connection con, long usuarioId, boolean activo) throws SQLException {
        String sql = "UPDATE usuario SET activo = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setLong(2, usuarioId);
            ps.executeUpdate();
        }
    }

    public void reemplazarRoles(Connection con, long usuarioId, Set<Long> rolIds) throws SQLException {
        String sqlDel = "DELETE FROM usuario_rol WHERE usuario_id = ?";
        try (PreparedStatement psDel = con.prepareStatement(sqlDel)) {
            psDel.setLong(1, usuarioId);
            psDel.executeUpdate();
        }

        String sqlIns = "INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)";
        try (PreparedStatement psIns = con.prepareStatement(sqlIns)) {
            for (Long rId : rolIds) {
                psIns.setLong(1, usuarioId);
                psIns.setLong(2, rId);
                psIns.addBatch();
            }
            psIns.executeBatch();
        }
    }
}