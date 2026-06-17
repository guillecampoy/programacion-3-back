package com.tp.jpa.utils;

public final class ConsolaUtils {
    public static final String SEPARADOR = "------------------------------------------------------------";

    private static final String PREFIJO_MENSAJE = "> ";
    private static final String PREFIJO_ERROR = "  ! ";

    private ConsolaUtils() {
    }

    public static void imprimirTitulo(String titulo) {
        System.out.println();
        System.out.println(SEPARADOR);
        System.out.println(titulo);
        System.out.println(SEPARADOR);
    }

    public static void imprimirSubtitulo(String titulo) {
        System.out.println();
        System.out.println(titulo);
        System.out.println(SEPARADOR);
    }

    public static void imprimirOpcion(String opcion, String descripcion) {
        System.out.printf("  %s) %-50s%n", opcion, descripcion);
    }

    public static void imprimirDato(String etiqueta, Object valor) {
        System.out.printf("  %-35s %s%n", etiqueta + ":", valor);
    }

    public static void imprimirMensaje(String mensaje) {
        System.out.println(PREFIJO_MENSAJE + mensaje);
    }

    public static void imprimirError(String mensaje) {
        System.out.println(PREFIJO_ERROR + mensaje);
    }

    public static String prompt(String etiqueta) {
        return PREFIJO_MENSAJE + etiqueta + ": ";
    }
}
