package com.tp.jpa.entities;

import com.tp.jpa.dtos.UsuarioDTO;
import com.tp.jpa.enums.Rol;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario extends Base {
    private String nombre;
    private String apellido;
    private String mail;
    private String celular;
    private String contrasenia;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Pedido> pedidos = new HashSet<>();

    public void setNombre(String nombre) {
        this.nombre = requireNonBlank(nombre, "El nombre");
    }

    public void setApellido(String apellido) {
        this.apellido = requireNonBlank(apellido, "El apellido");
    }

    public void setMail(String mail) {
        String mailNormalizado = requireNonBlank(mail, "El mail");
        if (!mailNormalizado.contains("@")) {
            throw new IllegalArgumentException("El mail debe contener @.");
        }
        this.mail = mailNormalizado;
    }

    public void setCelular(String celular) {
        this.celular = requireNonBlank(celular, "El celular");
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = requireNonBlank(contrasenia, "La contrasenia");
    }

    public void setRol(Rol rol) {
        this.rol = requireNonNull(rol, "El rol");
    }

    public void setPedidos(Set<Pedido> pedidos) {
        this.pedidos = requireNonNull(pedidos, "Los pedidos");
    }

    public void addPedido(Pedido pedido) {
        if (pedido == null) {
            return;
        }
        this.pedidos.add(pedido);
        if (pedido.getUsuario() != this) {
            pedido.setUsuario(this);
        }
    }

    @Override
    public String toString() {
        return UsuarioDTO.from(this).toString();
    }

}
