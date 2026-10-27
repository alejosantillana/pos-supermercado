package com.supermercado.pos.dominio;

import java.util.Objects;

public class Permiso {
    private Long id;
    private String codigo; // ej. "VENTA_COBRAR"
    private String descripcion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return Objects.equals(codigo, permiso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}