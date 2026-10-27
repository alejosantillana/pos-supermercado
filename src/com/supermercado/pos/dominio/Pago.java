package com.supermercado.pos.dominio;

import java.math.BigDecimal;

public class Pago {
    private Long id;
    private Venta venta;
    private MetodoPago metodo;
    private BigDecimal monto; // aplicado a la venta
    private BigDecimal recibido; // lo que entregó el cliente
    private BigDecimal cambio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public MetodoPago getMetodo() { return metodo; }
    public void setMetodo(MetodoPago metodo) { this.metodo = metodo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getRecibido() { return recibido; }
    public void setRecibido(BigDecimal recibido) { this.recibido = recibido; }
    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }
}