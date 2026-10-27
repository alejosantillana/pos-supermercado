package com.supermercado.pos.servicio;

import com.supermercado.pos.dao.MovimientoCajaDAO;
import com.supermercado.pos.dao.ProductoDAO;
import com.supermercado.pos.dao.ReversionDAO;
import com.supermercado.pos.dominio.*;
import com.supermercado.pos.excepcion.AutorizacionException;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.util.Conexion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

public class ReversionService {
    private final AuthService authService = new AuthService();
    private final ReversionDAO reversionDao = new ReversionDAO();
    private final ProductoDAO productoDao = new ProductoDAO();
    private final MovimientoCajaDAO movimientoCajaDao = new MovimientoCajaDAO();

    public Usuario validarSupervisor(String username, char[] clave, Usuario cajero) {
        Usuario supervisor = authService.autenticar(username, clave);

        if (!supervisor.tienePermiso("REVERSION_AUTORIZAR")) {
            throw new AutorizacionException("El usuario no tiene el permiso REVERSION_AUTORIZAR");
        }

        if (supervisor.nivelMaximo() <= cajero.nivelMaximo()) {
            throw new AutorizacionException("El nivel jerárquico del autorizador debe ser estrictamente superior al del cajero");
        }

        return supervisor;
    }

    public void procesarReversion(VentaDetalle detalle, int cantidadARevertir, String motivo, Usuario cajero, Usuario supervisor, Turno turnoActual) {
        if (cantidadARevertir <= 0) {
            throw new ReglaNegocioException("La cantidad a revertir debe ser mayor a cero");
        }

        int disponible = detalle.getCantidad() - detalle.getCantidadReversada();
        if (cantidadARevertir > disponible) {
            throw new ReglaNegocioException("No se pueden revertir más unidades de las compradas (Disponibles: " + disponible + ")");
        }

        BigDecimal montoDevolucion = detalle.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(cantidadARevertir))
                .setScale(2, RoundingMode.HALF_UP);

        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                // 1. Registrar auditoría de reversión
                Reversion rev = Reversion.de(detalle, cantidadARevertir, montoDevolucion, motivo, cajero, supervisor);
                reversionDao.insertar(con, rev);

                // 2. Actualizar cantidad reversada en el detalle
                int totalReversado = detalle.getCantidadReversada() + cantidadARevertir;
                detalle.setCantidadReversada(totalReversado);
                reversionDao.actualizarCantidadReversada(con, detalle.getId(), totalReversado);

                // 3. Reingresar inventario
                productoDao.reingresarStock(con, detalle.getProducto().getId(), cantidadARevertir);

                // 4. Registrar salida de caja por la devolución en efectivo
                MovimientoCaja mov = MovimientoCaja.de(turnoActual, TipoMovimiento.REVERSION_EFECTIVO, montoDevolucion, supervisor);
                mov.setReferencia("REV-DET-" + detalle.getId());
                movimientoCajaDao.insertar(con, mov);

                // 5. Ajustar estado general de la venta
                String nuevoEstado = (totalReversado == detalle.getCantidad()) ? "PARCIALMENTE_REVERSADA" : "PARCIALMENTE_REVERSADA";
                reversionDao.actualizarEstadoVenta(con, detalle.getVenta().getId(), nuevoEstado);

                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al registrar la reversión", e);
        }
    }
}
