package com.tp.jpa.integration;

import com.tp.jpa.model.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdGenerationCompatibilityTest {

    @Test
    void guardaCategoriaConTablaViejaSinIdentityEnId() throws SQLException {
        String jdbcUrl = "jdbc:h2:mem:old_schema_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        crearTablaCategoriaSinIdentity(jdbcUrl);

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(
                "miUnidad",
                Map.of(
                        "jakarta.persistence.jdbc.url", jdbcUrl,
                        "hibernate.hbm2ddl.auto", "update",
                        "hibernate.show_sql", "false",
                        "hibernate.format_sql", "false"
                )
        );
        try {
            Categoria categoria = new Categoria();
            categoria.setNombre("Bebidas");
            categoria.setDescripcion("Bebidas varias");
            categoria.setEliminado(false);
            categoria.setCreatedAt(LocalDateTime.now());

            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(categoria);
                entityManager.getTransaction().commit();
            } catch (RuntimeException exception) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw exception;
            } finally {
                entityManager.close();
            }

            assertNotNull(categoria.getId());
            assertTrue(categoria.getId() > 0);
        } finally {
            entityManagerFactory.close();
        }
    }

    private void crearTablaCategoriaSinIdentity(String jdbcUrl) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    create table Categoria (
                        id bigint not null,
                        createdAt timestamp(6),
                        eliminado boolean,
                        descripcion varchar(255),
                        nombre varchar(255),
                        primary key (id)
                    )
                    """);
        }
    }
}
