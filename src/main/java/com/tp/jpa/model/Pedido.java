package com.tp.jpa.model;

import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class Pedido extends Base implements Calculable {
    private LocalDate fecha;
    @Enumerated(EnumType.STRING)
    private Estado estado;
    @Builder.Default
    private Double total = 0.0;
    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;
    @ManyToOne
    @ToString.Exclude
    private Usuario usuario;
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pedido_id")
    private Set<DetallePedido> detallePedidos = new HashSet<>();

    public void setFecha(LocalDate fecha) {
        this.fecha = requireNonNull(fecha, "La fecha");
    }

    public void setEstado(Estado estado) {
        this.estado = requireNonNull(estado, "El estado");
    }

    public void setTotal(Double total) {
        requireNonNull(total, "El total");
        if (total < 0) {
            throw new IllegalArgumentException("El total debe ser mayor o igual a 0.");
        }
        this.total = total;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = requireNonNull(formaPago, "La forma de pago");
    }

    public void setDetallePedidos(Set<DetallePedido> detallePedidos) {
        this.detallePedidos = requireNonNull(detallePedidos, "Los detalles del pedido");
        calcularTotal();
    }

    public void setUsuario(Usuario usuario) {
        if (this.usuario == usuario) {
            return;
        }

        Usuario usuarioAnterior = this.usuario;
        this.usuario = usuario;

        if (usuarioAnterior != null) {
            usuarioAnterior.getPedidos().remove(this);
        }
        if (usuario != null && !usuario.getPedidos().contains(this)) {
            usuario.addPedido(this);
        }
    }

    public void addDetallePedido(int cantidad, Producto producto) {
        requireMin(cantidad, 1, "La cantidad");
        requireNonNull(producto, "El producto");
        requirePositive(producto.getPrecio(), "El precio del producto");

        DetallePedido detallePedidoExistente = findDetallePedidoByProducto(producto);
        if (detallePedidoExistente != null) {
            int nuevaCantidad = detallePedidoExistente.getCantidad() + cantidad;
            detallePedidoExistente.setCantidad(nuevaCantidad);
            detallePedidoExistente.setSubtotal(producto.getPrecio() * nuevaCantidad);
            calcularTotal();
            return;
        }

        DetallePedido detallePedido = DetallePedido.builder()
                .producto(producto)
                .cantidad(cantidad)
                .subtotal(producto.getPrecio() * cantidad)
                .build();
        this.detallePedidos.add(detallePedido);
        calcularTotal();
    }

    public void addDetallePedido(Producto producto, int cantidad) {
        addDetallePedido(cantidad, producto);
    }

    public DetallePedido findDetallePedidoByProducto(Producto producto) {
        for (DetallePedido detallePedido : detallePedidos) {
            if (Objects.equals(detallePedido.getProducto(), producto)) {
                return detallePedido;
            }
        }
        return null;
    }

    public DetallePedido findeDetallePedidoByProducto(Producto producto) {
        return findDetallePedidoByProducto(producto);
    }

    public void deleteDetallePedidoByProducto(Producto producto) {
        detallePedidos.removeIf(detallePedido -> Objects.equals(detallePedido.getProducto(), producto));
        calcularTotal();
    }

    public Pedido(Usuario usuario) {
        this(LocalDate.now(), Estado.PENDIENTE, FormaPago.EFECTIVO, usuario);
    }

    public Pedido(LocalDate fecha, Estado estado, FormaPago formaPago, Usuario usuario) {
        setFecha(fecha);
        setEstado(estado);
        setTotal(0.0);
        setFormaPago(formaPago);
        this.usuario = usuario;
        this.detallePedidos = new HashSet<>();
        if (usuario != null) {
            usuario.addPedido(this);
        }
    }

    @Override
    public void calcularTotal() {
        this.total = detallePedidos.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
    }
}
