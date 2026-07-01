package com.tp.jpa.repository;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class PedidoRepository extends BaseRepository<Pedido> {
  /** Crea el repositorio de pedidos. */
  public PedidoRepository() {
    super(Pedido.class);
  }

  /**
   * Busca los pedidos activos asociados a un usuario.
   *
   * @param idUsuario identificador del usuario
   * @return pedidos activos del usuario
   */
  public List<Pedido> buscarPorUsuario(Long idUsuario) {
    try (EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager()) {
      // Consulta JPQL que obtiene los pedidos activos asociados al usuario indicado.
      TypedQuery<Pedido> query =
          entityManager.createQuery(
              "select p from Pedido p " + "where p.usuario.id = :uid and p.eliminado = false",
              Pedido.class);
      query.setParameter("uid", idUsuario);
      return query.getResultList();
    }
  }

  /**
   * Busca los pedidos activos con un estado determinado.
   *
   * @param estado estado a filtrar
   * @return pedidos activos con el estado solicitado
   */
  public List<Pedido> buscarPorEstado(Estado estado) {
    try (EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager()) {
      // Consulta JPQL que obtiene los pedidos activos filtrados por el estado solicitado.
      TypedQuery<Pedido> query =
          entityManager.createQuery(
              "select p from Pedido p where p.estado = :estado and p.eliminado = false",
              Pedido.class);
      query.setParameter("estado", estado);
      return query.getResultList();
    }
  }
}
