package com.tp.jpa.seed;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;

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
