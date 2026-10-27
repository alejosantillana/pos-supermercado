package com.supermercado.pos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reversion {
    private Long id;
    private VentaDetalle detalle;
    private int cantidad;
    private BigDecimal monto;
    private String motivo;
    private Usuario solicitadoPor;
    private Usuario autorizadoPor;
    private LocalDateTime fecha = LocalDateTime.now();

    public static Reversion de(VentaDetalle d, int cantidad, BigDecimal monto, String motivo, Usuario solicitadoPor, Usuario autorizadoPor) {
        Reversion r = new Reversion();
        r.setDetalle(d);
        r.setCantidad(cantidad);
        r.setMonto(monto);
        r.setMotivo(motivo);
        r.setSolicitadoPor(solicitadoPor);
        r.setAutorizadoPor(autorizadoPor);
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public VentaDetalle getDetalle() { return detalle; }
    public void setDetalle(VentaDetalle detalle) { this.detalle = detalle; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Usuario getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(Usuario solicitadoPor) { this.solicitadoPor = solicitadoPor; }
    public Usuario getAutorizadoPor() { return autorizadoPor; }
    public void setAutorizadoPor(Usuario autorizadoPor) { this.autorizadoPor = autorizadoPor; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}