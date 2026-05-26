package com.tpo.util;

public final class Validator {

    private Validator() {
    }

    public static void validarNoVacio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
    }
}
