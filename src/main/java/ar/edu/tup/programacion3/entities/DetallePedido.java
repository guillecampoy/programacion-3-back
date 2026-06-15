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
public class DetallePedido extends Base {
    private int cantidad;
    private Double subtotal;
    @ManyToOne
    private Producto producto;

    public void setCantidad(int cantidad) {
        this.cantidad = requireMin(cantidad, 1, "La cantidad");
    }

    public void setSubtotal(Double subtotal) {
        requireNonNull(subtotal, "El subtotal");
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal debe ser mayor o igual a 0.");
        }
        this.subtotal = subtotal;
    }

    public void setProducto(Producto producto) {
        this.producto = requireNonNull(producto, "El producto");
    }
}
