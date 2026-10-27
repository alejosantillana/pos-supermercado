package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO {

    public Optional<Producto> buscarPorCodigoBarras(Connection con, String codigo) throws SQLException {
        String sql = "SELECT * FROM producto WHERE codigo_barras = ? AND activo = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapear(rs));
            }
        }
    }

    public List<Producto> listarTodos(Connection con) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY id ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int descontarStock(Connection con, long productoId, int cantidad) throws SQLException {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, productoId);
            ps.setInt(3, cantidad);
            return ps.executeUpdate();
        }
    }

    public void reingresarStock(Connection con, long productoId, int cantidad) throws SQLException {
        String sql = "UPDATE producto SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, productoId);
            ps.executeUpdate();
        }
    }

    public void actualizarStockDirecto(Connection con, long productoId, int nuevoStock) throws SQLException {
        String sql = "UPDATE producto SET stock = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setLong(2, productoId);
            ps.executeUpdate();
        }
    }

    public void guardar(Connection con, Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo_barras, nombre, precio, porcentaje_impuesto, stock, activo) "
                + "VALUES (?, ?, ?, 12.00, ?, 1)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, p.getCodigoBarras());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setPorcentajeImpuesto(rs.getBigDecimal("porcentaje_impuesto"));
        p.setStock(rs.getInt("stock"));
        p.setActivo(rs.getInt("activo") == 1);
        return p;
    }
}