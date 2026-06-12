package ar.edu.tup.programacion3.entities;

import ar.edu.tup.programacion3.dtos.UsuarioDTO;
import ar.edu.tup.programacion3.enums.Rol;
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
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Usuario extends Base {
    private String nombre;
    private String apellido;
    @EqualsAndHashCode.Include
    private String mail;
    private String celular;
    private String contrasenia;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Pedido> pedidos = new HashSet<>();

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
