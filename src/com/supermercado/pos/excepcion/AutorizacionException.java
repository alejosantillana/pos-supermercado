package com.supermercado.pos.excepcion;

public class AutorizacionException extends RuntimeException {
    public AutorizacionException(String mensaje) {
        super(mensaje);
    }
}