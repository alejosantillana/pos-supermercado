package com.supermercado.pos.dominio;

import java.math.BigDecimal;

public class VentaDetalle {
    private Long id;
    private Venta venta;
    private Producto producto;
    private int cantidad;
    private int cantidadReversada = 0;
    private BigDecimal precioUnitario; // congelado al vender
    private BigDecimal subtotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getCantidadReversada() { return cantidadReversada; }
    public void setCantidadReversada(int cantidadReversada) { this.cantidadReversada = cantidadReversada; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}