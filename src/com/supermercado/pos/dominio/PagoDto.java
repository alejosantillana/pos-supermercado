package com.supermercado.pos.dominio;

import java.math.BigDecimal;

public record PagoDto(MetodoPago metodo, BigDecimal monto, BigDecimal recibido, BigDecimal cambio) {
}