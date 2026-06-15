package ar.edu.tup.programacion3.utils;

import java.util.Set;
import java.util.Scanner;
import java.util.function.Predicate;

public class EntradaValidada {
    private final Scanner scanner;

    public EntradaValidada(Scanner scanner) {
        this.scanner = scanner;
    }

    public String leerOpcion(String prompt, Set<String> opcionesValidas) {
        return leerTextoValidado(
                prompt,
                entrada -> opcionesValidas.contains(entrada),
                "Opcion invalida."
        );
    }

    public String leerTextoNoVacio(String prompt) {
        return leerTextoValidado(
                prompt,
                entrada -> !entrada.isBlank(),
                "Ingrese un texto no vacio."
        );
    }

    public long leerLong(String prompt, Predicate<Long> validador, String mensajeError) {
        while (true) {
            String entrada = leerLinea(prompt);
            if (!entrada.matches("\\d+")) {
                System.out.println(mensajeError);
                continue;
            }

            long valor;
            try {
                valor = Long.parseLong(entrada);
            } catch (NumberFormatException exception) {
                System.out.println(mensajeError);
                continue;
            }

            if (validador.test(valor)) {
                return valor;
            }
            System.out.println(mensajeError);
        }
    }

    public int leerEntero(String prompt, int minimo) {
        while (true) {
            String entrada = leerLinea(prompt);
            if (!entrada.matches("\\d+")) {
                System.out.println("Ingrese un numero entero mayor o igual a " + minimo + ".");
                continue;
            }

            int valor;
            try {
                valor = Integer.parseInt(entrada);
            } catch (NumberFormatException exception) {
                System.out.println("Ingrese un numero entero mayor o igual a " + minimo + ".");
                continue;
            }

            if (valor >= minimo) {
                return valor;
            }
            System.out.println("Ingrese un numero entero mayor o igual a " + minimo + ".");
        }
    }

    public double leerDecimal(String prompt, double minimo) {
        while (true) {
            String entrada = leerLinea(prompt);
            if (!entrada.matches("\\d+(\\.\\d+)?")) {
                System.out.println("Ingrese un numero decimal mayor o igual a " + minimo + ".");
                continue;
            }

            double valor;
            try {
                valor = Double.parseDouble(entrada);
            } catch (NumberFormatException exception) {
                System.out.println("Ingrese un numero decimal mayor o igual a " + minimo + ".");
                continue;
            }

            if (valor >= minimo) {
                return valor;
            }
            System.out.println("Ingrese un numero decimal mayor o igual a " + minimo + ".");
        }
    }

    public boolean leerBooleano(String prompt) {
        while (true) {
            String entrada = leerLinea(prompt).toLowerCase();
            if (Set.of("s", "si", "true").contains(entrada)) {
                return true;
            }
            if (Set.of("n", "no", "false").contains(entrada)) {
                return false;
            }
            System.out.println("Ingrese si/no, s/n o true/false.");
        }
    }

    private String leerTextoValidado(
            String prompt,
            Predicate<String> validador,
            String mensajeError
    ) {
        while (true) {
            String entrada = leerLinea(prompt);
            if (validador.test(entrada)) {
                return entrada;
            }
            System.out.println(mensajeError);
        }
    }

    private String leerLinea(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("No hay mas entrada disponible.");
        }
        return scanner.nextLine().trim();
    }
}
