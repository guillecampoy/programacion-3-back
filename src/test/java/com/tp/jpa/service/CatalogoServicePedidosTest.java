package com.tp.jpa.service;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogoServicePedidosTest {
  private final CategoriaRepository categoriaRepository = new CategoriaRepository();
  private final ProductoRepository productoRepository = new ProductoRepository();
  private final UsuarioRepository usuarioRepository = new UsuarioRepository();
  private final PedidoRepository pedidoRepository = new PedidoRepository();

  @BeforeEach
  void cleanDatabase() {
    EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
    try {
      em.getTransaction().begin();
      em.createQuery("delete from DetallePedido").executeUpdate();
      em.createQuery("delete from Pedido").executeUpdate();
      em.createQuery("delete from Producto").executeUpdate();
      em.createQuery("delete from Categoria").executeUpdate();
      em.createQuery("delete from Usuario").executeUpdate();
      em.createNativeQuery("alter table Categoria alter column id restart with 1").executeUpdate();
      em.createNativeQuery("alter table Producto alter column id restart with 1").executeUpdate();
      em.createNativeQuery("alter table Usuario alter column id restart with 1").executeUpdate();
      em.createNativeQuery("alter table Pedido alter column id restart with 1").executeUpdate();
      em.getTransaction().commit();
    } catch (RuntimeException exception) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw exception;
    } finally {
      em.close();
    }
  }

  @Test
  void crearPedidoPersisteDetalleTotalYReduceStock() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario usuario = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));
    Producto te = productoRepository.guardar(crearProducto(categoria, "Te", 7.0, 7, true));

    Pedido pedido =
        service.crearPedido(
            usuario.getId(),
            FormaPago.EFECTIVO,
            List.of(
                new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 2),
                new CatalogoService.LineaPedidoSolicitud(te.getId(), 1)));

    assertNotNull(pedido.getId());
    assertEquals(Estado.PENDIENTE, pedido.getEstado());
    assertEquals(FormaPago.EFECTIVO, pedido.getFormaPago());
    assertEquals(usuario.getId(), pedido.getUsuario().getId());
    assertEquals(2, pedido.getDetallePedidos().size());
    assertEquals(27.0, pedido.getTotal());
    assertEquals(6, productoRepository.buscarPorId(te.getId()).orElseThrow().getStock());
    assertEquals(8, productoRepository.buscarPorId(cafe.getId()).orElseThrow().getStock());

    Pedido persistido = pedidoRepository.buscarPorId(pedido.getId()).orElseThrow();
    assertEquals(pedido.getId(), persistido.getId());
    assertEquals(27.0, persistido.getTotal());
  }

  @Test
  void crearPedidoRevoqueTodoSiFallaUnaValidacion() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario usuario = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));
    productoRepository.guardar(crearProducto(categoria, "Te", 7.0, 7, true));

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () ->
                service.crearPedido(
                    usuario.getId(),
                    FormaPago.TARJETA,
                    List.of(
                        new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1),
                        new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 20))));

    assertTrue(exception.getMessage().contains("stock insuficiente"));
    assertTrue(pedidoRepository.buscarPorUsuario(usuario.getId()).isEmpty());
    assertEquals(10, productoRepository.buscarPorId(cafe.getId()).orElseThrow().getStock());
  }

  @Test
  void cambiarEstadoPedidoPersisteElNuevoEstado() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario usuario = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));

    Pedido pedido =
        service.crearPedido(
            usuario.getId(),
            FormaPago.EFECTIVO,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));

    Pedido actualizado = service.cambiarEstadoPedido(pedido.getId(), Estado.CONFIRMADO);

    assertEquals(Estado.CONFIRMADO, actualizado.getEstado());
    assertEquals(1, actualizado.getDetallePedidos().size());
    assertEquals(9, productoRepository.buscarPorId(cafe.getId()).orElseThrow().getStock());
    assertEquals(
        Estado.CONFIRMADO, pedidoRepository.buscarPorId(pedido.getId()).orElseThrow().getEstado());
  }

  @Test
  void cambiarEstadoPedidoRechazaPedidoEliminado() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario usuario = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));

    Pedido pedido =
        service.crearPedido(
            usuario.getId(),
            FormaPago.EFECTIVO,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    pedidoRepository.eliminarLogico(pedido.getId());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.cambiarEstadoPedido(pedido.getId(), Estado.TERMINADO));

    assertEquals("Error: no existe un pedido activo con el ID indicado.", exception.getMessage());
  }

  @Test
  void cambiarEstadoPedidoRechazaPedidoInexistente() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.cambiarEstadoPedido(99L, Estado.CANCELADO));

    assertEquals("Error: no existe un pedido activo con el ID indicado.", exception.getMessage());
  }

  private Usuario crearUsuario(String mail) {
    Usuario usuario = new Usuario();
    usuario.setNombre("Ana");
    usuario.setApellido("Garcia");
    usuario.setMail(mail);
    usuario.setCelular("123456");
    usuario.setContrasenia("Clave123");
    usuario.setRol(Rol.USUARIO);
    usuario.setEliminado(false);
    usuario.setCreatedAt(LocalDateTime.now());
    return usuario;
  }

  private Categoria crearCategoria(String nombre) {
    Categoria categoria = new Categoria();
    categoria.setNombre(nombre);
    categoria.setDescripcion("Descripcion " + nombre);
    categoria.setEliminado(false);
    categoria.setCreatedAt(LocalDateTime.now());
    return categoria;
  }

  private Producto crearProducto(
      Categoria categoria, String nombre, double precio, int stock, boolean disponible) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    producto.setDescripcion("Descripcion " + nombre);
    producto.setPrecio(precio);
    producto.setStock(stock);
    producto.setImagen(nombre + ".png");
    producto.setDisponible(disponible);
    producto.setCategoria(categoria);
    producto.setEliminado(false);
    producto.setCreatedAt(LocalDateTime.now());
    return producto;
  }
}
