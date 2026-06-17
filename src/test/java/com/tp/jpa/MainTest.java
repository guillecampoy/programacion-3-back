package com.tp.jpa;

import com.tp.jpa.entities.Categoria;
import com.tp.jpa.entities.Producto;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private static void ejecutar(Main main) {
        try {
            Method method = Main.class.getDeclaredMethod("ejecutar");
            method.setAccessible(true);
            method.invoke(main);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---- Fake repositories ----

    static class FakeCategoriaRepository extends CategoriaRepository {
        private final Map<Long, Categoria> store = new HashMap<>();
        private long nextId = 1;

        FakeCategoriaRepository() {
        }

        void add(Categoria c) {
            store.put(c.getId(), c);
            if (c.getId() >= nextId) nextId = c.getId() + 1;
        }

        @Override
        public Categoria guardar(Categoria entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            } else if (!store.containsKey(entity.getId())) {
                if (entity.getId() >= nextId) nextId = entity.getId() + 1;
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
                    .filter(c -> !Boolean.TRUE.equals(c.getEliminado()))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean eliminarLogico(Long id) {
            Categoria c = store.get(id);
            if (c == null || Boolean.TRUE.equals(c.getEliminado())) return false;
            c.setEliminado(true);
            return true;
        }

        @Override
        public long siguienteId() {
            return nextId++;
        }
    }

    static class FakeProductoRepository extends ProductoRepository {
        private final Map<Long, Producto> store = new HashMap<>();
        private long nextId = 1;

        FakeProductoRepository() {
        }

        void add(Producto p) {
            store.put(p.getId(), p);
            if (p.getId() >= nextId) nextId = p.getId() + 1;
        }

        @Override
        public Producto guardar(Producto entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            } else if (!store.containsKey(entity.getId())) {
                if (entity.getId() >= nextId) nextId = entity.getId() + 1;
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
                    .filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean eliminarLogico(Long id) {
            Producto p = store.get(id);
            if (p == null || Boolean.TRUE.equals(p.getEliminado())) return false;
            p.setEliminado(true);
            return true;
        }

        @Override
        public long siguienteId() {
            return nextId++;
        }

        @Override
        public List<Producto> buscarPorCategoria(Long categoriaId) {
            return store.values().stream()
                    .filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
                    .filter(p -> p.getCategoria() != null && Objects.equals(p.getCategoria().getId(), categoriaId))
                    .collect(Collectors.toList());
        }
    }

    // ---- Helper: build a valid Categoria for testing ----

    private static Categoria crearCategoria(long id, String nombre) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        c.setDescripcion("Desc " + nombre);
        c.setEliminado(false);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    private static Producto crearProducto(long id, String nombre, double precio, int stock, Categoria categoria) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setDescripcion("Desc " + nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setImagen("test.png");
        p.setDisponible(true);
        p.setEliminado(false);
        p.setCreatedAt(LocalDateTime.now());
        p.setCategoria(categoria);
        return p;
    }

    // ===== MAIN MENU FLOW TESTS =====

    @Test
    void testExitMenu() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Sistema JPA"));
    }

    @Test
    void testInvalidOptionThenExit() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("9\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Opcion invalida"));
    }

    // ===== CATEGORIA TESTS =====

    @Test
    void testAltaCategoria() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("1\n1\nBebidas\nBebidas varias\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Categoria creada correctamente"));
        assertTrue(catRepo.listarActivos().stream().anyMatch(c -> c.getNombre().equals("Bebidas")));
    }

    @Test
    void testAltaCategoriaBlankNameThenValid() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("1\n1\n\nBebidas\nDescripcion\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("el nombre de la categoria es obligatorio"));
        assertTrue(output.contains("Categoria creada correctamente"));
    }

    @Test
    void testModificarCategoria() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria existing = crearCategoria(1, "Original");
        catRepo.add(existing);
        Scanner scanner = new Scanner("1\n2\n1\nModificado\nDescMod\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Categoria modificada correctamente"));
        assertEquals("Modificado", catRepo.buscarPorId(1L).map(Categoria::getNombre).orElse(""));
    }

    @Test
    void testModificarCategoriaEmptyFieldPreserves() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria existing = crearCategoria(1, "Original");
        catRepo.add(existing);
        Scanner scanner = new Scanner("1\n2\n1\n\n\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Categoria modificada correctamente"));
        assertEquals("Original", catRepo.buscarPorId(1L).map(Categoria::getNombre).orElse(""));
    }

    @Test
    void testModificarCategoriaNoActives() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("1\n2\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("No hay categorias activas para modificar"));
    }

    @Test
    void testBajaCategoria() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria c = crearCategoria(1, "ParaBorrar");
        catRepo.add(c);
        assertFalse(c.getEliminado());
        Scanner scanner = new Scanner("1\n3\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Categoria dada de baja correctamente"));
        assertTrue(catRepo.buscarPorId(1L).map(Categoria::getEliminado).orElse(false));
    }

    @Test
    void testBajaCategoriaNotFound() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("1\n3\n99\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("no existe una categoria activa"));
    }

    @Test
    void testBajaCategoriaAlreadyDeleted() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria c = crearCategoria(1, "YaBorrada");
        c.setEliminado(true);
        catRepo.add(c);
        Scanner scanner = new Scanner("1\n3\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("ya se encuentra dada de baja"));
    }

    // ===== PRODUCTO TESTS =====

    @Test
    void testAltaProducto() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Scanner scanner = new Scanner("2\n1\n1\nCafe\nCafe molido\n1500.00\n10\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Producto creado correctamente"));
        assertTrue(prodRepo.listarActivos().stream().anyMatch(p -> p.getNombre().equals("Cafe")));
    }

    @Test
    void testAltaProductoNoCategorias() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("2\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("No hay categorias activas disponibles"));
    }

    @Test
    void testAltaProductoInvalidPrecioThenValid() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Scanner scanner = new Scanner("2\n1\n1\nTe\nTe verde\n0\n10.50\n5\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Producto creado correctamente"));
    }

    @Test
    void testAltaProductoInvalidStock() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Scanner scanner = new Scanner("2\n1\n1\nAgua\nAgua mineral\n1.50\n-1\n5\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Producto creado correctamente"));
    }

    @Test
    void testModificarProducto() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Producto prod = crearProducto(1, "Original", 100.0, 5, cat);
        prodRepo.add(prod);
        Scanner scanner = new Scanner("2\n2\n1\nModificado\n\n\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Producto modificado correctamente"));
        assertEquals("Modificado", prodRepo.buscarPorId(1L).map(Producto::getNombre).orElse(""));
    }

    @Test
    void testModificarProductoNoActives() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("2\n2\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("No hay productos activos para modificar"));
    }

    @Test
    void testBajaProducto() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Producto prod = crearProducto(1, "ParaBorrar", 50.0, 3, cat);
        prodRepo.add(prod);
        Scanner scanner = new Scanner("2\n3\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Producto dado de baja correctamente"));
        assertTrue(prodRepo.buscarPorId(1L).map(Producto::getEliminado).orElse(false));
    }

    @Test
    void testBajaProductoNotFound() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("2\n3\n99\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("no existe un producto activo"));
    }

    @Test
    void testBajaProductoAlreadyDeleted() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Producto prod = crearProducto(1, "YaBorrado", 50.0, 3, cat);
        prod.setEliminado(true);
        prodRepo.add(prod);
        Scanner scanner = new Scanner("2\n3\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("ya se encuentra dado de baja"));
    }

    // ===== REPORTES TESTS =====

    @Test
    void testReportePorCategoria() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Bebidas");
        catRepo.add(cat);
        Producto prod = crearProducto(1, "Cafe", 1500.0, 20, cat);
        prodRepo.add(prod);
        Scanner scanner = new Scanner("3\n1\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("Productos activos de la categoria"));
        assertTrue(output.contains("Cafe"));
    }

    @Test
    void testReportePorCategoriaNoActives() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Scanner scanner = new Scanner("3\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("No hay categorias activas disponibles"));
    }

    @Test
    void testReportePorCategoriaSinProductos() {
        FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
        FakeProductoRepository prodRepo = new FakeProductoRepository();
        Categoria cat = crearCategoria(1, "Vacia");
        catRepo.add(cat);
        Scanner scanner = new Scanner("3\n1\n1\n0\n0\n");
        Main main = new Main(scanner, catRepo, prodRepo);
        ejecutar(main);
        String output = outContent.toString();
        assertTrue(output.contains("No hay productos activos para la categoria seleccionada"));
    }

    // ===== ENTIDADES TEST =====

    @Test
    void testCategoriaEntityValidation() {
        Categoria c = crearCategoria(1, "Valida");
        assertEquals("Valida", c.getNombre());
        assertThrows(IllegalArgumentException.class, () -> c.setNombre(""));
        assertThrows(IllegalArgumentException.class, () -> c.setNombre("  "));
    }

    @Test
    void testProductoEntityValidation() {
        Categoria cat = crearCategoria(1, "Cat");
        Producto p = crearProducto(1, "Valido", 10.0, 5, cat);
        assertEquals("Valido", p.getNombre());
        assertThrows(IllegalArgumentException.class, () -> p.setNombre(""));
        assertThrows(IllegalArgumentException.class, () -> p.setPrecio(0.0));
        assertThrows(IllegalArgumentException.class, () -> p.setPrecio(-1.0));
        assertThrows(IllegalArgumentException.class, () -> p.setStock(-1));
    }
}
