package com.tp.jpa.repository;

import com.tp.jpa.entities.Base;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T> {
    private final Class<T> entityClass;
    private final EntityManagerFactory entityManagerFactory;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.entityManagerFactory = JPAUtil.getEntityManagerFactory();
    }

    public T guardar(T entity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(entityClass, id));
        } finally {
            entityManager.close();
        }
    }

    public List<T> listarActivos() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return entityManager.createQuery(
                            "select e from " + entityName() + " e where e.eliminado = false",
                            entityClass
                    )
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public boolean eliminarLogico(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            T entity = entityManager.find(entityClass, id);
            if (entity == null) {
                entityManager.getTransaction().commit();
                return false;
            }
            marcarEliminado(entity);
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

    public long siguienteId() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Long maxId = entityManager.createQuery(
                            "select coalesce(max(e.id), 0) from " + entityName() + " e",
                            Long.class
                    )
                    .getSingleResult();
            return maxId + 1;
        } finally {
            entityManager.close();
        }
    }

    private void marcarEliminado(T entity) {
        if (!(entity instanceof Base base)) {
            throw new IllegalArgumentException("La entidad no hereda de Base: " + entityClass.getName());
        }
        base.setEliminado(true);
    }

    private String entityName() {
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity != null && !entity.name().isBlank()) {
            return entity.name();
        }
        return entityClass.getSimpleName();
    }
}
