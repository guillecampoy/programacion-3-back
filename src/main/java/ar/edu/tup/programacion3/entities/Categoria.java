package ar.edu.tup.programacion3.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
@ToString
public class Categoria extends Base {
    @EqualsAndHashCode.Include
    private String nombre;
    private String descripcion;
    @Builder.Default
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Producto> productos = new HashSet<>();

    public void addProducto(Producto producto) {
        if (producto == null) {
            return;
        }
        this.productos.add(producto);
        if (producto.getCategoria() != this) {
            producto.setCategoria(this);
        }
    }

}
