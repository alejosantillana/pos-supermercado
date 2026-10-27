package com.supermercado.pos.dominio;

import java.math.BigDecimal;

public class Producto {
    private Long id;
    private String codigoBarras;
    private String nombre;
    private BigDecimal precio;
    private BigDecimal porcentajeImpuesto; // 12.00
    private int stock;
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public BigDecimal getPorcentajeImpuesto() { return porcentajeImpuesto; }
    public void setPorcentajeImpuesto(BigDecimal porcentajeImpuesto) { this.porcentajeImpuesto = porcentajeImpuesto; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}