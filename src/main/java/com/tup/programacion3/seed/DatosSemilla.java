package com.tup.programacion3.seed;

import com.tup.programacion3.entities.Categoria;
import com.tup.programacion3.entities.Pedido;
import com.tup.programacion3.entities.Producto;
import com.tup.programacion3.entities.Usuario;

import java.util.Set;

public record DatosSemilla(
        Set<Usuario> usuarios,
        Set<Categoria> categorias,
        Set<Producto> productos,
        Set<Pedido> pedidos,
        Producto productoParaMostrar,
        Producto productoParaComparar
) {
}
