package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.MovimientoCaja;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class MovimientoCajaDAO {

    public long insertar(Connection con, MovimientoCaja m) throws SQLException {
        String sql = "INSERT INTO movimiento_caja (turno_id, tipo, monto, autorizado_por, referencia, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
            ps.setLong(1, m.getTurno().getId());
            ps.setString(2, m.getTipo().name());
            ps.setBigDecimal(3, m.getMonto());

            if (m.getAutorizadoPor() != null && m.getAutorizadoPor().getId() != null) {
                ps.setLong(4, m.getAutorizadoPor().getId());
            } else {
                ps.setNull(4, Types.NUMERIC);
            }

            ps.setString(5, m.getReferencia());
            ps.setTimestamp(6, Timestamp.valueOf(m.getFecha()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    m.setId(id);
                    return id;
                }
                return 0;
            }
        }
    }
}