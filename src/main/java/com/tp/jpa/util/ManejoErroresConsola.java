package com.tp.jpa.util;

import java.util.Objects;
import java.util.function.Consumer;

public final class ManejoErroresConsola {
  private final Consumer<String> impresor;

  public ManejoErroresConsola(Consumer<String> impresor) {
    this.impresor = Objects.requireNonNull(impresor, "El impresor no puede ser nulo.");
  }

  public void mostrar(RuntimeException exception) {
    impresor.accept(formatear(exception));
  }

  public void mostrar(String contexto, RuntimeException exception) {
    impresor.accept(formatear(contexto, exception));
  }

  public String formatear(RuntimeException exception) {
    return formatear(null, exception);
  }

  public String formatear(String contexto, RuntimeException exception) {
    String mensaje = mensajeDe(exception);
    if (contexto == null || contexto.isBlank()) {
      return mensaje;
    }
    return contexto + mensaje;
  }

  private String mensajeDe(RuntimeException exception) {
    if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
      return "Error inesperado.";
    }
    return exception.getMessage();
  }
}
