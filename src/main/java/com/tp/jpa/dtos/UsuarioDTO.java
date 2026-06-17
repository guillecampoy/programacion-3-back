package com.tp.jpa.dtos;

import com.tp.jpa.model.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record UsuarioDTO(
        Long id,
        Boolean eliminado,
        LocalDateTime createdAt,
        String nombre,
        String apellido,
        String mail,
        String celular
) {
    private static final DateTimeFormatter ARG_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy/HH:mm 'UTC-3 (ARG)'");

    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getEliminado(),
                usuario.getCreatedAt(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getMail(),
                usuario.getCelular()
        );
    }

    @Override
    public String toString() {
        return "UsuarioDTO[" +
                "id=" + id +
                ", eliminado=" + eliminado +
                ", createdAt=" + formatCreatedAt() +
                ", nombre=" + nombre +
                ", apellido=" + apellido +
                ", mail=" + mail +
                ", celular=" + celular +
                "]";
    }

    private String formatCreatedAt() {
        if (createdAt == null) {
            return null;
        }
        return createdAt.format(ARG_DATE_TIME_FORMAT);
    }
}
