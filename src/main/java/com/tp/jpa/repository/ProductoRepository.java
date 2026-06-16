package com.tp.jpa.repository;

import ar.edu.tup.programacion3.entities.Producto;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ProductoRepository extends BaseRepository<Producto> {
    public ProductoRepository() {
        super(Producto.class);
    }

    public List<Producto> buscarPorCategoria(Long categoriaId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Consulta JPQL que obtiene los productos activos asociados a la categoria indicada por su ID.
            TypedQuery<Producto> query = entityManager.createQuery(
                    "select p from Producto p where p.categoria.id = :categoriaId and p.eliminado = false",
                    Producto.class
            );
            query.setParameter("categoriaId", categoriaId);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }
}
