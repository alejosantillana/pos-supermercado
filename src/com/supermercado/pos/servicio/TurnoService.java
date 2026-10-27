package com.supermercado.pos.servicio;

import com.supermercado.pos.dao.CajaDAO;
import com.supermercado.pos.dao.MovimientoCajaDAO;
import com.supermercado.pos.dao.TurnoDAO;
import com.supermercado.pos.dominio.Caja;
import com.supermercado.pos.dominio.EstadoTurno;
import com.supermercado.pos.dominio.MovimientoCaja;
import com.supermercado.pos.dominio.TipoMovimiento;
import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.util.Conexion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TurnoService {
    private final TurnoDAO turnoDao = new TurnoDAO();
    private final CajaDAO cajaDao = new CajaDAO();
    private final MovimientoCajaDAO movimientoDao = new MovimientoCajaDAO();

    public List<Caja> obtenerCajasDisponibles() {
        try (Connection con = Conexion.obtener()) {
            return cajaDao.listarActivasSinTurno(con);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar cajas disponibles", e);
        }
    }

    // Sobrecarga estándar según rúbrica (Fase 4 y Fase 10)
    public Turno abrir(Usuario cajero, long cajaId, BigDecimal fondo) {
        return abrirTurno(cajaId, cajero, fondo);
    }

    public Turno abrirTurno(long cajaId, Usuario cajero, BigDecimal fondoInicial) {
        if (fondoInicial == null || fondoInicial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El fondo inicial debe ser mayor a cero");
        }

        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);

            if (turnoDao.buscarAbiertoPorCajero(con, cajero.getId()).isPresent()) {
                con.rollback();
                throw new ReglaNegocioException("El cajero ya tiene un turno abierto");
            }

            if (turnoDao.existeAbiertoEnCaja(con, cajaId)) {
                con.rollback();
                throw new ReglaNegocioException("La caja seleccionada ya está abierta");
            }

            Caja caja = cajaDao.buscarPorId(con, cajaId)
                    .orElseThrow(() -> new ReglaNegocioException("La caja especificada no existe"));

            Turno t = new Turno();
            t.setCaja(caja);
            t.setCajero(cajero);
            t.setApertura(LocalDateTime.now());
            t.setFondoInicial(fondoInicial.setScale(2, RoundingMode.HALF_UP));
            t.setEstado(EstadoTurno.ABIERTO);

            long idGenerado = turnoDao.insertar(con, t);
            t.setId(idGenerado);

            // Registro en movimiento_caja (Fase 4.2)
            movimientoDao.insertar(con, MovimientoCaja.de(t, TipoMovimiento.FONDO_INICIAL, t.getFondoInicial(), null));

            con.commit();
            return t;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { // ORA-00001
                throw new ReglaNegocioException("La caja o el cajero ya tienen un turno abierto");
            }
            throw new IllegalStateException("Error al registrar apertura de turno", e);
        }
    }

    public Optional<Turno> obtenerTurnoActivo(Usuario cajero) {
        try (Connection con = Conexion.obtener()) {
            return turnoDao.buscarAbiertoPorCajero(con, cajero.getId());
        } catch (SQLException e) {
            throw new IllegalStateException("Error al verificar turno activo", e);
        }
    }

    public Turno cerrarTurno(Turno turno, BigDecimal efectivoDeclarado) {
        if (efectivoDeclarado == null || efectivoDeclarado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El efectivo declarado no puede ser negativo");
        }

        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                BigDecimal esperado = turnoDao.calcularEfectivoEsperado(con, turno.getId());
                BigDecimal diferencia = efectivoDeclarado.subtract(esperado);

                turno.setCierre(LocalDateTime.now());
                turno.setEfectivoDeclarado(efectivoDeclarado);
                turno.setEfectivoEsperado(esperado);
                turno.setDiferencia(diferencia);
                turno.setEstado(EstadoTurno.CERRADO);

                turnoDao.cerrarTurno(con, turno);
                con.commit();
                return turno;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al registrar cierre de turno", e);
        }
    }
}