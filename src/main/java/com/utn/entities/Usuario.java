package com.utn.entities;

import com.utn.dtos.UsuarioDTO;
import com.utn.enums.Rol;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Usuario extends Base{
    private String nombre;
    private String apellido;
    @EqualsAndHashCode.Include
    private String mail;
    private String celular;
    private String contrasenia;
    private Rol rol;
    @Builder.Default
    @ToString.Exclude
    private Set<Pedido> pedidos = new HashSet<>();

    public void addPedido(Pedido pedido) {
        this.pedidos.add(pedido);
    }

    @Override
    public String toString() {
        return UsuarioDTO.from(this).toString();
    }

}
