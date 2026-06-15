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
@ToString
public class Producto extends Base {
    private String nombre;
    private Double precio;
    private String descripcion;
    private int stock;
    private String imagen;
    private Boolean disponible;
    @ManyToOne
    private Categoria categoria;

    public void setNombre(String nombre) {
        this.nombre = requireNonBlank(nombre, "El nombre");
    }

    public void setPrecio(Double precio) {
        this.precio = requirePositive(precio, "El precio");
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = requireNonBlank(descripcion, "La descripcion");
    }

    public void setStock(int stock) {
        this.stock = requireMin(stock, 0, "El stock");
    }

    public void setImagen(String imagen) {
        this.imagen = requireNonBlank(imagen, "La imagen");
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = requireNonNull(disponible, "La disponibilidad");
    }

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
