package com.tp.jpa.service;

import ar.edu.tup.programacion3.entities.Categoria;
import ar.edu.tup.programacion3.entities.Producto;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceTest {
    @Test
    void crearCategoriaDelegaIdALRepositorio() {
        FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
        CatalogoService service = new CatalogoService(categoriaRepository, new FakeProductoRepository());

        Categoria categoria = service.crearCategoria("Bebidas", "Bebidas varias");

        assertTrue(categoriaRepository.ultimoGuardadoLlegoSinId);
        assertEquals(1L, categoria.getId());
        assertEquals("Bebidas", categoria.getNombre());
        assertFalse(categoria.getEliminado());
    }

    @Test
    void crearProductoValidaCategoriaActivaYDelegaIdAlRepositorio() {
        FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
        FakeProductoRepository productoRepository = new FakeProductoRepository();
        Categoria categoria = crearCategoria(7L, "Bebidas", false);
        categoriaRepository.add(categoria);
        CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

        Producto producto = service.crearProducto(7L, "Cafe", "Cafe molido", 1500.0, 10);

        assertTrue(productoRepository.ultimoGuardadoLlegoSinId);
        assertEquals(1L, producto.getId());
        assertEquals("Cafe", producto.getNombre());
        assertEquals(7L, producto.getCategoria().getId());
    }

    @Test
    void bajaProductoInactivoInformaEstadoCorrecto() {
        FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
        FakeProductoRepository productoRepository = new FakeProductoRepository();
        Categoria categoria = crearCategoria(1L, "Bebidas", false);
        Producto producto = crearProducto(1L, "Cafe", categoria, true);
        productoRepository.add(producto);
        CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.bajaProducto(1L)
        );

        assertEquals("Error: el producto ya se encuentra dado de baja.", exception.getMessage());
    }

    @Test
    void reporteRechazaCategoriaInactiva() {
        FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
        FakeProductoRepository productoRepository = new FakeProductoRepository();
        categoriaRepository.add(crearCategoria(3L, "Archivada", true));
        CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarProductosActivosPorCategoria(3L)
        );

        assertEquals("Error: no existe una categoria activa con el ID indicado.", exception.getMessage());
    }

    private static Categoria crearCategoria(Long id, String nombre, boolean eliminado) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setDescripcion("Desc " + nombre);
        categoria.setEliminado(eliminado);
        categoria.setCreatedAt(LocalDateTime.now());
        return categoria;
    }

    private static Producto crearProducto(Long id, String nombre, Categoria categoria, boolean eliminado) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setDescripcion("Desc " + nombre);
        producto.setPrecio(100.0);
        producto.setStock(5);
        producto.setImagen("test.png");
        producto.setDisponible(true);
        producto.setEliminado(eliminado);
        producto.setCreatedAt(LocalDateTime.now());
        producto.setCategoria(categoria);
        return producto;
    }

    private static class FakeCategoriaRepository extends CategoriaRepository {
        private final Map<Long, Categoria> store = new HashMap<>();
        private long nextId = 1L;
        private boolean ultimoGuardadoLlegoSinId;

        void add(Categoria categoria) {
            store.put(categoria.getId(), categoria);
            nextId = Math.max(nextId, categoria.getId() + 1);
        }

        @Override
        public Categoria guardar(Categoria entity) {
            ultimoGuardadoLlegoSinId = entity.getId() == null;
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<Categoria> buscarPorId(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Categoria> listarActivos() {
            return store.values().stream()
                    .filter(categoria -> !Boolean.TRUE.equals(categoria.getEliminado()))
                    .toList();
        }

        @Override
        public boolean eliminarLogico(Long id) {
            Categoria categoria = store.get(id);
            if (categoria == null || Boolean.TRUE.equals(categoria.getEliminado())) {
                return false;
            }
            categoria.setEliminado(true);
            return true;
        }
    }

    private static class FakeProductoRepository extends ProductoRepository {
        private final Map<Long, Producto> store = new HashMap<>();
        private long nextId = 1L;
        private boolean ultimoGuardadoLlegoSinId;

        void add(Producto producto) {
            store.put(producto.getId(), producto);
            nextId = Math.max(nextId, producto.getId() + 1);
        }

        @Override
        public Producto guardar(Producto entity) {
            ultimoGuardadoLlegoSinId = entity.getId() == null;
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<Producto> buscarPorId(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Producto> listarActivos() {
            return store.values().stream()
                    .filter(producto -> !Boolean.TRUE.equals(producto.getEliminado()))
                    .toList();
        }

        @Override
        public boolean eliminarLogico(Long id) {
            Producto producto = store.get(id);
            if (producto == null || Boolean.TRUE.equals(producto.getEliminado())) {
                return false;
            }
            producto.setEliminado(true);
            return true;
        }

        @Override
        public List<Producto> buscarPorCategoria(Long categoriaId) {
            return store.values().stream()
                    .filter(producto -> !Boolean.TRUE.equals(producto.getEliminado()))
                    .filter(producto -> producto.getCategoria() != null)
                    .filter(producto -> Objects.equals(producto.getCategoria().getId(), categoriaId))
                    .toList();
        }
    }
}
