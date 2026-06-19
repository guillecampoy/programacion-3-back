package com.tp.jpa.repository;

import com.tp.jpa.model.Usuario;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends BaseRepository<Usuario> {
  public UsuarioRepository() {
    super(Usuario.class);
  }

  public Optional<Usuario> buscarPorMail(String mail) {
    if (mail == null || mail.isBlank()) {
      return Optional.empty();
    }

    EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
    try {
      // Consulta JPQL que busca el primer usuario activo cuyo mail coincide de forma parcial.
      TypedQuery<Usuario> query =
          entityManager.createQuery(
              "select u from Usuario u "
                  + "where lower(u.mail) like lower(:mail) and u.eliminado = false "
                  + "order by u.id",
              Usuario.class);
      query.setParameter("mail", "%" + mail.trim() + "%");
      query.setMaxResults(1);
      List<Usuario> resultados = query.getResultList();
      return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    } finally {
      entityManager.close();
    }
  }
}
