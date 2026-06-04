package com.tup.programacion3.entities;

import com.tup.programacion3.enums.Estado;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;

public class Pedido extends Base implements Calculable {
    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private Usuario usuario;
    private HashSet<DetallePedido> detallePedidos;



    public void addDetallePedido(Producto producto, int cantidad) {
        DetallePedido detallePedido = new DetallePedido(producto, cantidad);
    };

    public DetallePedido findeDetallePedidoByProducto (Producto producto){
        return null;
    }

    public void deleteDetallePedidoByProducto(Producto producto){};

    public Pedido(Usuario usuario) {
        this.usuario = usuario;
        this.detallePedidos = new HashSet<>();
    }

    @Override
    public void calcularTotal() {

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pedido pedido)) return false;
        return Objects.equals(fecha, pedido.fecha) && estado == pedido.estado && Objects.equals(total, pedido.total) && Objects.equals(usuario, pedido.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fecha, estado, total, usuario);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "fecha=" + fecha +
                ", estado=" + estado +
                ", total=" + total +
                ", usuario=" + usuario +
                '}';
    }
}
