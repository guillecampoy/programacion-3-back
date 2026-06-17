package com.tp.jpa.model;

import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.seed.DatosSemilla;
import com.tp.jpa.seed.DatosSemillaFactory;
import com.tp.jpa.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelacionesTest {
    @Test
    void entidadesPrincipalesExtiendenBaseYPedidoImplementaCalculable() {
        assertInstanceOf(Base.class, Usuario.builder().build());
        assertInstanceOf(Base.class, Categoria.builder().build());
        assertInstanceOf(Base.class, Producto.builder().build());
        assertInstanceOf(Base.class, DetallePedido.builder().build());

        Pedido pedido = Pedido.builder().build();
        assertInstanceOf(Base.class, pedido);
        assertInstanceOf(Calculable.class, pedido);
    }

    @Test
    void usuarioMantieneRelacionConPedidos() {
        Usuario usuario = Usuario.builder()
                .mail("usuario@test.com")
                .build();
        Pedido pedido = crearPedidoSinUsuario();

        usuario.addPedido(pedido);

        assertTrue(usuario.getPedidos().contains(pedido));
        assertSame(usuario, pedido.getUsuario());
    }

    @Test
    void pedidoMantieneRelacionConUsuario() {
        Usuario usuario = Usuario.builder()
                .mail("usuario@test.com")
                .build();
        Pedido pedido = crearPedidoSinUsuario();

        pedido.setUsuario(usuario);

        assertSame(usuario, pedido.getUsuario());
        assertTrue(usuario.getPedidos().contains(pedido));
    }

    @Test
    void categoriaMantieneRelacionConProductos() {
        Categoria categoria = Categoria.builder()
                .nombre("Almacen")
                .build();
        Producto producto = crearProducto("Cafe molido", 3200.0);

        categoria.addProducto(producto);

        assertTrue(categoria.getProductos().contains(producto));
        assertSame(categoria, producto.getCategoria());
    }

    @Test
    void productoMantieneRelacionConCategoria() {
        Categoria categoria = Categoria.builder()
                .nombre("Almacen")
                .build();
        Producto producto = crearProducto("Cafe molido", 3200.0);

        producto.setCategoria(categoria);

        assertSame(categoria, producto.getCategoria());
        assertTrue(categoria.getProductos().contains(producto));
    }

    @Test
    void pedidoComponeDetallesYDetalleReferenciaUnProducto() {
        Pedido pedido = crearPedidoSinUsuario();
        Producto producto = crearProducto("Cafe molido", 3200.0);

        pedido.addDetallePedido(2, producto);

        DetallePedido detallePedido = pedido.findDetallePedidoByProducto(producto);
        assertEquals(1, pedido.getDetallePedidos().size());
        assertSame(producto, detallePedido.getProducto());
        assertEquals(6400.0, detallePedido.getSubtotal());
        assertEquals(6400.0, pedido.getTotal());
    }

    @Test
    void detallePedidoUsaProductoComoIdentidadLogica() {
        Producto producto = Producto.builder()
                .id(1L)
                .nombre("Cafe molido")
                .precio(3200.0)
                .build();
        DetallePedido detalle = DetallePedido.builder()
                .producto(producto)
                .cantidad(1)
                .subtotal(3200.0)
                .build();
        DetallePedido mismoProductoConOtraCantidad = DetallePedido.builder()
                .producto(producto)
                .cantidad(3)
                .subtotal(9600.0)
                .build();

        assertEquals(detalle, mismoProductoConOtraCantidad);
        assertEquals(detalle.hashCode(), mismoProductoConOtraCantidad.hashCode());
    }

    @Test
    void pedidoAgrupaDetallesDelMismoProducto() {
        Pedido pedido = crearPedidoSinUsuario();
        Producto producto = crearProducto("Cafe molido", 3200.0);

        pedido.addDetallePedido(1, producto);
        pedido.addDetallePedido(2, producto);

        DetallePedido detallePedido = pedido.findDetallePedidoByProducto(producto);
        assertEquals(1, pedido.getDetallePedidos().size());
        assertEquals(3, detallePedido.getCantidad());
        assertEquals(9600.0, detallePedido.getSubtotal());
        assertEquals(9600.0, pedido.getTotal());
    }

    @Test
    void datosSemillaMantienenRelacionesDelDiagrama() {
        DatosSemilla datosSemilla = DatosSemillaFactory.crear();

        datosSemilla.pedidos().forEach(pedido -> {
            assertTrue(pedido.getUsuario().getPedidos().contains(pedido));
            assertTrue(pedido.getDetallePedidos().size() >= 1);
            pedido.getDetallePedidos().forEach(detallePedido ->
                    assertTrue(datosSemilla.productos().contains(detallePedido.getProducto()))
            );
        });
        datosSemilla.productos().forEach(producto ->
                assertTrue(producto.getCategoria().getProductos().contains(producto))
        );
        datosSemilla.categorias().forEach(categoria ->
                assertTrue(categoria.getProductos().size() >= 1)
        );
    }

    @Test
    void igualdadUsaIdYNoCamposMutablesNiCreatedAt() {
        Usuario usuario = Usuario.builder()
                .id(48L)
                .mail("usuario@test.com")
                .createdAt(LocalDateTime.of(2026, 5, 10, 12, 0))
                .build();
        Usuario mismoUsuario = Usuario.builder()
                .id(48L)
                .mail("mail.actualizado@test.com")
                .createdAt(LocalDateTime.of(2026, 5, 10, 12, 1))
                .build();

        assertEquals(usuario, mismoUsuario);
        assertEquals(usuario.hashCode(), mismoUsuario.hashCode());
    }

    @Test
    void entidadesSinIdNoSonIgualesAunqueSeanDelMismoTipo() {
        Producto cafe = crearProducto("Cafe molido", 3200.0);
        Producto otroCafe = crearProducto("Cafe molido", 3200.0);
        Set<Producto> productos = new HashSet<>();

        productos.add(cafe);
        productos.add(otroCafe);

        assertNotEquals(cafe, otroCafe);
        assertEquals(2, productos.size());
    }

    @Test
    void settersValidanDatosBasicosDeProducto() {
        Producto producto = Producto.builder().build();

        assertThrows(IllegalArgumentException.class, () -> producto.setId(0L));
        assertThrows(IllegalArgumentException.class, () -> producto.setNombre(" "));
        assertThrows(IllegalArgumentException.class, () -> producto.setPrecio(0.0));
        assertThrows(IllegalArgumentException.class, () -> producto.setDescripcion(""));
        assertThrows(IllegalArgumentException.class, () -> producto.setStock(-1));
        assertThrows(IllegalArgumentException.class, () -> producto.setImagen(null));
        assertThrows(IllegalArgumentException.class, () -> producto.setDisponible(null));

        producto.setId(1L);
        producto.setNombre(" Cafe molido ");
        producto.setPrecio(3200.0);
        producto.setDescripcion("Cafe tostado molido 500g");
        producto.setStock(20);
        producto.setImagen("cafe-molido.jpg");
        producto.setDisponible(true);

        assertEquals("Cafe molido", producto.getNombre());
        assertEquals(3200.0, producto.getPrecio());
        assertEquals(20, producto.getStock());
    }

    @Test
    void pedidoValidaDatosBasicosAlAgregarDetalle() {
        Pedido pedido = crearPedidoSinUsuario();
        Producto productoSinPrecio = Producto.builder()
                .nombre("Cafe molido")
                .build();
        Producto productoValido = crearProducto("Cafe molido", 3200.0);

        assertThrows(IllegalArgumentException.class, () -> pedido.addDetallePedido(0, productoValido));
        assertThrows(IllegalArgumentException.class, () -> pedido.addDetallePedido(1, null));
        assertThrows(IllegalArgumentException.class, () -> pedido.addDetallePedido(1, productoSinPrecio));
    }

    private Pedido crearPedidoSinUsuario() {
        return Pedido.builder()
                .fecha(LocalDate.of(2026, 5, 10))
                .estado(Estado.PENDIENTE)
                .formaPago(FormaPago.EFECTIVO)
                .build();
    }

    private Producto crearProducto(String nombre, Double precio) {
        return Producto.builder()
                .nombre(nombre)
                .precio(precio)
                .build();
    }
}
