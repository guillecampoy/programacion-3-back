package com.tp.jpa.dtos;

import com.tp.jpa.model.enums.Rol;

public record UsuarioAltaDTO(
    String nombre, String apellido, String mail, String celular, String contrasenia, Rol rol) {}
