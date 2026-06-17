package com.tp.jpa.repository;

import com.tp.jpa.model.Base;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T> {
  private final Class<T> entityClass;

  protected BaseRepository(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  public T guardar(T entity) {
    EntityManager entityManager = crearEntityManager();
    try {
      entityManager.getTransaction().begin();
      T merged = entityManager.merge(entity);
      entityManager.getTransaction().commit();
      return merged;
    } catch (RuntimeException exception) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  public Optional<T> buscarPorId(Long id) {
    try (EntityManager entityManager = crearEntityManager()) {
      return Optional.ofNullable(entityManager.find(entityClass, id));
    }
  }

  public List<T> listarActivos() {
    try (EntityManager entityManager = crearEntityManager()) {
      return entityManager
          .createQuery(
              "select e from " + entityName() + " e where e.eliminado = false", entityClass)
          .getResultList();
    }
  }

  public List<T> listarEliminados() {
    try (EntityManager entityManager = crearEntityManager()) {
      return entityManager
          .createQuery("select e from " + entityName() + " e where e.eliminado = true", entityClass)
          .getResultList();
    }
  }

  /**
   * Se utiliza un único método para hacer el borrado lógico y revertir el mismo Como no se cuenta
   * con lógica especifica para las especializaciones concretas En Categoría y Producto, se deja en
   * esta clase.
   *
   * @param id se corresponde con el ID de la entidad que se desea modificar estado
   * @param eliminado modificador que indica si se borra o restaura
   * @return
   */
  public T cambiarEstadoEliminado(Long id, boolean eliminado) {
    EntityManager entityManager = crearEntityManager();
    try {
      entityManager.getTransaction().begin();
      T entity = entityManager.find(entityClass, id);
      if (entity == null) {
        entityManager.getTransaction().commit();
        return null;
      }
      marcarEliminado(entity, eliminado);
      T merged = entityManager.merge(entity);
      entityManager.getTransaction().commit();
      return merged;
    } catch (RuntimeException exception) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  public long siguienteId() {
    try (EntityManager entityManager = crearEntityManager()) {
      Long maxId =
          entityManager
              .createQuery("select coalesce(max(e.id), 0) from " + entityName() + " e", Long.class)
              .getSingleResult();
      return maxId + 1;
    }
  }

  private void marcarEliminado(T entity, boolean eliminado) {
    if (!(entity instanceof Base base)) {
      throw new IllegalArgumentException("La entidad no hereda de Base: " + entityClass.getName());
    }
    base.setEliminado(eliminado);
  }

  private EntityManager crearEntityManager() {
    return JPAUtil.getEntityManagerFactory().createEntityManager();
  }

  private String entityName() {
    Entity entity = entityClass.getAnnotation(Entity.class);
    if (entity != null && !entity.name().isBlank()) {
      return entity.name();
    }
    return entityClass.getSimpleName();
  }
}
