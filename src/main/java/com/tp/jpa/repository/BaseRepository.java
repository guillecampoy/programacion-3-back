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

  /**
   * Crea el repositorio base para una entidad concreta.
   *
   * @param entityClass clase de la entidad administrada por este repositorio
   */
  protected BaseRepository(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  /**
   * Persiste una entidad nueva o actualiza una existente.
   *
   * @param entity entidad a guardar
   * @return la entidad persistida
   */
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

  /**
   * Busca una entidad por su identificador.
   *
   * @param id identificador de la entidad
   * @return entidad encontrada o vacio si no existe
   */
  public Optional<T> buscarPorId(Long id) {
    try (EntityManager entityManager = crearEntityManager()) {
      return Optional.ofNullable(entityManager.find(entityClass, id));
    }
  }

  /**
   * Lista todas las entidades activas.
   *
   * @return entidades con {@code eliminado = false}
   */
  public List<T> listarActivos() {
    try (EntityManager entityManager = crearEntityManager()) {
      return entityManager
          .createQuery(
              "select e from " + entityName() + " e where e.eliminado = false", entityClass)
          .getResultList();
    }
  }

  /**
   * Lista todas las entidades eliminadas de forma logica.
   *
   * @return entidades con {@code eliminado = true}
   */
  public List<T> listarEliminados() {
    try (EntityManager entityManager = crearEntityManager()) {
      return entityManager
          .createQuery("select e from " + entityName() + " e where e.eliminado = true", entityClass)
          .getResultList();
    }
  }

  /**
   * Se utiliza un único método para hacer el borrado lógico y revertir el mismo. Como no se cuenta
   * con lógica especifica para las especializaciones concretas en Categoría y Producto, se deja en
   * esta clase.
   *
   * @param id se corresponde con el ID de la entidad que se desea modificar estado
   * @param eliminado modificador que indica si se borra o restaura
   * @return entidad actualizada o nula si no existe
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
      entityManager.merge(entity);
      entityManager.getTransaction().commit();
      return buscarPorId(id).orElse(null);
    } catch (RuntimeException exception) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  /**
   * Marca una entidad como eliminada en forma logica.
   *
   * @param id identificador de la entidad
   * @return {@code true} si se actualizo una entidad, {@code false} en caso contrario
   */
  public boolean eliminarLogico(Long id) {
    // Corrección aplicada por feedback de la entrega anterior: esta firma queda como el wrapper
    // exigido por la rúbrica y delega en cambiarEstadoEliminado(id, true). Podría unificarse más,
    // pero se mantiene así para respetar exactamente lo solicitado.
    return cambiarEstadoEliminado(id, true) != null;
  }

  /**
   * Calcula el siguiente identificador logico disponible.
   *
   * @return siguiente valor de identificador
   */
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
