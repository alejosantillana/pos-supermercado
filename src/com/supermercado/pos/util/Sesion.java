package com.supermercado.pos.util;

import com.supermercado.pos.dominio.Turno;
import com.supermercado.pos.dominio.Usuario;

public final class Sesion {
    private static Usuario usuario;
    private static Turno turno;

    private Sesion() {}

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario u) {
        usuario = u;
    }

    public static Turno getTurno() {
        return turno;
    }

    public static void setTurno(Turno t) {
        turno = t;
    }

    public static void cerrar() {
        usuario = null;
        turno = null;
    }
}