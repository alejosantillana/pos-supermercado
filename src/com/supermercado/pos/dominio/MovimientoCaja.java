package com.supermercado.pos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoCaja {
    private Long id;
    private Turno turno;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private Usuario autorizadoPor; // supervisor que recibe el dinero o autoriza
    private String referencia; // folio de recibo de bóveda
    private LocalDateTime fecha = LocalDateTime.now();

    // Método de fábrica estático
    public static MovimientoCaja de(Turno t, TipoMovimiento tipo, BigDecimal monto, Usuario autoriza) {
        MovimientoCaja m = new MovimientoCaja();
        m.setTurno(t);
        m.setTipo(tipo);
        m.setMonto(monto);
        m.setAutorizadoPor(autoriza);
        return m;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public Usuario getAutorizadoPor() { return autorizadoPor; }
    public void setAutorizadoPor(Usuario autorizadoPor) { this.autorizadoPor = autorizadoPor; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}