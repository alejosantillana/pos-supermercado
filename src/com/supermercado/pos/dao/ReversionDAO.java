package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.Reversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ReversionDAO {

    public long insertar(Connection con, Reversion rev) throws SQLException {
        String sql = "INSERT INTO reversion (detalle_id, cantidad, monto, motivo, solicitado_por, autorizado_por, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
            ps.setLong(1, rev.getDetalle().getId());
            ps.setInt(2, rev.getCantidad());
            ps.setBigDecimal(3, rev.getMonto());
            ps.setString(4, rev.getMotivo());
            ps.setLong(5, rev.getSolicitadoPor().getId());
            ps.setLong(6, rev.getAutorizadoPor().getId());
            ps.setTimestamp(7, Timestamp.valueOf(rev.getFecha()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void actualizarCantidadReversada(Connection con, long detalleId, int nuevaCantidadReversada) throws SQLException {
        String sql = "UPDATE venta_detalle SET cantidad_reversada = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevaCantidadReversada);
            ps.setLong(2, detalleId);
            ps.executeUpdate();
        }
    }

    public void actualizarEstadoVenta(Connection con, long ventaId, String nuevoEstado) throws SQLException {
        String sql = "UPDATE venta SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setLong(2, ventaId);
            ps.executeUpdate();
        }
    }
}