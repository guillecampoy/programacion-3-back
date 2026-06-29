package com.tp.jpa.repository;

import com.tp.jpa.model.Usuario;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends BaseRepository<Usuario> {
  /** Crea el repositorio de usuarios. */
  public UsuarioRepository() {
    super(Usuario.class);
  }

  /**
   * Busca el usuario activo cuyo mail coincide de forma exacta.
   *
   * @param mail texto a buscar dentro del mail
   * @return usuario encontrado o vacio si no hay coincidencias validas
   */
  public Optional<Usuario> buscarPorMail(String mail) {
    if (mail == null || mail.isBlank()) {
      return Optional.empty();
    }

    EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
    try {
      // Se cumple la rúbrica con coincidencia exacta; una búsqueda parcial puede evaluarse como
      // extensión, pero no forma parte del contrato de entrega.
      TypedQuery<Usuario> query =
          entityManager.createQuery(
              "select u from Usuario u where lower(u.mail) = lower(:mail) and u.eliminado = false",
              Usuario.class);
      query.setParameter("mail", mail.trim());
      List<Usuario> resultados = query.getResultList();
      return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    } finally {
      entityManager.close();
    }
  }
}
