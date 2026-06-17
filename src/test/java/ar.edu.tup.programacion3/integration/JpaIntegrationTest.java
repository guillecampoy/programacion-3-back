package ar.edu.tup.programacion3.integration;

import com.tp.jpa.entities.Categoria;
import com.tp.jpa.entities.Pedido;
import com.tp.jpa.entities.Producto;
import com.tp.jpa.entities.Usuario;
import com.tp.jpa.seed.DatosSemilla;
import com.tp.jpa.seed.DatosSemillaFactory;
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
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaIntegrationTest {
    private static final String TEST_JDBC_URL = "jdbc:h2:mem:jpa_integration_test;DB_CLOSE_DELAY=-1";

    private EntityManagerFactory entityManagerFactory;
    private DatosSemilla datosSemilla;

    @BeforeAll
    void setUp() {
        assertTrue(TEST_JDBC_URL.startsWith("jdbc:h2:mem:"));
        assertFalse(TEST_JDBC_URL.contains("./data/jpa_db"));

        entityManagerFactory = Persistence.createEntityManagerFactory(
                "miUnidad",
                Map.of("jakarta.persistence.jdbc.url", TEST_JDBC_URL)
        );
        datosSemilla = DatosSemillaFactory.crear();

        ejecutarEnTransaccion(entityManager -> {
            limpiarBase(entityManager);
            datosSemilla.categorias().forEach(entityManager::persist);
            datosSemilla.usuarios().forEach(entityManager::persist);
        });

        imprimirResultado("SETUP", "Base H2 lista, tablas limpiadas y datos semilla persistidos");
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
        assertEquals(10L, contar(Producto.class));
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

        imprimirResultado(
                "PERSISTENCIA",
                "usuarios=2, categorias=3, productos=10, pedidos=3, pedidosConDetalles>=2"
        );
    }

    @Test
    @Order(2)
    void actualizaAlMenosDosProductos() {
        Long cafeId = buscarProductoPorNombre("Cafe molido").getId();
        Long yerbaId = buscarProductoPorNombre("Yerba mate").getId();

        ejecutarEnTransaccion(entityManager -> {
            Producto cafe = entityManager.find(Producto.class, cafeId);
            Producto yerba = entityManager.find(Producto.class, yerbaId);

            cafe.setPrecio(cafe.getPrecio() + 100.0);
            cafe.setStock(cafe.getStock() - 1);
            yerba.setPrecio(yerba.getPrecio() + 200.0);
            yerba.setStock(yerba.getStock() - 2);
        });

        Producto cafeActualizado = buscar(Producto.class, cafeId);
        Producto yerbaActualizada = buscar(Producto.class, yerbaId);

        assertEquals(3300.0, cafeActualizado.getPrecio());
        assertEquals(19, cafeActualizado.getStock());
        assertEquals(3000.0, yerbaActualizada.getPrecio());
        assertEquals(46, yerbaActualizada.getStock());

        imprimirResultado(
                "UPDATE",
                "Cafe molido precio=3300.0 stock=19, Yerba mate precio=3000.0 stock=46"
        );
    }

    @Test
    @Order(3)
    void buscaUsuarioPorId() {
        Long usuarioId = buscarUsuarioPorMail("anagarcia@gmail.com").getId();
        Usuario usuario = buscar(Usuario.class, usuarioId);

        assertNotNull(usuario);
        assertEquals("Ana", usuario.getNombre());
        assertEquals("anagarcia@gmail.com", usuario.getMail());

        imprimirResultado("BUSQUEDA_ID", "id=" + usuarioId + ", nombre=Ana, mail=anagarcia@gmail.com");
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

        assertTrue(usuario.getId() > 0);
        assertEquals("Bruno", usuario.getNombre());

        imprimirResultado("BUSQUEDA_MAIL", "mail=bjuarez90@gmail.com, id=" + usuario.getId() + ", nombre=Bruno");
    }

    @Test
    @Order(5)
    void borraUnProductoLogicamente() {
        Long productoId = buscarProductoPorNombre("Esponja multiuso").getId();

        ejecutarEnTransaccion(entityManager -> {
            Producto producto = entityManager.find(Producto.class, productoId);
            assertNotNull(producto);
            producto.setEliminado(true);
        });

        assertEquals(10L, contar(Producto.class));
        Producto productoBorrado = buscar(Producto.class, productoId);
        boolean productoAusenteDeActivos = consultar(entityManager ->
                entityManager.createQuery(
                                "select p from Producto p where p.id = :id and p.eliminado = false",
                                Producto.class
                        )
                        .setParameter("id", productoId)
                        .getResultList()
                        .isEmpty()
        );
        assertTrue(productoBorrado.getEliminado());
        assertTrue(productoAusenteDeActivos);

        imprimirResultado("DELETE_LOGICO", "producto id=" + productoId + " marcado eliminado=true, productos fisicos=10");
    }

    private Producto buscarProductoPorNombre(String nombre) {
        return consultar(entityManager ->
                entityManager.createQuery(
                                "select p from Producto p where p.nombre = :nombre",
                                Producto.class
                        )
                        .setParameter("nombre", nombre)
                        .getSingleResult()
        );
    }

    private Usuario buscarUsuarioPorMail(String mail) {
        return consultar(entityManager ->
                entityManager.createQuery(
                                "select u from Usuario u where u.mail = :mail",
                                Usuario.class
                        )
                        .setParameter("mail", mail)
                        .getSingleResult()
        );
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

    private void imprimirResultado(String paso, String resultado) {
        System.out.println("[JPA-INTEGRATION] " + paso + " -> " + resultado);
    }
}
