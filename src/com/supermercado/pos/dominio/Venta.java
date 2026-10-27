package com.supermercado.pos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private Long id;
    private Turno turno;
    private LocalDateTime fecha = LocalDateTime.now();
    private List<VentaDetalle> detalles = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();
    private BigDecimal total;
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public List<VentaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<VentaDetalle> detalles) { this.detalles = detalles; }
    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
}