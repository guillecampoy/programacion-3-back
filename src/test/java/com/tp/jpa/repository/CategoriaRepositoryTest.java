package com.tp.jpa.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoriaRepositoryTest {

  private final CategoriaRepository repository = new CategoriaRepository();

  @BeforeEach
  void cleanDatabase() {
    EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
    try {
      em.getTransaction().begin();
      em.createQuery("delete from DetallePedido").executeUpdate();
      em.createQuery("delete from Pedido").executeUpdate();
      em.createQuery("delete from Producto").executeUpdate();
      em.createQuery("delete from Categoria").executeUpdate();
      em.createNativeQuery("alter table Categoria alter column id restart with 1").executeUpdate();
      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }
  }

  private Categoria crearCategoria(String nombre) {
    Categoria c = new Categoria();
    c.setNombre(nombre);
    c.setDescripcion("Desc " + nombre);
    c.setEliminado(false);
    c.setCreatedAt(LocalDateTime.now());
    return c;
  }

  @Test
  void testGuardar() {
    Categoria c = crearCategoria("Bebidas");
    Categoria guardada = repository.guardar(c);
    assertNotNull(guardada);
    assertNotNull(guardada.getId());
  }

  @Test
  void testBuscarPorId() {
    Categoria c = crearCategoria("Snacks");
    Categoria guardada = repository.guardar(c);
    Optional<Categoria> encontrada = repository.buscarPorId(guardada.getId());
    assertTrue(encontrada.isPresent());
    assertEquals("Snacks", encontrada.get().getNombre());
  }

  @Test
  void testBuscarPorIdNotFound() {
    Optional<Categoria> resultado = repository.buscarPorId(-1L);
    assertFalse(resultado.isPresent());
  }

  @Test
  void testListarActivos() {
    repository.guardar(crearCategoria("A"));
    repository.guardar(crearCategoria("B"));
    List<Categoria> activas = repository.listarActivos();
    assertEquals(2, activas.size());
  }

  @Test
  void testListarActivosExcludesDeleted() {
    Categoria c = crearCategoria("ParaBorrar");
    Categoria guardada = repository.guardar(c);
    repository.cambiarEstadoEliminado(guardada.getId(), true);
    List<Categoria> activas = repository.listarActivos();
    assertTrue(activas.stream().noneMatch(cat -> cat.getId().equals(guardada.getId())));
  }

  @Test
  void testEliminarLogico() {
    Categoria c = crearCategoria("ParaBorrar");
    Categoria guardada = repository.guardar(c);
    assertTrue(repository.eliminarLogico(guardada.getId()));
    Categoria recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();
    assertTrue(recuperada.getEliminado());
  }

  @Test
  void testEliminarLogicoNotFound() {
    assertFalse(repository.eliminarLogico(-1L));
  }

  @Test
  void testSiguienteId() {
    assertEquals(1L, repository.siguienteId());
    Categoria c = crearCategoria("Test");
    repository.guardar(c);
    assertEquals(2L, repository.siguienteId());
  }

  @Test
  void testGuardarYRecuperarMantieneDatos() {
    Categoria c = crearCategoria("Lacteos");
    Categoria guardada = repository.guardar(c);
    Categoria recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();
    assertEquals("Lacteos", recuperada.getNombre());
    assertEquals("Desc Lacteos", recuperada.getDescripcion());
    assertFalse(recuperada.getEliminado());
    assertNotNull(recuperada.getCreatedAt());
  }

  @Test
  void testGuardarConIdExistenteUsaMerge() {
    Categoria guardada = repository.guardar(crearCategoria("Original"));
    Categoria copiaDesacoplada = new Categoria();
    copiaDesacoplada.setId(guardada.getId());
    copiaDesacoplada.setNombre("Actualizada");
    copiaDesacoplada.setDescripcion("Desc Actualizada");
    copiaDesacoplada.setEliminado(false);
    copiaDesacoplada.setCreatedAt(guardada.getCreatedAt());

    Categoria persistida = repository.guardar(copiaDesacoplada);
    assertEquals(guardada.getId(), persistida.getId());

    Categoria recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();
    assertEquals("Actualizada", recuperada.getNombre());
    assertEquals("Desc Actualizada", recuperada.getDescripcion());
  }
}
