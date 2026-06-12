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
public class DetallePedido extends Base {
    @EqualsAndHashCode.Include
    private int cantidad;
    @EqualsAndHashCode.Include
    private Double subtotal;
    @EqualsAndHashCode.Include
    @ManyToOne
    private Producto producto;

}
