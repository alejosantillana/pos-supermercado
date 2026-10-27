package com.supermercado.pos.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Seguridad {
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_BITS = 256;

    private Seguridad() {}

    public static String hash(char[] clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] h = pbkdf2(clave, sal, ITERACIONES);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(sal)
                + ":" + Base64.getEncoder().encodeToString(h);
    }

    public static boolean verificar(char[] clave, String almacenado) {
        String[] p = almacenado.split(":");
        byte[] esperado = Base64.getDecoder().decode(p[2]);
        byte[] calculado = pbkdf2(clave, Base64.getDecoder().decode(p[1]), Integer.parseInt(p[0]));
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] pbkdf2(char[] clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave, sal, iteraciones, LONGITUD_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}