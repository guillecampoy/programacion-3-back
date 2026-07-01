package com.tp.jpa.repository;

import com.tp.jpa.model.Producto;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ProductoRepository extends BaseRepository<Producto> {
  /** Crea el repositorio de productos. */
  public ProductoRepository() {
    super(Producto.class);
  }

  /**
   * Busca los productos activos de una categoria.
   *
   * @param categoriaId identificador de la categoria
   * @return productos activos asociados a la categoria
   */
  public List<Producto> buscarPorCategoria(Long categoriaId) {
    try (EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager()) {
      // Consulta JPQL que obtiene los productos activos asociados a la categoria indicada.
      TypedQuery<Producto> query =
          entityManager.createQuery(
              "select p from Producto p where p.categoria.id = :catId and p.eliminado = false",
              Producto.class);
      query.setParameter("catId", categoriaId);
      return query.getResultList();
    }
  }
}
