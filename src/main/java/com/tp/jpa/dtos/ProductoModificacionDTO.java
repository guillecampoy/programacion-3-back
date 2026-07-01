package com.tp.jpa.dtos;

public record ProductoModificacionDTO(
    Long id, String nombre, Double precio, Integer stock, Long categoriaId) {}
