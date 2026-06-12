package ar.edu.tup.programacion3.integration;

import ar.edu.tup.programacion3.entities.Categoria;
import ar.edu.tup.programacion3.entities.DetallePedido;
import ar.edu.tup.programacion3.entities.Pedido;
import ar.edu.tup.programacion3.entities.Producto;
import ar.edu.tup.programacion3.entities.Usuario;
import ar.edu.tup.programacion3.seed.DatosSemilla;
import ar.edu.tup.programacion3.seed.DatosSemillaFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaIntegrationTest {
    private EntityManagerFactory entityManagerFactory;
    private DatosSemilla datosSemilla;

    @BeforeAll
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("miUnidad");
        datosSemilla = DatosSemillaFactory.crear();

        ejecutarEnTransaccion(entityManager -> {
            limpiarBase(entityManager);
            datosSemilla.categorias().forEach(entityManager::persist);
            datosSemilla.usuarios().forEach(entityManager::persist);
        });
    }

    @AfterAll
    void tearDown() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    void vuelcaDatosSemillaALaBaseYValidaRelaciones() {
        assertEquals(2L, contar(Usuario.class));
        assertEquals(3L, contar(Categoria.class));
        assertEquals(11L, contar(Producto.class));
        assertEquals(3L, contar(Pedido.class));

        List<Pedido> pedidos = consultar(entityManager ->
                entityManager.createQuery(
                                "select distinct p from Pedido p " +
                                        "join fetch p.usuario " +
                                        "join fetch p.detallePedidos d " +
                                        "join fetch d.producto",
                                Pedido.class
                        )
                        .getResultList()
        );

        assertFalse(pedidos.isEmpty());
        pedidos.forEach(pedido -> {
            assertNotNull(pedido.getUsuario());
            assertTrue(pedido.getDetallePedidos().size() >= 2);
            pedido.getDetallePedidos().forEach(detallePedido -> {
                assertNotNull(detallePedido.getProducto());
                assertTrue(detallePedido.getSubtotal() > 0);
            });
        });
    }

    @Test
    @Order(2)
    void actualizaAlMenosDosProductos() {
        ejecutarEnTransaccion(entityManager -> {
            Producto cafe = entityManager.find(Producto.class, 1L);
            Producto yerba = entityManager.find(Producto.class, 2L);

            cafe.setPrecio(cafe.getPrecio() + 100.0);
            cafe.setStock(cafe.getStock() - 1);
            yerba.setPrecio(yerba.getPrecio() + 200.0);
            yerba.setStock(yerba.getStock() - 2);
        });

        Producto cafeActualizado = buscar(Producto.class, 1L);
        Producto yerbaActualizada = buscar(Producto.class, 2L);

        assertEquals(3300.0, cafeActualizado.getPrecio());
        assertEquals(19, cafeActualizado.getStock());
        assertEquals(3000.0, yerbaActualizada.getPrecio());
        assertEquals(46, yerbaActualizada.getStock());
    }

    @Test
    @Order(3)
    void buscaUsuarioPorId() {
        Usuario usuario = buscar(Usuario.class, 48L);

        assertNotNull(usuario);
        assertEquals("Ana", usuario.getNombre());
        assertEquals("anagarcia@gmail.com", usuario.getMail());
    }

    @Test
    @Order(4)
    void buscaUsuarioPorMail() {
        Usuario usuario = consultar(entityManager ->
                entityManager.createQuery(
                                "select u from Usuario u where u.mail = :mail",
                                Usuario.class
                        )
                        .setParameter("mail", "bjuarez90@gmail.com")
                        .getSingleResult()
        );

        assertEquals(50L, usuario.getId());
        assertEquals("Bruno", usuario.getNombre());
    }

    @Test
    @Order(5)
    void borraUnProducto() {
        ejecutarEnTransaccion(entityManager -> {
            Producto producto = entityManager.find(Producto.class, 11L);
            assertNotNull(producto);
            entityManager.remove(producto);
        });

        assertEquals(10L, contar(Producto.class));
        boolean productoBorrado = consultar(entityManager ->
                entityManager.createQuery(
                                "select p from Producto p where p.id = :id",
                                Producto.class
                        )
                        .setParameter("id", 11L)
                        .getResultList()
                        .isEmpty()
        );
        assertTrue(productoBorrado);
    }

    private void limpiarBase(EntityManager entityManager) {
        entityManager.createQuery("delete from DetallePedido").executeUpdate();
        entityManager.createQuery("delete from Pedido").executeUpdate();
        entityManager.createQuery("delete from Producto").executeUpdate();
        entityManager.createQuery("delete from Categoria").executeUpdate();
        entityManager.createQuery("delete from Usuario").executeUpdate();
    }

    private <T> T buscar(Class<T> entityClass, Long id) {
        return consultar(entityManager -> entityManager.find(entityClass, id));
    }

    private long contar(Class<?> entityClass) {
        return consultar(entityManager ->
                entityManager.createQuery(
                                "select count(e) from " + entityClass.getSimpleName() + " e",
                                Long.class
                        )
                        .getSingleResult()
        );
    }

    private void ejecutarEnTransaccion(Consumer<EntityManager> consumer) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            consumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private <T> T consultar(Function<EntityManager, T> function) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return function.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }
}
