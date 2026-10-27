package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.Caja;
import com.supermercado.pos.dominio.EstadoTurno;
import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.dominio.Usuario;

import java.sql.*;
import java.util.Optional;

public class TurnoDAO {

    public Optional<Turno> buscarAbiertoPorCajero(Connection con, long cajeroId) throws SQLException {
        String sql = "SELECT t.*, c.numero AS caja_num, c.nombre AS caja_nom "
                + "FROM turno t JOIN caja c ON t.caja_id = c.id "
                + "WHERE t.cajero_id = ? AND t.estado = 'ABIERTO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, cajeroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Turno t = new Turno();
                t.setId(rs.getLong("id"));

                Caja c = new Caja();
                c.setId(rs.getLong("caja_id"));
                c.setNumero(rs.getInt("caja_num"));
                c.setNombre(rs.getString("caja_nom"));
                t.setCaja(c);

                Usuario u = new Usuario();
                u.setId(cajeroId);
                t.setCajero(u);

                t.setApertura(rs.getTimestamp("apertura").toLocalDateTime());
                t.setFondoInicial(rs.getBigDecimal("fondo_inicial"));
                t.setEstado(EstadoTurno.valueOf(rs.getString("estado")));
                return Optional.of(t);
            }
        }
    }

    public long insertar(Connection con, Turno t) throws SQLException {
        String sql = "INSERT INTO turno (caja_id, cajero_id, apertura, fondo_inicial, estado) "
                + "VALUES (?, ?, ?, ?, 'ABIERTO')";

        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"})) {
            ps.setLong(1, t.getCaja().getId());
            ps.setLong(2, t.getCajero().getId());
            ps.setTimestamp(3, Timestamp.valueOf(t.getApertura()));
            ps.setBigDecimal(4, t.getFondoInicial());
            ps.executeUpdate();

            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getLong(1);
            }
        }
    }

    private Turno mapear(Connection con, ResultSet rs) throws SQLException {
        Turno t = new Turno();
        t.setId(rs.getLong("id"));

        Caja caja = new Caja();
        caja.setId(rs.getLong("caja_id"));
        t.setCaja(caja);

        Usuario cajero = new Usuario();
        cajero.setId(rs.getLong("cajero_id"));
        t.setCajero(cajero);

        t.setApertura(rs.getTimestamp("apertura").toLocalDateTime());
        if (rs.getTimestamp("cierre") != null) {
            t.setCierre(rs.getTimestamp("cierre").toLocalDateTime());
        }
        t.setFondoInicial(rs.getBigDecimal("fondo_inicial"));
        t.setEfectivoDeclarado(rs.getBigDecimal("efectivo_declarado"));
        t.setEfectivoEsperado(rs.getBigDecimal("efectivo_esperado"));
        t.setDiferencia(rs.getBigDecimal("diferencia"));
        t.setEstado(EstadoTurno.valueOf(rs.getString("estado")));

        return t;
    }
    public boolean existeAbiertoEnCaja(Connection con, long cajaId) throws SQLException {
        String sql = "SELECT 1 FROM turno WHERE caja_id = ? AND estado = 'ABIERTO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, cajaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public java.math.BigDecimal calcularEfectivoEsperado(Connection con, long turnoId) throws SQLException {
        // RN-04: fondo_inicial + cobros en efectivo - reversiones en efectivo - retiros a bóveda
        String sql = "SELECT (t.fondo_inicial "
                + " + NVL((SELECT SUM(p.monto) FROM pago p JOIN venta v ON p.venta_id = v.id WHERE v.turno_id = t.id AND p.metodo = 'EFECTIVO'), 0) "
                + " - NVL((SELECT SUM(m.monto) FROM movimiento_caja m WHERE m.turno_id = t.id AND m.tipo = 'REVERSION_EFECTIVO'), 0) "
                + " - NVL((SELECT SUM(m.monto) FROM movimiento_caja m WHERE m.turno_id = t.id AND m.tipo = 'RETIRO_BOVEDA'), 0) "
                + ") AS esperado FROM turno t WHERE t.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, turnoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("esperado");
                }
                return java.math.BigDecimal.ZERO;
            }
        }
    }

    public void cerrarTurno(Connection con, Turno t) throws SQLException {
        String sql = "UPDATE turno SET cierre = ?, efectivo_declarado = ?, efectivo_esperado = ?, diferencia = ?, estado = 'CERRADO' WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(t.getCierre()));
            ps.setBigDecimal(2, t.getEfectivoDeclarado());
            ps.setBigDecimal(3, t.getEfectivoEsperado());
            ps.setBigDecimal(4, t.getDiferencia());
            ps.setLong(5, t.getId());
            ps.executeUpdate();
        }
    }
}