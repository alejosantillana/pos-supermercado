package com.supermercado.pos.dominio;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Rol {
    private Long id;
    private String nombre; // CAJERO, SUPERVISOR, GERENTE, ADMIN
    private int nivel; // 1=cajero, 2=supervisor, 3=gerente...
    private Set<Permiso> permisos = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(nombre, rol.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}