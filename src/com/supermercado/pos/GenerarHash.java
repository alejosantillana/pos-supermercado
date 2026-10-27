package com.supermercado.pos;

import com.supermercado.pos.util.Seguridad;

public class GenerarHash {
    public static void main(String[] args) {
        // Herramienta de utilidad para generar hashes bajo demanda
        String clavePrueba = "Admin123!";
        String hash = Seguridad.hash(clavePrueba.toCharArray());
        System.out.println("Hash generado para '" + clavePrueba + "':\n" + hash);
    }
}