package com.tp.jpa.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PedidoRepositoryTest {

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

  private Usuario crearUsuario(String mail) {
    Usuario usuario = new Usuario();
    usuario.setNombre("Ana");
    usuario.setApellido("Garcia");
    usuario.setMail(mail);
    usuario.setCelular("123-456-7890");
    usuario.setContrasenia("Clave123");
    usuario.setRol(Rol.USUARIO);
    usuario.setEliminado(false);
    usuario.setCreatedAt(LocalDateTime.now());
    return usuarioRepository.guardar(usuario);
  }

  private Pedido crearPedido(Usuario usuario, Estado estado) {
    Pedido pedido = new Pedido();
    pedido.setFecha(LocalDate.of(2026, 6, 19));
    pedido.setEstado(estado);
    pedido.setFormaPago(FormaPago.EFECTIVO);
    pedido.setTotal(0.0);
    pedido.setUsuario(usuario);
    pedido.setEliminado(false);
    pedido.setCreatedAt(LocalDateTime.now());
    return pedidoRepository.guardar(pedido);
  }

  @Test
  void testBuscarPorUsuario() {
    Usuario ana = crearUsuario("anagarcia@gmail.com");
    Usuario bruno = crearUsuario("bjuarez90@gmail.com");
    Pedido pedidoAna1 = crearPedido(ana, Estado.CONFIRMADO);
    Pedido pedidoAna2 = crearPedido(ana, Estado.PENDIENTE);
    crearPedido(bruno, Estado.TERMINADO);

    List<Pedido> pedidosAna = pedidoRepository.buscarPorUsuario(ana.getId());

    assertEquals(2, pedidosAna.size());
    assertTrue(
        pedidosAna.stream().allMatch(pedido -> pedido.getUsuario().getId().equals(ana.getId())));
    assertTrue(pedidosAna.stream().anyMatch(pedido -> pedido.getId().equals(pedidoAna1.getId())));
    assertTrue(pedidosAna.stream().anyMatch(pedido -> pedido.getId().equals(pedidoAna2.getId())));
  }

  @Test
  void testBuscarPorUsuarioExcluyeEliminados() {
    Usuario ana = crearUsuario("anagarcia@gmail.com");
    Pedido pedido = crearPedido(ana, Estado.CONFIRMADO);
    pedidoRepository.eliminarLogico(pedido.getId());

    assertTrue(pedidoRepository.buscarPorUsuario(ana.getId()).isEmpty());
  }

  @Test
  void testBuscarPorEstado() {
    Usuario ana = crearUsuario("anagarcia@gmail.com");
    Usuario bruno = crearUsuario("bjuarez90@gmail.com");
    Pedido pedido1 = crearPedido(ana, Estado.CONFIRMADO);
    Pedido pedido2 = crearPedido(bruno, Estado.CONFIRMADO);
    crearPedido(ana, Estado.PENDIENTE);

    List<Pedido> confirmados = pedidoRepository.buscarPorEstado(Estado.CONFIRMADO);

    assertEquals(2, confirmados.size());
    assertTrue(confirmados.stream().allMatch(pedido -> pedido.getEstado() == Estado.CONFIRMADO));
    assertTrue(confirmados.stream().anyMatch(pedido -> pedido.getId().equals(pedido1.getId())));
    assertTrue(confirmados.stream().anyMatch(pedido -> pedido.getId().equals(pedido2.getId())));
  }

  @Test
  void testBuscarPorEstadoSinResultados() {
    crearUsuario("anagarcia@gmail.com");

    assertTrue(pedidoRepository.buscarPorEstado(Estado.CANCELADO).isEmpty());
  }

  @Test
  void testBuscarPorEstadoExcluyeEliminados() {
    Usuario ana = crearUsuario("anagarcia@gmail.com");
    Pedido pedido = crearPedido(ana, Estado.TERMINADO);
    pedidoRepository.eliminarLogico(pedido.getId());

    assertTrue(pedidoRepository.buscarPorEstado(Estado.TERMINADO).isEmpty());
  }
}
