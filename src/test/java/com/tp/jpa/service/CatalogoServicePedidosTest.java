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

  @Test
  void listarPedidosActivosPorUsuarioDevuelveSoloActivosDelUsuario() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario ana = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Usuario bruno = usuarioRepository.guardar(crearUsuario("bruno@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));

    Pedido pedidoActivo =
        service.crearPedido(
            ana.getId(),
            FormaPago.EFECTIVO,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    Pedido pedidoEliminado =
        service.crearPedido(
            ana.getId(),
            FormaPago.TARJETA,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    service.crearPedido(
        bruno.getId(),
        FormaPago.TRANSFERENCIA,
        List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    pedidoRepository.eliminarLogico(pedidoEliminado.getId());

    List<Pedido> pedidos = service.listarPedidosActivosPorUsuario(ana.getId());

    assertEquals(1, pedidos.size());
    assertEquals(pedidoActivo.getId(), pedidos.get(0).getId());
    assertEquals(FormaPago.EFECTIVO, pedidos.get(0).getFormaPago());
  }

  @Test
  void listarPedidosActivosPorEstadoDevuelveSoloActivosDelEstado() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario ana = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Usuario bruno = usuarioRepository.guardar(crearUsuario("bruno@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));

    Pedido pendienteAna =
        service.crearPedido(
            ana.getId(),
            FormaPago.EFECTIVO,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    Pedido confirmadoBruno =
        service.crearPedido(
            bruno.getId(),
            FormaPago.TARJETA,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    service.cambiarEstadoPedido(confirmadoBruno.getId(), Estado.CONFIRMADO);
    Pedido pendienteEliminado =
        service.crearPedido(
            ana.getId(),
            FormaPago.TRANSFERENCIA,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 1)));
    pedidoRepository.eliminarLogico(pendienteEliminado.getId());

    List<Pedido> pedidos = service.listarPedidosActivosPorEstado(Estado.PENDIENTE);

    assertEquals(1, pedidos.size());
    assertEquals(pendienteAna.getId(), pedidos.get(0).getId());
    assertEquals(Estado.PENDIENTE, pedidos.get(0).getEstado());
  }

  @Test
  void totalFacturadoTerminadosSumaSoloTerminadosActivosYTrataNullComoCero() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario usuario = usuarioRepository.guardar(crearUsuario("ana@example.com"));

    Pedido terminadoConTotal = new Pedido();
    terminadoConTotal.setFecha(java.time.LocalDate.of(2026, 6, 20));
    terminadoConTotal.setEstado(Estado.TERMINADO);
    terminadoConTotal.setFormaPago(FormaPago.EFECTIVO);
    terminadoConTotal.setTotal(12500.0);
    terminadoConTotal.setUsuario(usuario);
    terminadoConTotal.setEliminado(false);
    terminadoConTotal.setCreatedAt(LocalDateTime.now());
    pedidoRepository.guardar(terminadoConTotal);

    Pedido terminadoSinTotal = new Pedido();
    terminadoSinTotal.setFecha(java.time.LocalDate.of(2026, 6, 21));
    terminadoSinTotal.setEstado(Estado.TERMINADO);
    terminadoSinTotal.setFormaPago(FormaPago.TARJETA);
    terminadoSinTotal.setTotal(0.0);
    terminadoSinTotal.setUsuario(usuario);
    terminadoSinTotal.setEliminado(false);
    terminadoSinTotal.setCreatedAt(LocalDateTime.now());
    pedidoRepository.guardar(terminadoSinTotal);

    try (EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager()) {
      em.getTransaction().begin();
      em.createNativeQuery("update Pedido set total = null where id = ?")
          .setParameter(1, terminadoSinTotal.getId())
          .executeUpdate();
      em.getTransaction().commit();
    }

    Pedido pendiente = new Pedido();
    pendiente.setFecha(java.time.LocalDate.of(2026, 6, 22));
    pendiente.setEstado(Estado.PENDIENTE);
    pendiente.setFormaPago(FormaPago.TRANSFERENCIA);
    pendiente.setTotal(9000.0);
    pendiente.setUsuario(usuario);
    pendiente.setEliminado(false);
    pendiente.setCreatedAt(LocalDateTime.now());
    pedidoRepository.guardar(pendiente);

    Pedido terminadoEliminado = new Pedido();
    terminadoEliminado.setFecha(java.time.LocalDate.of(2026, 6, 23));
    terminadoEliminado.setEstado(Estado.TERMINADO);
    terminadoEliminado.setFormaPago(FormaPago.EFECTIVO);
    terminadoEliminado.setTotal(9999.0);
    terminadoEliminado.setUsuario(usuario);
    terminadoEliminado.setEliminado(false);
    terminadoEliminado.setCreatedAt(LocalDateTime.now());
    pedidoRepository.guardar(terminadoEliminado);
    pedidoRepository.eliminarLogico(terminadoEliminado.getId());

    assertEquals(12500.0, service.totalFacturadoTerminados());
  }

  @Test
  void bajaPedidoMarcaEliminadoYConservaStockYDetalles() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);
    Usuario usuario = usuarioRepository.guardar(crearUsuario("ana@example.com"));
    Categoria categoria = categoriaRepository.guardar(crearCategoria("Bebidas"));
    Producto cafe = productoRepository.guardar(crearProducto(categoria, "Cafe", 10.0, 10, true));

    Pedido pedido =
        service.crearPedido(
            usuario.getId(),
            FormaPago.EFECTIVO,
            List.of(new CatalogoService.LineaPedidoSolicitud(cafe.getId(), 2)));

    service.bajaPedido(pedido.getId());

    assertTrue(
        Boolean.TRUE.equals(
            pedidoRepository.buscarPorId(pedido.getId()).orElseThrow().getEliminado()));
    assertEquals(8, productoRepository.buscarPorId(cafe.getId()).orElseThrow().getStock());
    assertTrue(pedidoRepository.buscarPorUsuario(usuario.getId()).isEmpty());
    try (EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager()) {
      Number cantidadDetalles =
          (Number)
              em.createNativeQuery("select count(*) from DetallePedido where pedido_id = ?")
                  .setParameter(1, pedido.getId())
                  .getSingleResult();
      assertEquals(1L, cantidadDetalles.longValue());
    }
  }

  @Test
  void bajaPedidoRechazaPedidoInexistente() {
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.bajaPedido(99L));

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
