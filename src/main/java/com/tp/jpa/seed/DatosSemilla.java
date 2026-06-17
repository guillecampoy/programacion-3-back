package com.tp.jpa.seed;

import com.tp.jpa.entities.Categoria;
import com.tp.jpa.entities.Pedido;
import com.tp.jpa.entities.Producto;
import com.tp.jpa.entities.Usuario;

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
