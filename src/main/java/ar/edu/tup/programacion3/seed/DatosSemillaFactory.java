package ar.edu.tup.programacion3.seed;

import ar.edu.tup.programacion3.entities.Categoria;
import ar.edu.tup.programacion3.entities.Pedido;
import ar.edu.tup.programacion3.entities.Producto;
import ar.edu.tup.programacion3.entities.Usuario;
import ar.edu.tup.programacion3.enums.Estado;
import ar.edu.tup.programacion3.enums.FormaPago;
import ar.edu.tup.programacion3.enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Usuario ana = Usuario.builder()
                .nombre("Ana")
                .apellido("Garcia")
                .id(48L)
                .mail("anagarcia@gmail.com")
                .celular("123-456-7890")
                .contrasenia("Anita345")
                .createdAt(LocalDateTime.now())
                .rol(Rol.USUARIO)
                .eliminado(false)
                .build();
        Usuario bruno = Usuario.builder()
                .nombre("Bruno")
                .apellido("Juárez")
                .id(50L)
                .mail("bjuarez90@gmail.com")
                .celular("123-456-7890")
                .contrasenia("BruN096")
                .createdAt(LocalDateTime.now())
                .rol(Rol.ADMIN)
                .eliminado(false)
                .build();

        usuarios.add(ana);
        usuarios.add(bruno);

        return new UsuariosSemilla(usuarios, ana, bruno);
}

private static CategoriasSemilla crearCategorias() {
        Set<Categoria> categorias = new LinkedHashSet<>();
        Categoria almacen = Categoria.builder()
                .nombre("Almacen")
                .descripcion("Productos secos y envasados")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Categoria bebidas = Categoria.builder()
                .nombre("Bebidas")
                .descripcion("Bebidas frias y calientes")
                .id(2L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Categoria limpieza = Categoria.builder()
                .nombre("Limpieza")
                .descripcion("Articulos de limpieza del hogar")
                .id(3L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        categorias.add(almacen);
        categorias.add(bebidas);
        categorias.add(limpieza);

        return new CategoriasSemilla(categorias, almacen, bebidas, limpieza);
}

private static ProductosSemilla crearProductos(CategoriasSemilla categorias) {
        Set<Producto> productos = new LinkedHashSet<>();
        Producto cafeMolido = Producto.builder()
                .nombre("Cafe molido")
                .precio(3200.0)
                .descripcion("Cafe tostado molido 500g")
                .imagen("cafe-molido.jpg")
                .disponible(true)
                .stock(20)
                .categoria(categorias.almacen())
                .id(1L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto yerbaMate = Producto.builder()
                .nombre("Yerba mate")
                .precio(2800.0)
                .descripcion("Yerba mate tradicional 1kg")
                .imagen("yerba.jpg")
                .disponible(true)
                .stock(48)
                .categoria(categorias.almacen())
                .id(2L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto arroz = Producto.builder()
                .nombre("Arroz largo fino")
                .precio(1300.0)
                .descripcion("Arroz largo fino 1kg")
                .imagen("arroz.jpg")
                .disponible(true)
                .stock(99)
                .categoria(categorias.almacen())
                .id(3L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto fideos = Producto.builder()
                .nombre("Fideos moñito")
                .precio(950.0)
                .descripcion("Fideos moñito 500g")
                .imagen("fideos-moñito.jpg")
                .disponible(true)
                .stock(150)
                .categoria(categorias.almacen())
                .id(4L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto gaseosa = Producto.builder()
                .nombre("Gaseosa cola")
                .precio(1800.0)
                .descripcion("Gaseosa cola 2.25L")
                .imagen("gaseosa-cola.jpg")
                .disponible(true)
                .stock(15)
                .categoria(categorias.bebidas())
                .id(5L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto aguaMineral = Producto.builder()
                .nombre("Agua Mineral")
                .precio(900.0)
                .descripcion("Agua mineral sin gas 1.5L")
                .imagen("agua.jpg")
                .disponible(true)
                .stock(25)
                .categoria(categorias.bebidas())
                .id(6L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto jugoNaranja = Producto.builder()
                .nombre("Jugo de naranja")
                .precio(1500.0)
                .descripcion("Jugo de naranja 1L")
                .imagen("jugo-naranja.jpg")
                .disponible(true)
                .stock(259)
                .categoria(categorias.almacen())
                .id(7L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto detergente = Producto.builder()
                .nombre("Detergente")
                .precio(1200.0)
                .descripcion("Detergente concentrado limón 750ml")
                .imagen("detergente-limon.jpg")
                .disponible(true)
                .stock(99)
                .categoria(categorias.limpieza())
                .id(8L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto lavandina = Producto.builder()
                .nombre("Lavandina")
                .precio(1100.0)
                .descripcion("Lavandina concentrada 1L")
                .imagen("lavandina.jpg")
                .disponible(true)
                .stock(80)
                .categoria(categorias.limpieza())
                .id(9L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto esponja = Producto.builder()
                .nombre("Esponja multiuso")
                .precio(700.0)
                .descripcion("Esponja multiuso doble cara")
                .imagen("esponja.jpg")
                .disponible(true)
                .stock(4)
                .categoria(categorias.almacen())
                .id(10L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        Producto jabonTocador = Producto.builder()
                .nombre("Jabón de manos")
                .precio(1100.0)
                .descripcion("Jabón neutro")
                .imagen("jabon_tocador.jpg")
                .disponible(true)
                .stock(2)
                .categoria(categorias.almacen())
                .id(11L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();

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
        productos.add(jabonTocador);

        categorias.almacen().addProducto(cafeMolido);
        categorias.almacen().addProducto(yerbaMate);
        categorias.almacen().addProducto(arroz);
        categorias.almacen().addProducto(fideos);
        categorias.bebidas().addProducto(gaseosa);
        categorias.bebidas().addProducto(aguaMineral);
        categorias.almacen().addProducto(jugoNaranja);
        categorias.limpieza().addProducto(detergente);
        categorias.limpieza().addProducto(lavandina);
        categorias.limpieza().addProducto(jabonTocador);
        categorias.almacen().addProducto(esponja);

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

        Pedido pedido1 = crearPedido(1L, LocalDate.of(2026, 5, 10), Estado.CONFIRMADO, FormaPago.TARJETA, usuarios.ana());
        pedido1.addDetallePedido(productos.cafeMolido(), 1);
        pedido1.addDetallePedido(productos.gaseosa(), 2);
        pedidos.add(pedido1);

        Pedido pedido2 = crearPedido(2L, LocalDate.of(2026, 5, 12), Estado.PENDIENTE, FormaPago.EFECTIVO, usuarios.ana());
        pedido2.addDetallePedido(productos.yerbaMate(), 1);
        pedido2.addDetallePedido(productos.detergente(), 1);
        pedido2.addDetallePedido(productos.aguaMineral(), 3);
        pedidos.add(pedido2);

        Pedido pedido3 = crearPedido(3L, LocalDate.of(2026, 5, 13), Estado.TERMINADO, FormaPago.TRANSFERENCIA, usuarios.bruno());
        pedido3.addDetallePedido(productos.arroz(), 2);
        pedido3.addDetallePedido(productos.fideos(), 4);
        pedido3.addDetallePedido(productos.lavandina(), 1);
        pedidos.add(pedido3);

        asignarIdsDetalles(pedidos);

        return pedidos;
}

private static void asignarIdsDetalles(Set<Pedido> pedidos) {
        long id = 1L;
        for (Pedido pedido : pedidos) {
                for (var detallePedido : pedido.getDetallePedidos()) {
                        detallePedido.setId(id);
                        id++;
                }
        }
}

private static Pedido crearPedido(Long id, LocalDate fecha, Estado estado, FormaPago formaPago, Usuario usuario) {
        Pedido pedido = Pedido.builder()
                .id(id)
                .fecha(fecha)
                .estado(estado)
                .formaPago(formaPago)
                .usuario(usuario)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        usuario.addPedido(pedido);
        return pedido;
}

private static Producto crearProductoEquivalenteAlCafeMolido() {
        Categoria categoriaEquivalente = Categoria.builder()
                .nombre("Almacen")
                .descripcion("Productos secos y envasados")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
        return Producto.builder()
                .nombre("Cafe molido")
                .precio(3200.0)
                .descripcion("Cafe tostado molido 500g")
                .imagen("cafe-molido.jpg")
                .disponible(true)
                .stock(20)
                .categoria(categoriaEquivalente)
                .id(1L)
                .createdAt(LocalDateTime.now())
                .eliminado(false)
                .build();
}

private record UsuariosSemilla(Set<Usuario> todos, Usuario ana, Usuario bruno) {
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
