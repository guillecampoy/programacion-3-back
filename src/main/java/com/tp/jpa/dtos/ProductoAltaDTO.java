package com.tp.jpa.dtos;

public record ProductoAltaDTO(
    Long categoriaId,
    String nombre,
    String descripcion,
    double precio,
    int stock,
    String imagen,
    boolean disponible) {}
