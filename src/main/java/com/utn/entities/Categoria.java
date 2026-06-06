package com.utn.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@ToString
public class Categoria extends Base {
    private String nombre;
    private String descripcion;
    private Set<Producto> productos;

    public void addProducto(Producto producto) {
        this.productos.add(producto);
    }

}
