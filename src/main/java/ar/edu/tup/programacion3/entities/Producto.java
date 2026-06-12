package ar.edu.tup.programacion3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString
public class Producto extends Base {
    @EqualsAndHashCode.Include
    private String nombre;
    private Double precio;
    private String descripcion;
    private int stock;
    private String imagen;
    private Boolean disponible;
    @ManyToOne
    private Categoria categoria;

    public void setCategoria(Categoria categoria) {
        if (this.categoria == categoria) {
            return;
        }

        Categoria categoriaAnterior = this.categoria;
        this.categoria = categoria;

        if (categoriaAnterior != null) {
            categoriaAnterior.getProductos().remove(this);
        }
        if (categoria != null && !categoria.getProductos().contains(this)) {
            categoria.addProducto(this);
        }
    }
}
