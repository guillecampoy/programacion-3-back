package ar.edu.tup.programacion3.utils;

import org.junit.jupiter.api.Test;

import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntradaValidadaTest {
    @Test
    void rechazaEntradaVaciaEnOpcionesSinDefault() {
        EntradaValidada entradaValidada = crearEntrada("\n2\n");

        String opcion = entradaValidada.leerOpcion("Campo: ", Set.of("1", "2"));

        assertEquals("2", opcion);
    }

    @Test
    void rechazaOpcionInvalidaHastaRecibirOpcionValida() {
        EntradaValidada entradaValidada = crearEntrada("x\n2\n");

        String opcion = entradaValidada.leerOpcion("Opcion: ", Set.of("1", "2"));

        assertEquals("2", opcion);
    }

    @Test
    void validaEnteroConMinimo() {
        EntradaValidada entradaValidada = crearEntrada("-1\nabc\n5\n");

        int valor = entradaValidada.leerEntero("Stock: ", 0);

        assertEquals(5, valor);
    }

    @Test
    void validaDecimalConMinimo() {
        EntradaValidada entradaValidada = crearEntrada("0\nabc\n10.5\n");

        double valor = entradaValidada.leerDecimal("Precio: ", 0.01);

        assertEquals(10.5, valor);
    }

    @Test
    void validaBooleano() {
        assertTrue(crearEntrada("si\n").leerBooleano("Disponible: "));
        assertFalse(crearEntrada("no\n").leerBooleano("Disponible: "));
    }

    @Test
    void validaTextoNoVacio() {
        EntradaValidada entradaValidada = crearEntrada("\nCafe tostado\n");

        String texto = entradaValidada.leerTextoNoVacio("Nombre: ");

        assertEquals("Cafe tostado", texto);
    }

    @Test
    void validaTextoConPredicadoPersonalizado() {
        EntradaValidada entradaValidada = crearEntrada("con espacios\ngmail\n");

        String texto = entradaValidada.leerTexto(
                "Texto: ",
                entrada -> !entrada.matches(".*\\s+.*"),
                "Ingrese un texto sin espacios."
        );

        assertEquals("gmail", texto);
    }

    private EntradaValidada crearEntrada(String entrada) {
        return new EntradaValidada(new Scanner(entrada));
    }
}
