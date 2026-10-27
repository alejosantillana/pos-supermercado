package com.supermercado.pos.dao;

import com.supermercado.pos.dominio.Caja;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CajaDAO {

    public Optional<Caja> buscarPorId(Connection con, long id) throws SQLException {
        String sql = "SELECT * FROM caja WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Caja c = new Caja();
                    c.setId(rs.getLong("id"));
                    c.setNumero(rs.getInt("numero"));
                    c.setNombre(rs.getString("nombre"));
                    c.setActiva(rs.getInt("activa") == 1);
                    return Optional.of(c);
                }
                return Optional.empty();
            }
        }
    }

    public List<Caja> listarActivasSinTurno(Connection con) throws SQLException {
        String sql = "SELECT * FROM caja c WHERE c.activa = 1 "
                + "AND NOT EXISTS (SELECT 1 FROM turno t WHERE t.caja_id = c.id AND t.estado = 'ABIERTO')";
        List<Caja> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Caja c = new Caja();
                c.setId(rs.getLong("id"));
                c.setNumero(rs.getInt("numero"));
                c.setNombre(rs.getString("nombre"));
                c.setActiva(true);
                lista.add(c);
            }
        }
        return lista;
    }
}