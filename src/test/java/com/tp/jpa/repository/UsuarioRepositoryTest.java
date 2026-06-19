package com.tp.jpa.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsuarioRepositoryTest {

  private final UsuarioRepository repository = new UsuarioRepository();

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

  private Usuario crearUsuario(String nombre, String mail) {
    Usuario usuario = new Usuario();
    usuario.setNombre(nombre);
    usuario.setApellido("Apellido " + nombre);
    usuario.setMail(mail);
    usuario.setCelular("123-456-7890");
    usuario.setContrasenia("Clave123");
    usuario.setRol(Rol.USUARIO);
    usuario.setEliminado(false);
    usuario.setCreatedAt(LocalDateTime.now());
    return usuario;
  }

  @Test
  void testBuscarPorMailExacto() {
    Usuario guardado = repository.guardar(crearUsuario("Ana", "anagarcia@gmail.com"));

    Optional<Usuario> resultado = repository.buscarPorMail("anagarcia@gmail.com");

    assertTrue(resultado.isPresent());
    assertEquals(guardado.getId(), resultado.orElseThrow().getId());
  }

  @Test
  void testBuscarPorMailParcial() {
    Usuario guardado = repository.guardar(crearUsuario("Ana", "anagarcia@gmail.com"));
    repository.guardar(crearUsuario("Bruno", "bjuarez90@gmail.com"));

    Optional<Usuario> resultado = repository.buscarPorMail("anagarcia");

    assertTrue(resultado.isPresent());
    assertEquals(guardado.getId(), resultado.orElseThrow().getId());
  }

  @Test
  void testBuscarPorMailNoExisteDevuelveVacio() {
    repository.guardar(crearUsuario("Ana", "anagarcia@gmail.com"));

    assertTrue(repository.buscarPorMail("noexiste").isEmpty());
    assertTrue(repository.buscarPorMail("").isEmpty());
    assertTrue(repository.buscarPorMail(null).isEmpty());
  }

  @Test
  void testBuscarPorMailExcluyeEliminados() {
    Usuario guardado = repository.guardar(crearUsuario("Ana", "anagarcia@gmail.com"));
    repository.eliminarLogico(guardado.getId());

    assertTrue(repository.buscarPorMail("anagarcia").isEmpty());
  }
}
