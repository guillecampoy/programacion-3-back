package ar.edu.tup.programacion3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DetallePedido detallePedido)) {
            return false;
        }

        return producto != null && Objects.equals(producto, detallePedido.producto);
    }

    @Override
    public int hashCode() {
        return producto == null ? System.identityHashCode(this) : producto.hashCode();
    }
}
