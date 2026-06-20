package com.tp.jpa.repository;

import com.tp.jpa.model.Base;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class BaseRepository<T> {
  private final Class<T> entityClass;

  protected BaseRepository(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  public T guardar(T entity) {
    EntityManager entityManager = crearEntityManager();
    try {
      Objects.requireNonNull(entity, "La entidad no puede ser nula.");
      entityManager.getTransaction().begin();
      T persistido;
      if (tieneId(entity)) {
        persistido = entityManager.merge(entity);
      } else {
        entityManager.persist(entity);
        persistido = entity;
      }
      entityManager.getTransaction().commit();
      return persistido;
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
  public boolean eliminarLogico(Long id) {
    EntityManager entityManager = crearEntityManager();
    try {
      entityManager.getTransaction().begin();
      T entity = entityManager.find(entityClass, id);
      if (entity == null) {
        entityManager.getTransaction().commit();
        return false;
      }
      marcarEliminado(entity, true);
      entityManager.merge(entity);
      entityManager.getTransaction().commit();
      return true;
    } catch (RuntimeException exception) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  public T cambiarEstadoEliminado(Long id, boolean eliminado) {
    if (eliminado) {
      eliminarLogico(id);
    } else {
      restaurarLogico(id);
    }
    return buscarPorId(id).orElse(null);
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

  private boolean tieneId(T entity) {
    if (!(entity instanceof Base base)) {
      throw new IllegalArgumentException("La entidad no hereda de Base: " + entityClass.getName());
    }
    return base.getId() != null;
  }

  private boolean restaurarLogico(Long id) {
    EntityManager entityManager = crearEntityManager();
    try {
      entityManager.getTransaction().begin();
      T entity = entityManager.find(entityClass, id);
      if (entity == null) {
        entityManager.getTransaction().commit();
        return false;
      }
      marcarEliminado(entity, false);
      entityManager.merge(entity);
      entityManager.getTransaction().commit();
      return true;
    } catch (RuntimeException exception) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
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
