package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.Permiso;
import com.supermercado.pos.dominio.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    public List<Rol> listarTodos(Connection con) throws SQLException {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT * FROM rol ORDER BY nivel ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rol r = new Rol();
                r.setId(rs.getLong("id"));
                r.setNombre(rs.getString("nombre"));
                r.setNivel(rs.getInt("nivel"));
                lista.add(r);
            }
        }
        return lista;
    }

    public List<Permiso> listarPermisos(Connection con) throws SQLException {
        List<Permiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM permiso ORDER BY codigo ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Permiso p = new Permiso();
                p.setId(rs.getLong("id"));
                p.setCodigo(rs.getString("codigo"));
                p.setDescripcion(rs.getString("descripcion"));
                lista.add(p);
            }
        }
        return lista;
    }

    public void crearRol(Connection con, String nombre, int nivel) throws SQLException {
        String sql = "INSERT INTO rol (nombre, nivel) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.toUpperCase());
            ps.setInt(2, nivel);
            ps.executeUpdate();
        }
    }
    public java.util.Set<String> obtenerPermisosDeRol(Connection con, long rolId) throws SQLException {
        java.util.Set<String> permisos = new java.util.HashSet<>();
        String sql = "SELECT p.codigo FROM permiso p JOIN rol_permiso rp ON p.id = rp.permiso_id WHERE rp.rol_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(rs.getString("codigo"));
                }
            }
        }
        return permisos;
    }

    public void guardarPermisosDeRol(Connection con, long rolId, java.util.Set<String> codigosPermisos) throws SQLException {
        String sqlDel = "DELETE FROM rol_permiso WHERE rol_id = ?";
        try (PreparedStatement psDel = con.prepareStatement(sqlDel)) {
            psDel.setLong(1, rolId);
            psDel.executeUpdate();
        }

        String sqlIns = "INSERT INTO rol_permiso (rol_id, permiso_id) "
                + "SELECT ?, id FROM permiso WHERE codigo = ?";
        try (PreparedStatement psIns = con.prepareStatement(sqlIns)) {
            for (String cod : codigosPermisos) {
                psIns.setLong(1, rolId);
                psIns.setString(2, cod);
                psIns.addBatch();
            }
            psIns.executeBatch();
        }
    }
}