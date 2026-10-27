package com.supermercado.pos.servicio;

import com.supermercado.pos.dao.ProductoDAO;
import com.supermercado.pos.dao.VentaDAO;
import com.supermercado.pos.dominio.*;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.util.Conexion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class VentaService {
    private final ProductoDAO productoDao = new ProductoDAO();
    private final VentaDAO ventaDao = new VentaDAO();

    public Producto buscarPorCodigo(String codigoBarras) {
        try (Connection con = Conexion.obtener()) {
            return productoDao.buscarPorCodigoBarras(con, codigoBarras)
                    .orElseThrow(() -> new ReglaNegocioException("Producto no encontrado o inactivo"));
        } catch (SQLException e) {
            throw new IllegalStateException("Error de lectura en base de datos", e);
        }
    }

    public Venta procesarCobroMultiple(Turno turno, List<VentaDetalle> lineas, List<PagoDto> pagos) {
        if (lineas == null || lineas.isEmpty()) {
            throw new ReglaNegocioException("No hay productos en la venta actual");
        }

        BigDecimal total = lineas.stream()
                .map(VentaDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal pagado = pagos.stream()
                .map(PagoDto::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (pagado.compareTo(total) < 0) {
            throw new ReglaNegocioException("Pago insuficiente. Falta por cubrir: Q " + total.subtract(pagado));
        }

        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                // 1. Descuento atómico de existencias
                for (VentaDetalle d : lineas) {
                    int filas = productoDao.descontarStock(con, d.getProducto().getId(), d.getCantidad());
                    if (filas == 0) {
                        throw new ReglaNegocioException("Sin stock suficiente para: " + d.getProducto().getNombre());
                    }
                }

                // 2. Encabezado de venta
                Venta venta = new Venta();
                venta.setTurno(turno);
                venta.setFecha(LocalDateTime.now());
                venta.setTotal(total);
                venta.setEstado(EstadoVenta.COMPLETADA);

                long ventaId = ventaDao.insertarVenta(con, venta);
                venta.setId(ventaId);

                // 3. Detalle
                for (VentaDetalle d : lineas) {
                    d.setVenta(venta);
                    ventaDao.insertarDetalle(con, ventaId, d);
                }

                // 4. Registrar pagos aplicados
                for (PagoDto dto : pagos) {
                    Pago pago = new Pago();
                    pago.setVenta(venta);
                    pago.setMetodo(dto.metodo());
                    pago.setMonto(dto.monto());
                    pago.setRecibido(dto.recibido());
                    pago.setCambio(dto.cambio());
                    ventaDao.insertarPago(con, ventaId, pago);
                    venta.getPagos().add(pago);
                }

                venta.getDetalles().addAll(lineas);
                con.commit();
                return venta;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al registrar el cobro", e);
        }
    }
    public Venta consultarVenta(long ventaId) {
        try (Connection con = Conexion.obtener()) {
            return ventaDao.buscarPorIdConDetalles(con, ventaId)
                    .orElseThrow(() -> new ReglaNegocioException("No se encontró ninguna venta con ID: " + ventaId));
        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar venta", e);
        }
    }
}
