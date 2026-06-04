package com.tup.programacion3.entities;

import com.tup.programacion3.enums.Rol;

import java.util.HashSet;
import java.util.Objects;

public class Usuario extends Base{
    private String nombre;
    private String apellido;
    private String mail;
    private String celular;
    private String password;
    private Rol rol;
    private HashSet<Pedido> pedidos;
    public Usuario() {}

    public Usuario(String nombre, String apellido, String mail, String celular, String password, Rol rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.celular = celular;
        this.password = password;
        this.rol = rol;
        this.pedidos = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(nombre, usuario.nombre) && Objects.equals(apellido, usuario.apellido) && Objects.equals(mail, usuario.mail) && Objects.equals(celular, usuario.celular) && Objects.equals(password, usuario.password) && rol == usuario.rol && Objects.equals(pedidos, usuario.pedidos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellido, mail, celular, password, rol, pedidos);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", mail='" + mail + '\'' +
                ", celular='" + celular + '\'' +
                ", password='" + password + '\'' +
                ", rol=" + rol +
                ", pedidos=" + pedidos +
                '}';
    }
}
