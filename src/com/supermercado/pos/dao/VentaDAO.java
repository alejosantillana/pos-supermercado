package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.EstadoVenta;
import com.supermercado.pos.dominio.Pago;
import com.supermercado.pos.dominio.Producto;
import com.supermercado.pos.dominio.Venta;
import com.supermercado.pos.dominio.VentaDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class VentaDAO {

    public long insertarVenta(Connection con, Venta venta) throws SQLException {
        String sql = "INSERT INTO venta (turno_id, fecha, total, estado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
            ps.setLong(1, venta.getTurno().getId());
            ps.setTimestamp(2, Timestamp.valueOf(venta.getFecha()));
            ps.setBigDecimal(3, venta.getTotal());
            ps.setString(4, venta.getEstado().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void insertarDetalle(Connection con, long ventaId, VentaDetalle det) throws SQLException {
        String sql = "INSERT INTO venta_detalle (venta_id, producto_id, cantidad, cantidad_reversada, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, 0, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
            ps.setLong(1, ventaId);
            ps.setLong(2, det.getProducto().getId());
            ps.setInt(3, det.getCantidad());
            ps.setBigDecimal(4, det.getPrecioUnitario());
            ps.setBigDecimal(5, det.getSubtotal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                det.setId(rs.getLong(1));
            }
        }
    }

    public void insertarPago(Connection con, long ventaId, Pago pago) throws SQLException {
        String sql = "INSERT INTO pago (venta_id, metodo, monto, recibido, cambio) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, ventaId);
            ps.setString(2, pago.getMetodo().name());
            ps.setBigDecimal(3, pago.getMonto());
            ps.setBigDecimal(4, pago.getRecibido());
            ps.setBigDecimal(5, pago.getCambio());
            ps.executeUpdate();
        }
    }

    public Optional<Venta> buscarPorIdConDetalles(Connection con, long ventaId) throws SQLException {
        String sqlVenta = "SELECT * FROM venta WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlVenta)) {
            ps.setLong(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Venta v = new Venta();
                v.setId(rs.getLong("id"));
                v.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                v.setTotal(rs.getBigDecimal("total"));
                v.setEstado(EstadoVenta.valueOf(rs.getString("estado")));

                String sqlDet = "SELECT vd.*, p.nombre, p.codigo_barras FROM venta_detalle vd "
                        + "JOIN producto p ON vd.producto_id = p.id WHERE vd.venta_id = ?";
                try (PreparedStatement psDet = con.prepareStatement(sqlDet)) {
                    psDet.setLong(1, ventaId);
                    try (ResultSet rsDet = psDet.executeQuery()) {
                        while (rsDet.next()) {
                            Producto prod = new Producto();
                            prod.setId(rsDet.getLong("producto_id"));
                            prod.setNombre(rsDet.getString("nombre"));
                            prod.setCodigoBarras(rsDet.getString("codigo_barras"));

                            VentaDetalle d = new VentaDetalle();
                            d.setId(rsDet.getLong("id"));
                            d.setVenta(v);
                            d.setProducto(prod);
                            d.setCantidad(rsDet.getInt("cantidad"));
                            d.setCantidadReversada(rsDet.getInt("cantidad_reversada"));
                            d.setPrecioUnitario(rsDet.getBigDecimal("precio_unitario"));
                            d.setSubtotal(rsDet.getBigDecimal("subtotal"));

                            v.getDetalles().add(d);
                        }
                    }
                }
                return Optional.of(v);
            }
        }
    }
}