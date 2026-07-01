package com.tp.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class Categoria extends Base {
  private String nombre;
  private String descripcion;

  @Builder.Default
  @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private Set<Producto> productos = new HashSet<>();

  public void setNombre(String nombre) {
    this.nombre = requireNonBlank(nombre, "El nombre");
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion == null ? "" : descripcion.trim();
  }

  public void setProductos(Set<Producto> productos) {
    this.productos = requireNonNull(productos, "Los productos");
  }

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
