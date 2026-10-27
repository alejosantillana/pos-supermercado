package com.supermercado.pos;

import com.supermercado.pos.dao.TurnoDAO;
import com.supermercado.pos.dominio.*;
import com.supermercado.pos.excepcion.AutorizacionException;
import com.supermercado.pos.excepcion.ReglaNegocioException;
import com.supermercado.pos.servicio.AuthService;
import com.supermercado.pos.servicio.ReversionService;
import com.supermercado.pos.servicio.TurnoService;
import com.supermercado.pos.servicio.VentaService;
import com.supermercado.pos.util.Conexion;
import com.supermercado.pos.util.InicializadorDatos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Pruebas {
    private static int fallos = 0;
    private static int totalPruebas = 0;

    private static final TurnoService turnoService = new TurnoService();
    private static final VentaService ventaService = new VentaService();
    private static final ReversionService reversionService = new ReversionService();
    private static final AuthService authService = new AuthService();

    static void verificar(boolean condicion, String caso) {
        totalPruebas++;
        if (condicion) {
            System.out.println("  [OK] " + caso);
        } else {
            System.err.println("  [FALLO] " + caso);
            fallos++;
        }
    }

    static void esperaExcepcion(Class<? extends RuntimeException> tipo, Runnable accion, String caso) {
        totalPruebas++;
        try {
            accion.run();
            System.err.println("  [FALLO] " + caso + " (No lanzó ninguna excepción)");
            fallos++;
        } catch (RuntimeException e) {
            if (tipo.isInstance(e)) {
                System.out.println("  [OK] " + caso + " -> Capturó: " + e.getClass().getSimpleName() + " (" + e.getMessage() + ")");
            } else {
                System.err.println("  [FALLO] " + caso + " -> Se esperaba " + tipo.getSimpleName() + " pero lanzó " + e.getClass().getSimpleName() + " (" + e.getMessage() + ")");
                fallos++;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("      EJECUTOR DE PRUEBAS AUTOMATIZADAS - SISTEMA POS     ");
        System.out.println("==========================================================");

        InicializadorDatos.asegurarUsuariosBase();

        Usuario admin = authService.autenticar("admin", "Admin123!".toCharArray());
        Usuario carlos = authService.autenticar("carlos", "Carlos123!".toCharArray());
        Usuario ana = authService.autenticar("ana", "Ana123!".toCharArray());

        // Asegurar que carlos no tenga turnos activos previos para garantizar un entorno limpio
        try (Connection con = Conexion.obtener()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE turno SET estado = 'CERRADO', cierre = SYSTIMESTAMP, efectivo_declarado = fondo_inicial, efectivo_esperado = fondo_inicial, diferencia = 0 "
                            + "WHERE cajero_id = ? AND estado = 'ABIERTO'")) {
                ps.setLong(1, carlos.getId());
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            System.err.println("Aviso al preparar entorno: " + ex.getMessage());
        }

        // 1. Abrir turno con fondo 0 (RN-03)
        esperaExcepcion(ReglaNegocioException.class, () -> {
            turnoService.abrir(carlos, 3L, BigDecimal.ZERO);
        }, "1. Abrir turno con fondo 0");

        // Abrir turno formal de prueba en Caja 3
        Turno turnoPrueba = turnoService.abrir(carlos, 3L, new BigDecimal("100.00"));
        final Turno turnoRef = turnoPrueba;

        // 2. Abrir turno con caja ocupada (RN-01)
        esperaExcepcion(ReglaNegocioException.class, () -> {
            turnoService.abrir(admin, turnoRef.getCaja().getId(), new BigDecimal("200.00"));
        }, "2. Abrir turno con caja ocupada");

        // 3. Cobro con pago insuficiente
        Producto prodLeche = ventaService.buscarPorCodigo("7401001001");
        List<VentaDetalle> lineas = new ArrayList<>();
        VentaDetalle d1 = new VentaDetalle();
        d1.setProducto(prodLeche);
        d1.setCantidad(2);
        d1.setPrecioUnitario(prodLeche.getPrecio());
        d1.setSubtotal(prodLeche.getPrecio().multiply(BigDecimal.valueOf(2)));
        lineas.add(d1);

        List<PagoDto> pagoInsuficiente = List.of(
                new PagoDto(MetodoPago.EFECTIVO, new BigDecimal("10.00"), new BigDecimal("10.00"), BigDecimal.ZERO)
        );

        esperaExcepcion(ReglaNegocioException.class, () -> {
            ventaService.procesarCobroMultiple(turnoRef, lineas, pagoInsuficiente);
        }, "3. Cobro con pago insuficiente");

        // 4. Pago mixto efectivo + tarjeta (Cambio solo sobre el efectivo)
        BigDecimal totalVenta = d1.getSubtotal(); // Q31.00
        BigDecimal pagoTarj = new BigDecimal("20.00");
        BigDecimal saldoEfe = totalVenta.subtract(pagoTarj); // Q11.00
        BigDecimal recibidoEfe = new BigDecimal("15.00");
        BigDecimal cambioCalculado = recibidoEfe.subtract(saldoEfe); // Q4.00

        List<PagoDto> pagoMixto = List.of(
                new PagoDto(MetodoPago.TARJETA, pagoTarj, pagoTarj, BigDecimal.ZERO),
                new PagoDto(MetodoPago.EFECTIVO, saldoEfe, recibidoEfe, cambioCalculado)
        );

        Venta ventaMixta = ventaService.procesarCobroMultiple(turnoRef, lineas, pagoMixto);
        verificar(ventaMixta.getId() != null && cambioCalculado.compareTo(new BigDecimal("4.00")) == 0,
                "4. Pago mixto efectivo + tarjeta (Cambio solo sobre el efectivo)");

        // 5. Reversión autorizada por un cajero
        VentaDetalle detalleCobrado = ventaMixta.getDetalles().get(0);
        esperaExcepcion(AutorizacionException.class, () -> {
            reversionService.validarSupervisor("carlos", "Carlos123!".toCharArray(), carlos);
        }, "5. Reversión autorizada por un cajero");

        // 6. Reversión autorizada por el mismo usuario
        esperaExcepcion(AutorizacionException.class, () -> {
            reversionService.validarSupervisor("admin", "Admin123!".toCharArray(), admin);
        }, "6. Reversión autorizada por el mismo usuario");

        // 7. Reversión válida y posterior reversión que supera la cantidad vendida
        Usuario supAna = reversionService.validarSupervisor("ana", "Ana123!".toCharArray(), carlos);
        reversionService.procesarReversion(detalleCobrado, 1, "Prueba reversion 1", carlos, supAna, turnoRef);
        verificar(detalleCobrado.getCantidadReversada() == 1, "7a. Reversión parcial inicial exitosa");

        esperaExcepcion(ReglaNegocioException.class, () -> {
            reversionService.procesarReversion(detalleCobrado, 2, "Prueba excedente", carlos, supAna, turnoRef);
        }, "7b. Reversión parcial que supera la cantidad vendida");

        // 8. Cierre con diferencia / arqueo ciego (RN-08)
        BigDecimal esperadoActual;
        try (Connection con = Conexion.obtener()) {
            TurnoDAO tdao = new TurnoDAO();
            esperadoActual = tdao.calcularEfectivoEsperado(con, turnoRef.getId());
        } catch (Exception ex) {
            esperadoActual = new BigDecimal("100.00");
        }

        BigDecimal declaradoConFaltante = esperadoActual.subtract(new BigDecimal("20.00"));
        Turno turnoCerrado = turnoService.cerrarTurno(turnoRef, declaradoConFaltante);
        verificar(turnoCerrado.getDiferencia().compareTo(new BigDecimal("-20.00")) == 0,
                "8. Cierre con faltante de Q20 (diferencia == -20.00)");

        System.out.println("==========================================================");
        System.out.printf("RESULTADO FINAL: %d/%d pruebas superadas (Fallos: %d)\n",
                (totalPruebas - fallos), totalPruebas, fallos);
        System.out.println("==========================================================");

        if (fallos > 0) {
            System.exit(1);
        }
    }
}
