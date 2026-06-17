package com.tp.jpa.repository;

import com.tp.jpa.entities.Categoria;
import com.tp.jpa.entities.Producto;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductoRepositoryTest {

    private final CategoriaRepository categoriaRepository = new CategoriaRepository();
    private final ProductoRepository productoRepository = new ProductoRepository();

    @BeforeEach
    void cleanDatabase() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("delete from DetallePedido").executeUpdate();
            em.createQuery("delete from Pedido").executeUpdate();
            em.createQuery("delete from Producto").executeUpdate();
            em.createQuery("delete from Categoria").executeUpdate();
            em.createNativeQuery("alter table Categoria alter column id restart with 1").executeUpdate();
            em.createNativeQuery("alter table Producto alter column id restart with 1").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private Categoria guardarCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion("Desc " + nombre);
        c.setEliminado(false);
        c.setCreatedAt(LocalDateTime.now());
        return categoriaRepository.guardar(c);
    }

    private Producto guardarProducto(String nombre, double precio, int stock, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion("Desc " + nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setImagen("test.png");
        p.setDisponible(true);
        p.setEliminado(false);
        p.setCreatedAt(LocalDateTime.now());
        p.setCategoria(categoria);
        return productoRepository.guardar(p);
    }

    @Test
    void testGuardar() {
        Categoria cat = guardarCategoria("Bebidas");
        Producto p = guardarProducto("Cafe", 1500.0, 20, cat);
        assertNotNull(p);
        assertNotNull(p.getId());
    }

    @Test
    void testBuscarPorId() {
        Categoria cat = guardarCategoria("Bebidas");
        Producto guardado = guardarProducto("Te", 800.0, 15, cat);
        Optional<Producto> encontrado = productoRepository.buscarPorId(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Te", encontrado.get().getNombre());
    }

    @Test
    void testBuscarPorIdNotFound() {
        Optional<Producto> resultado = productoRepository.buscarPorId(-1L);
        assertFalse(resultado.isPresent());
    }

    @Test
    void testListarActivos() {
        Categoria cat = guardarCategoria("Bebidas");
        guardarProducto("Agua", 500.0, 30, cat);
        guardarProducto("Jugo", 700.0, 10, cat);
        List<Producto> activos = productoRepository.listarActivos();
        assertEquals(2, activos.size());
    }

    @Test
    void testListarActivosExcludesDeleted() {
        Categoria cat = guardarCategoria("Bebidas");
        Producto p = guardarProducto("ParaBorrar", 100.0, 5, cat);
        productoRepository.eliminarLogico(p.getId());
        List<Producto> activos = productoRepository.listarActivos();
        assertTrue(activos.stream().noneMatch(prod -> prod.getId().equals(p.getId())));
    }

    @Test
    void testEliminarLogico() {
        Categoria cat = guardarCategoria("Bebidas");
        Producto p = guardarProducto("ParaBorrar", 100.0, 5, cat);
        assertTrue(productoRepository.eliminarLogico(p.getId()));
        Producto recuperado = productoRepository.buscarPorId(p.getId()).orElseThrow();
        assertTrue(recuperado.getEliminado());
    }

    @Test
    void testEliminarLogicoNotFound() {
        assertFalse(productoRepository.eliminarLogico(-1L));
    }

    @Test
    void testSiguienteId() {
        assertEquals(1L, productoRepository.siguienteId());
        Categoria cat = guardarCategoria("Bebidas");
        guardarProducto("Test", 10.0, 1, cat);
        assertEquals(2L, productoRepository.siguienteId());
    }

    @Test
    void testBuscarPorCategoria() {
        Categoria bebidas = guardarCategoria("Bebidas");
        Categoria comidas = guardarCategoria("Comidas");
        guardarProducto("Cafe", 1500.0, 20, bebidas);
        guardarProducto("Te", 800.0, 15, bebidas);
        guardarProducto("Pan", 300.0, 50, comidas);
        List<Producto> productosBebidas = productoRepository.buscarPorCategoria(bebidas.getId());
        assertEquals(2, productosBebidas.size());
        assertTrue(productosBebidas.stream().allMatch(p -> p.getCategoria().getId().equals(bebidas.getId())));
    }

    @Test
    void testBuscarPorCategoriaNoResults() {
        Categoria cat = guardarCategoria("Vacia");
        List<Producto> resultados = productoRepository.buscarPorCategoria(cat.getId());
        assertTrue(resultados.isEmpty());
    }

    @Test
    void testBuscarPorCategoriaExcludesDeleted() {
        Categoria cat = guardarCategoria("Bebidas");
        Producto p = guardarProducto("Cafe", 1500.0, 20, cat);
        productoRepository.eliminarLogico(p.getId());
        List<Producto> resultados = productoRepository.buscarPorCategoria(cat.getId());
        assertTrue(resultados.isEmpty());
    }

    @Test
    void testGuardarYRecuperarMantieneDatos() {
        Categoria cat = guardarCategoria("Bebidas");
        Producto p = guardarProducto("Cafe", 1500.0, 20, cat);
        Producto recuperado = productoRepository.buscarPorId(p.getId()).orElseThrow();
        assertEquals("Cafe", recuperado.getNombre());
        assertEquals(1500.0, recuperado.getPrecio(), 0.001);
        assertEquals(20, recuperado.getStock());
        assertFalse(recuperado.getEliminado());
        assertNotNull(recuperado.getCreatedAt());
        assertNotNull(recuperado.getCategoria());
        assertEquals(cat.getId(), recuperado.getCategoria().getId());
    }
}
