package com.tp.jpa;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.seed.PersistenciaInicial;
import com.tp.jpa.service.CatalogoService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    FakeCategoriaRepository() {}

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
    public List<Categoria> listarEliminados() {
      return store.values().stream()
          .filter(c -> Boolean.TRUE.equals(c.getEliminado()))
          .collect(Collectors.toList());
    }

    @Override
    public Categoria cambiarEstadoEliminado(Long id, boolean eliminado) {
      Categoria c = store.get(id);
      if (c == null) return null;
      c.setEliminado(eliminado);
      return c;
    }

    @Override
    public long siguienteId() {
      return nextId++;
    }
  }

  static class FakeProductoRepository extends ProductoRepository {
    private final Map<Long, Producto> store = new HashMap<>();
    private long nextId = 1;

    FakeProductoRepository() {}

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
    public List<Producto> listarEliminados() {
      return store.values().stream()
          .filter(p -> Boolean.TRUE.equals(p.getEliminado()))
          .collect(Collectors.toList());
    }

    @Override
    public Producto cambiarEstadoEliminado(Long id, boolean eliminado) {
      Producto p = store.get(id);
      if (p == null) return null;
      p.setEliminado(eliminado);
      return p;
    }

    @Override
    public long siguienteId() {
      return nextId++;
    }

    @Override
    public List<Producto> buscarPorCategoria(Long categoriaId) {
      return store.values().stream()
          .filter(p -> !Boolean.TRUE.equals(p.getEliminado()))
          .filter(
              p ->
                  p.getCategoria() != null && Objects.equals(p.getCategoria().getId(), categoriaId))
          .collect(Collectors.toList());
    }
  }

  static class FakeUsuarioRepository extends UsuarioRepository {
    private final Map<Long, Usuario> store = new HashMap<>();
    private long nextId = 1;
    private boolean ultimoGuardadoLlegoSinId;
    private int guardarLlamadas;

    void add(Usuario usuario) {
      store.put(usuario.getId(), usuario);
      if (usuario.getId() >= nextId) nextId = usuario.getId() + 1;
    }

    @Override
    public Usuario guardar(Usuario entity) {
      guardarLlamadas++;
      ultimoGuardadoLlegoSinId = entity.getId() == null;
      if (entity.getId() == null) {
        entity.setId(nextId++);
      }
      store.put(entity.getId(), entity);
      return entity;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Usuario> listarActivos() {
      return store.values().stream()
          .filter(usuario -> !Boolean.TRUE.equals(usuario.getEliminado()))
          .collect(Collectors.toList());
    }

    @Override
    public Optional<Usuario> buscarPorMail(String mail) {
      if (mail == null || mail.isBlank()) {
        return Optional.empty();
      }
      return store.values().stream()
          .filter(usuario -> !Boolean.TRUE.equals(usuario.getEliminado()))
          .filter(usuario -> usuario.getMail() != null && usuario.getMail().equalsIgnoreCase(mail))
          .findFirst();
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

  private static Producto crearProducto(
      long id, String nombre, double precio, int stock, Categoria categoria) {
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

  private static Usuario crearUsuario(long id, String nombre, String mail, boolean eliminado) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setNombre(nombre);
    usuario.setApellido("Apellido " + nombre);
    usuario.setMail(mail);
    usuario.setCelular("123456");
    usuario.setContrasenia("Clave123");
    usuario.setRol(Rol.USUARIO);
    usuario.setEliminado(eliminado);
    usuario.setCreatedAt(LocalDateTime.now());
    return usuario;
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

  @Test
  void testRegenerarDatosDesdeMenuPrincipal() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    AtomicBoolean regenerado = new AtomicBoolean(false);
    Scanner scanner = new Scanner("4\ns\n0\n");
    Main main =
        new Main(
            scanner,
            new CatalogoService(catRepo, prodRepo),
            () -> {
              regenerado.set(true);
              return new PersistenciaInicial.ResumenPersistencia(2, 3, 10, 10, 0, 3);
            });

    ejecutar(main);

    String output = outContent.toString();
    assertTrue(regenerado.get());
    assertTrue(output.contains("Base local regenerada correctamente"));
    assertTrue(output.contains("Usuarios: 2 | Categorias: 3 | Productos: 10 | Pedidos: 3"));
  }

  @Test
  void testRegenerarDatosCanceladoNoEjecutaAccion() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    AtomicBoolean regenerado = new AtomicBoolean(false);
    Scanner scanner = new Scanner("4\nn\n0\n");
    Main main =
        new Main(
            scanner,
            new CatalogoService(catRepo, prodRepo),
            () -> {
              regenerado.set(true);
              return new PersistenciaInicial.ResumenPersistencia(2, 3, 10, 10, 0, 3);
            });

    ejecutar(main);

    String output = outContent.toString();
    assertFalse(regenerado.get());
    assertTrue(output.contains("Operacion cancelada"));
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
  void testAltaCategoriaDescripcionVacia() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Scanner scanner = new Scanner("1\n1\nBebidas\n\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Categoria creada correctamente"));
    assertEquals("", catRepo.buscarPorId(1L).map(Categoria::getDescripcion).orElse("x"));
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
  void testModificarCategoriaIdInvalidoLuegoValido() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria existing = crearCategoria(1, "Original");
    catRepo.add(existing);
    Scanner scanner = new Scanner("1\n2\n99\n1\nNuevo\nNueva desc\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("no existe una categoria activa con el ID indicado"));
    assertTrue(output.contains("Categoria modificada correctamente"));
    assertEquals("Nuevo", catRepo.buscarPorId(1L).map(Categoria::getNombre).orElse(""));
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
    Producto activo = crearProducto(1, "Cafe", 50.0, 3, c);
    Producto eliminado = crearProducto(2, "Te", 40.0, 2, c);
    eliminado.setEliminado(true);
    prodRepo.add(activo);
    prodRepo.add(eliminado);
    assertFalse(c.getEliminado());
    Scanner scanner = new Scanner("1\n3\n1\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Categoria dada de baja correctamente"));
    assertFalse(output.contains("Productos dados de baja por cascada"));
    assertTrue(catRepo.buscarPorId(1L).map(Categoria::getEliminado).orElse(false));
    assertFalse(prodRepo.buscarPorId(1L).map(Producto::getEliminado).orElse(true));
    assertTrue(prodRepo.buscarPorId(2L).map(Producto::getEliminado).orElse(false));
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

  @Test
  void testListarCategoriasActivas() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria activa = crearCategoria(1, "Bebidas");
    Categoria eliminada = crearCategoria(2, "Archivada");
    eliminada.setEliminado(true);
    catRepo.add(activa);
    catRepo.add(eliminada);
    Scanner scanner = new Scanner("1\n4\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Categorias activas"));
    assertTrue(output.contains("Bebidas"));
    assertFalse(output.contains("Archivada"));
  }

  @Test
  void testRestaurarCategoria() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria eliminada = crearCategoria(1, "Archivada");
    eliminada.setEliminado(true);
    catRepo.add(eliminada);
    Scanner scanner = new Scanner("1\n5\n1\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Revertir baja logica de categoria"));
    assertTrue(output.contains("Categoria restaurada correctamente"));
    assertFalse(catRepo.buscarPorId(1L).map(Categoria::getEliminado).orElse(true));
  }

  @Test
  void testRestaurarCategoriaSinEliminadas() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Scanner scanner = new Scanner("1\n5\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("No hay categorias eliminadas para restaurar"));
  }

  // ===== PRODUCTO TESTS =====

  @Test
  void testAltaProducto() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria cat = crearCategoria(1, "Bebidas");
    catRepo.add(cat);
    Scanner scanner = new Scanner("2\n1\n1\nCafe\nCafe molido\n1500.00\n10\ncafe.png\nsi\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Producto creado correctamente"));
    assertTrue(prodRepo.listarActivos().stream().anyMatch(p -> p.getNombre().equals("Cafe")));
    assertEquals("cafe.png", prodRepo.buscarPorId(1L).map(Producto::getImagen).orElse(""));
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
    Scanner scanner = new Scanner("2\n1\n1\nTe\nTe verde\n0\n10.50\n5\nte.png\nno\n0\n0\n");
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
    Scanner scanner = new Scanner("2\n1\n1\nAgua\nAgua mineral\n1.50\n-1\n5\nagua.png\nsi\n0\n0\n");
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
    Scanner scanner = new Scanner("2\n2\n1\nModificado\n\n\n\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Producto modificado correctamente"));
    assertEquals("Modificado", prodRepo.buscarPorId(1L).map(Producto::getNombre).orElse(""));
    assertEquals(100.0, prodRepo.buscarPorId(1L).map(Producto::getPrecio).orElse(-1.0));
    assertEquals(5, prodRepo.buscarPorId(1L).map(Producto::getStock).orElse(-1));
    assertEquals("test.png", prodRepo.buscarPorId(1L).map(Producto::getImagen).orElse(""));
  }

  @Test
  void testModificarProductoReasignaCategoria() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria catOrigen = crearCategoria(1, "Bebidas");
    Categoria catDestino = crearCategoria(2, "Snacks");
    catRepo.add(catOrigen);
    catRepo.add(catDestino);
    Producto prod = crearProducto(1, "Original", 100.0, 5, catOrigen);
    prodRepo.add(prod);
    Scanner scanner = new Scanner("2\n2\n1\n\n\n\n2\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Producto modificado correctamente"));
    assertEquals(2L, prodRepo.buscarPorId(1L).map(p -> p.getCategoria().getId()).orElse(-1L));
  }

  @Test
  void testModificarProductoCategoriaInvalida() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria catOrigen = crearCategoria(1, "Bebidas");
    catRepo.add(catOrigen);
    Producto prod = crearProducto(1, "Original", 100.0, 5, catOrigen);
    prodRepo.add(prod);
    Scanner scanner = new Scanner("2\n2\n1\n\n\n\n99\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("no existe una categoria activa"));
    assertEquals(1L, prodRepo.buscarPorId(1L).map(p -> p.getCategoria().getId()).orElse(-1L));
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
    assertTrue(output.contains("ParaBorrar"));
    assertTrue(prodRepo.buscarPorId(1L).map(Producto::getEliminado).orElse(false));
    assertTrue(prodRepo.listarActivos().stream().noneMatch(p -> p.getId().equals(1L)));
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

  @Test
  void testListarProductosActivos() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria cat = crearCategoria(1, "Bebidas");
    catRepo.add(cat);
    Producto activo = crearProducto(1, "Cafe", 1500.0, 20, cat);
    Producto eliminado = crearProducto(2, "Archivado", 100.0, 1, cat);
    eliminado.setEliminado(true);
    prodRepo.add(activo);
    prodRepo.add(eliminado);
    Scanner scanner = new Scanner("2\n4\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Productos activos"));
    assertTrue(output.contains("Cafe"));
    assertTrue(output.contains("Desc Cafe"));
    assertFalse(output.contains("Archivado"));
  }

  @Test
  void testRestaurarProducto() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Categoria cat = crearCategoria(1, "Bebidas");
    catRepo.add(cat);
    Producto eliminado = crearProducto(1, "Archivado", 100.0, 1, cat);
    eliminado.setEliminado(true);
    prodRepo.add(eliminado);
    Scanner scanner = new Scanner("2\n5\n1\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("Revertir baja logica de producto"));
    assertTrue(output.contains("Producto restaurado correctamente"));
    assertFalse(prodRepo.buscarPorId(1L).map(Producto::getEliminado).orElse(true));
  }

  @Test
  void testRestaurarProductoSinEliminados() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    Scanner scanner = new Scanner("2\n5\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo);
    ejecutar(main);
    String output = outContent.toString();
    assertTrue(output.contains("No hay productos eliminados para restaurar"));
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
    assertTrue(output.contains("ID"));
    assertTrue(output.contains("Cafe"));
    assertTrue(output.contains("1500.0"));
    assertTrue(output.contains("20"));
    assertTrue(output.contains("Desc Cafe"));
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

  // ===== USUARIO TESTS =====

  @Test
  void testAltaUsuario() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    FakeUsuarioRepository userRepo = new FakeUsuarioRepository();
    Scanner scanner = new Scanner("5\n1\nAna\nGomez\nana@example.com\n1234\nClave123\n2\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo, userRepo);
    ejecutar(main);

    String output = outContent.toString();
    assertTrue(output.contains("Usuario creado correctamente"));
    assertEquals(1L, userRepo.buscarPorMail("ana@example.com").orElseThrow().getId());
    assertFalse(userRepo.buscarPorMail("ana@example.com").orElseThrow().getEliminado());
  }

  @Test
  void testAltaUsuarioMailDuplicado() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    FakeUsuarioRepository userRepo = new FakeUsuarioRepository();
    userRepo.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    Scanner scanner = new Scanner("5\n1\nAna2\nGomez\nana@example.com\n1234\nClave123\n1\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo, userRepo);
    ejecutar(main);

    String output = outContent.toString();
    assertTrue(output.contains("No se guardo el usuario"));
    assertTrue(output.contains("ya existe un usuario activo con ese mail"));
    assertEquals(0, userRepo.guardarLlamadas);
  }

  @Test
  void testAltaUsuarioRolValidoLuegoGuarda() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    FakeUsuarioRepository userRepo = new FakeUsuarioRepository();
    Scanner scanner =
        new Scanner("5\n1\nBruno\nPerez\nbruno@example.com\n9999\nClave123\n3\n1\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo, userRepo);
    ejecutar(main);

    String output = outContent.toString();
    assertTrue(output.contains("Opcion invalida"));
    assertTrue(output.contains("Usuario creado correctamente"));
    assertEquals(Rol.ADMIN, userRepo.buscarPorMail("bruno@example.com").orElseThrow().getRol());
  }

  @Test
  void testModificarUsuario() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    FakeUsuarioRepository userRepo = new FakeUsuarioRepository();
    userRepo.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    Scanner scanner =
        new Scanner("5\n2\n1\nAna Maria\n\nana.nueva@example.com\n\nNuevaClave\nUSUARIO\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo, userRepo);
    ejecutar(main);

    String output = outContent.toString();
    assertTrue(output.contains("Usuario modificado correctamente"));
    Usuario modificado = userRepo.buscarPorId(1L).orElseThrow();
    assertEquals("Ana Maria", modificado.getNombre());
    assertEquals("Apellido Ana", modificado.getApellido());
    assertEquals("ana.nueva@example.com", modificado.getMail());
    assertEquals("123456", modificado.getCelular());
    assertEquals("NuevaClave", modificado.getContrasenia());
    assertEquals(Rol.USUARIO, modificado.getRol());
  }

  @Test
  void testModificarUsuarioMailDuplicado() {
    FakeCategoriaRepository catRepo = new FakeCategoriaRepository();
    FakeProductoRepository prodRepo = new FakeProductoRepository();
    FakeUsuarioRepository userRepo = new FakeUsuarioRepository();
    userRepo.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    userRepo.add(crearUsuario(2L, "Bruno", "bruno@example.com", false));
    Scanner scanner = new Scanner("5\n2\n1\n\n\nbruno@example.com\n\n\n\n0\n0\n");
    Main main = new Main(scanner, catRepo, prodRepo, userRepo);
    ejecutar(main);

    String output = outContent.toString();
    assertTrue(output.contains("No se modifico el usuario"));
    assertTrue(output.contains("ya existe un usuario activo con ese mail"));
    assertEquals("ana@example.com", userRepo.buscarPorId(1L).orElseThrow().getMail());
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
    assertThrows(IllegalArgumentException.class, () -> p.setImagen(""));
    assertThrows(IllegalArgumentException.class, () -> p.setDisponible(null));
  }
}
