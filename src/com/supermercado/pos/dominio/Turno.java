package com.supermercado.pos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Turno {
    private Long id;
    private Caja caja;
    private Usuario cajero;
    private LocalDateTime apertura;
    private LocalDateTime cierre;
    private BigDecimal fondoInicial;
    private BigDecimal efectivoDeclarado;
    private BigDecimal efectivoEsperado;
    private BigDecimal diferencia;
    private EstadoTurno estado = EstadoTurno.ABIERTO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Caja getCaja() { return caja; }
    public void setCaja(Caja caja) { this.caja = caja; }
    public Usuario getCajero() { return cajero; }
    public void setCajero(Usuario cajero) { this.cajero = cajero; }
    public LocalDateTime getApertura() { return apertura; }
    public void setApertura(LocalDateTime apertura) { this.apertura = apertura; }
    public LocalDateTime getCierre() { return cierre; }
    public void setCierre(LocalDateTime cierre) { this.cierre = cierre; }
    public BigDecimal getFondoInicial() { return fondoInicial; }
    public void setFondoInicial(BigDecimal fondoInicial) { this.fondoInicial = fondoInicial; }
    public BigDecimal getEfectivoDeclarado() { return efectivoDeclarado; }
    public void setEfectivoDeclarado(BigDecimal efectivoDeclarado) { this.efectivoDeclarado = efectivoDeclarado; }
    public BigDecimal getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(BigDecimal efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }
}