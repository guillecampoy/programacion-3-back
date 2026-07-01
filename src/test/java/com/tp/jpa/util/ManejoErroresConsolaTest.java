package com.tp.jpa.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ManejoErroresConsolaTest {

  @Test
  void muestraMensajeDelErrorSinContexto() {
    List<String> mensajes = new ArrayList<>();
    ManejoErroresConsola manejador = new ManejoErroresConsola(mensajes::add);

    manejador.mostrar(new IllegalStateException("Error: fallo inesperado."));

    assertEquals(List.of("Error: fallo inesperado."), mensajes);
  }

  @Test
  void agregaContextoAlMensajeDelError() {
    ManejoErroresConsola manejador = new ManejoErroresConsola(mensaje -> {});

    String formateado =
        manejador.formatear("No se pudo guardar: ", new IllegalStateException("Error: detalle"));

    assertEquals("No se pudo guardar: Error: detalle", formateado);
  }

  @Test
  void usaFallbackCuandoLaExcepcionNoTraeMensaje() {
    ManejoErroresConsola manejador = new ManejoErroresConsola(mensaje -> {});

    assertEquals("Error inesperado.", manejador.formatear(new RuntimeException()));
  }
}
