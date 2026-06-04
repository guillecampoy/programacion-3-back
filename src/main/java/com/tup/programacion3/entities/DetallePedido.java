package com.tup.programacion3.entities;

import java.util.Objects;

public class DetallePedido extends Base {
    private int cantidad;
    private double subtotal;
    private Producto producto;

    public DetallePedido(Producto producto, int cantidad) {
        this.cantidad = cantidad;
        this.producto = producto;
        //this.subtotal = subtotal;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DetallePedido that)) return false;
        return cantidad == that.cantidad && Double.compare(subtotal, that.subtotal) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cantidad, subtotal);
    }

    @Override
    public String toString() {
        return "DetallePedido{" +
                "cantidad=" + cantidad +
                ", subtotal=" + subtotal +
                '}';
    }
}
