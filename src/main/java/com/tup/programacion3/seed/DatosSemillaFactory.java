package com.tup.programacion3.seed;

import com.tup.programacion3.entities.Categoria;
import com.tup.programacion3.entities.Pedido;
import com.tup.programacion3.entities.Producto;
import com.tup.programacion3.entities.Usuario;
import com.tup.programacion3.enums.Estado;
import com.tup.programacion3.enums.FormaPago;
import com.tup.programacion3.enums.Rol;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class DatosSemillaFactory {
    private DatosSemillaFactory() {
    }

    public static DatosSemilla crear() {
        UsuariosSemilla usuarios = crearUsuarios();
        CategoriasSemilla categorias = crearCategorias();
        ProductosSemilla productos = crearProductos(categorias);
        Set<Pedido> pedidos = crearPedidos(usuarios, productos);
        Producto productoParaComparar = crearProductoEquivalenteAlCafeMolido();

        return new DatosSemilla(
                usuarios.todos(),
                categorias.todas(),
                productos.todos(),
                pedidos,
                productos.cafeMolido(),
                productoParaComparar
        );
    }

    private static UsuariosSemilla crearUsuarios() {
        Set<Usuario> usuarios = new LinkedHashSet<>();
        Usuario ana = new Usuario("Ana", "Garcia", "ana.garcia@mail.com", "3515551001", "ana123", Rol.USUARIO);
        Usuario bruno = new Usuario("Bruno", "Perez", "bruno.perez@mail.com", "3515551002", "bruno123", Rol.ADMIN);
        Usuario guille = new Usuario("Guillermo", "Campoy", "guillecamp@hotmail.com", "1164725999", "guille456", Rol.USUARIO);

        usuarios.add(ana);
        usuarios.add(bruno);
        usuarios.add(guille);

        return new UsuariosSemilla(usuarios, ana, bruno, guille);
    }

    private static CategoriasSemilla crearCategorias() {
        Set<Categoria> categorias = new LinkedHashSet<>();
        Categoria almacen = new Categoria("Almacen", "Productos secos y envasados");
        Categoria bebidas = new Categoria("Bebidas", "Bebidas frias y calientes");
        Categoria limpieza = new Categoria("Limpieza", "Articulos de limpieza del hogar");

        categorias.add(almacen);
        categorias.add(bebidas);
        categorias.add(limpieza);

        return new CategoriasSemilla(categorias, almacen, bebidas, limpieza);
    }

    private static ProductosSemilla crearProductos(CategoriasSemilla categorias) {
        Set<Producto> productos = new LinkedHashSet<>();
        Producto cafeMolido = new Producto("Cafe molido", 3200.0, "Cafe tostado molido 500g", 30, "cafe-molido.jpg", true, categorias.almacen());
        Producto yerbaMate = new Producto("Yerba mate", 2800.0, "Yerba mate tradicional 1kg", 45, "yerba-mate.jpg", true, categorias.almacen());
        Producto arroz = new Producto("Arroz largo fino", 1300.0, "Arroz largo fino 1kg", 60, "arroz.jpg", true, categorias.almacen());
        Producto fideos = new Producto("Fideos tirabuzon", 950.0, "Fideos secos tirabuzon 500g", 80, "fideos.jpg", true, categorias.almacen());
        Producto gaseosa = new Producto("Gaseosa cola", 1800.0, "Gaseosa cola 2.25L", 35, "gaseosa-cola.jpg", true, categorias.bebidas());
        Producto aguaMineral = new Producto("Agua mineral", 900.0, "Agua mineral sin gas 1.5L", 50, "agua-mineral.jpg", true, categorias.bebidas());
        Producto jugoNaranja = new Producto("Jugo de naranja", 1500.0, "Jugo de naranja 1L", 25, "jugo-naranja.jpg", true, categorias.bebidas());
        Producto detergente = new Producto("Detergente", 1200.0, "Detergente concentrado 750ml", 40, "detergente.jpg", true, categorias.limpieza());
        Producto lavandina = new Producto("Lavandina", 1100.0, "Lavandina tradicional 1L", 38, "lavandina.jpg", true, categorias.limpieza());
        Producto esponja = new Producto("Esponja multiuso", 700.0, "Esponja multiuso doble cara", 70, "esponja.jpg", true, categorias.limpieza());

        productos.add(cafeMolido);
        productos.add(yerbaMate);
        productos.add(arroz);
        productos.add(fideos);
        productos.add(gaseosa);
        productos.add(aguaMineral);
        productos.add(jugoNaranja);
        productos.add(detergente);
        productos.add(lavandina);
        productos.add(esponja);

        return new ProductosSemilla(
                productos,
                cafeMolido,
                yerbaMate,
                arroz,
                fideos,
                gaseosa,
                aguaMineral,
                detergente,
                lavandina
        );
    }

    private static Set<Pedido> crearPedidos(UsuariosSemilla usuarios, ProductosSemilla productos) {
        Set<Pedido> pedidos = new LinkedHashSet<>();

        Pedido pedido1 = new Pedido(LocalDate.of(2026, 5, 10), Estado.CONFIRMADO, FormaPago.TARJETA, usuarios.ana());
        pedido1.addDetallePedido(productos.cafeMolido(), 1);
        pedido1.addDetallePedido(productos.gaseosa(), 2);
        pedidos.add(pedido1);

        Pedido pedido2 = new Pedido(LocalDate.of(2026, 5, 12), Estado.PENDIENTE, FormaPago.EFECTIVO, usuarios.ana());
        pedido2.addDetallePedido(productos.yerbaMate(), 1);
        pedido2.addDetallePedido(productos.detergente(), 1);
        pedido2.addDetallePedido(productos.aguaMineral(), 3);
        pedidos.add(pedido2);

        Pedido pedido3 = new Pedido(LocalDate.of(2026, 5, 13), Estado.TERMINADO, FormaPago.TRANSFERENCIA, usuarios.bruno());
        pedido3.addDetallePedido(productos.arroz(), 2);
        pedido3.addDetallePedido(productos.fideos(), 4);
        pedido3.addDetallePedido(productos.lavandina(), 1);
        pedidos.add(pedido3);

        return pedidos;
    }

    private static Producto crearProductoEquivalenteAlCafeMolido() {
        Categoria categoriaEquivalente = new Categoria("Almacen", "Productos secos y envasados");
        return new Producto(
                "Cafe molido",
                3200.0,
                "Cafe tostado molido 500g",
                30,
                "cafe-molido.jpg",
                true,
                categoriaEquivalente
        );
    }

    private record UsuariosSemilla(Set<Usuario> todos, Usuario ana, Usuario bruno, Usuario guille) {
    }

    private record CategoriasSemilla(Set<Categoria> todas, Categoria almacen, Categoria bebidas, Categoria limpieza) {
    }

    private record ProductosSemilla(
            Set<Producto> todos,
            Producto cafeMolido,
            Producto yerbaMate,
            Producto arroz,
            Producto fideos,
            Producto gaseosa,
            Producto aguaMineral,
            Producto detergente,
            Producto lavandina
    ) {
    }
}
