package com.tp.jpa.service;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogoServiceTest {
  @Test
  void crearCategoriaDelegaIdALRepositorio() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    CatalogoService service =
        new CatalogoService(categoriaRepository, new FakeProductoRepository());

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
  void crearCategoriaRechazaEntradaInvalidaAntesDeGuardar() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    CatalogoService service =
        new CatalogoService(categoriaRepository, new FakeProductoRepository());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.crearCategoria(" ", "Descripcion"));

    assertEquals("Error: El nombre de la categoria es obligatorio.", exception.getMessage());
    assertEquals(0, categoriaRepository.guardarLlamadas);
  }

  @Test
  void crearProductoRechazaPrecioYStockInvalidosAntesDeGuardar() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    categoriaRepository.add(crearCategoria(1L, "Bebidas", false));
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalArgumentException precioException =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.crearProducto(1L, "Cafe", "Cafe molido", 0.0, 10));
    IllegalArgumentException stockException =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.crearProducto(1L, "Cafe", "Cafe molido", 1500.0, -1));

    assertEquals("Error: el precio debe ser mayor a 0.", precioException.getMessage());
    assertEquals("Error: el stock debe ser mayor o igual a 0.", stockException.getMessage());
    assertEquals(0, productoRepository.guardarLlamadas);
  }

  @Test
  void modificarProductoRechazaEntradaInvalidaSinMutarEntidad() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, false);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.modificarProducto(1L, "Actualizado", -5.0, 9, null));

    assertEquals("Error: el precio debe ser mayor a 0.", exception.getMessage());
    assertEquals("Cafe", producto.getNombre());
    assertEquals(100.0, producto.getPrecio());
    assertEquals(5, producto.getStock());
    assertEquals(0, productoRepository.guardarLlamadas);
  }

  @Test
  void modificarProductoPermiteReasignarCategoriaActiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoriaOrigen = crearCategoria(1L, "Bebidas", false);
    Categoria categoriaDestino = crearCategoria(2L, "Snacks", false);
    Producto producto = crearProducto(1L, "Cafe", categoriaOrigen, false);
    categoriaRepository.add(categoriaOrigen);
    categoriaRepository.add(categoriaDestino);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    Producto modificado = service.modificarProducto(1L, "Cafe premium", null, null, 2L);

    assertEquals("Cafe premium", modificado.getNombre());
    assertEquals(2L, modificado.getCategoria().getId());
    assertTrue(productoRepository.guardarLlamadas > 0);
  }

  @Test
  void modificarProductoRechazaCategoriaInactiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoriaOrigen = crearCategoria(1L, "Bebidas", false);
    Categoria categoriaDestino = crearCategoria(2L, "Snacks", true);
    Producto producto = crearProducto(1L, "Cafe", categoriaOrigen, false);
    categoriaRepository.add(categoriaOrigen);
    categoriaRepository.add(categoriaDestino);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.modificarProducto(1L, null, null, null, 2L));

    assertEquals(
        "Error: no existe una categoria activa con el ID indicado.", exception.getMessage());
    assertEquals(1L, productoRepository.buscarPorId(1L).orElseThrow().getCategoria().getId());
    assertEquals(0, productoRepository.guardarLlamadas);
  }

  @Test
  void operacionesRechazanIdsInvalidosAntesDeConsultarRepositorio() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.obtenerCategoriaActiva(0L));

    assertEquals("Error: el ID de categoria debe ser mayor a 0.", exception.getMessage());
    assertEquals(0, categoriaRepository.buscarPorIdLlamadas);
  }

  @Test
  void bajaProductoInactivoInformaEstadoCorrecto() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.bajaProducto(1L));

    assertEquals("Error: el producto ya se encuentra dado de baja.", exception.getMessage());
  }

  @Test
  void bajaCategoriaDesactivaProductosActivosAsociadosYReportaCualesSeAfectan() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto activo = crearProducto(1L, "Cafe", categoria, false);
    Producto yaEliminado = crearProducto(2L, "Te", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(activo);
    productoRepository.add(yaEliminado);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    CatalogoService.BajaCategoriaResultado resultado = service.bajaCategoria(1L);

    assertTrue(resultado.categoria().getEliminado());
    assertEquals(1, resultado.productosDadosDeBaja().size());
    assertEquals("Cafe", resultado.productosDadosDeBaja().get(0).getNombre());
    assertTrue(productoRepository.buscarPorId(1L).orElseThrow().getEliminado());
    assertTrue(productoRepository.buscarPorId(2L).orElseThrow().getEliminado());
  }

  @Test
  void restaurarCategoriaYProductoValidanEstadoYReactivanEntidad() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", true);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    Categoria categoriaRestaurada = service.restaurarCategoria(1L);
    Producto productoRestaurado = service.restaurarProducto(1L);

    assertFalse(categoriaRestaurada.getEliminado());
    assertFalse(productoRestaurado.getEliminado());
    assertThrows(IllegalStateException.class, () -> service.restaurarCategoria(1L));
    assertThrows(IllegalStateException.class, () -> service.restaurarProducto(1L));
  }

  @Test
  void restaurarProductoRechazaSiCategoriaEstaEliminada() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", true);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.restaurarProducto(1L));

    assertEquals(
        "Error: no se puede restaurar el producto porque su categoria se encuentra dada de baja.",
        exception.getMessage());
    assertTrue(productoRepository.buscarPorId(1L).orElseThrow().getEliminado());
  }

  @Test
  void restaurarProductoPermiteSiCategoriaEstaActiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    Producto productoRestaurado = service.restaurarProducto(1L);

    assertFalse(productoRestaurado.getEliminado());
  }

  @Test
  void reporteRechazaCategoriaInactiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    categoriaRepository.add(crearCategoria(3L, "Archivada", true));
    CatalogoService service = new CatalogoService(categoriaRepository, productoRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.buscarProductosActivosPorCategoria(3L));

    assertEquals(
        "Error: no existe una categoria activa con el ID indicado.", exception.getMessage());
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

  private static Producto crearProducto(
      Long id, String nombre, Categoria categoria, boolean eliminado) {
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
    private int guardarLlamadas;
    private int buscarPorIdLlamadas;

    void add(Categoria categoria) {
      store.put(categoria.getId(), categoria);
      nextId = Math.max(nextId, categoria.getId() + 1);
    }

    @Override
    public Categoria guardar(Categoria entity) {
      guardarLlamadas++;
      ultimoGuardadoLlegoSinId = entity.getId() == null;
      if (entity.getId() == null) {
        entity.setId(nextId++);
      }
      store.put(entity.getId(), entity);
      return entity;
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
      buscarPorIdLlamadas++;
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Categoria> listarActivos() {
      return store.values().stream()
          .filter(categoria -> !Boolean.TRUE.equals(categoria.getEliminado()))
          .toList();
    }

    @Override
    public Categoria cambiarEstadoEliminado(Long id, boolean eliminado) {
      Categoria categoria = store.get(id);
      if (categoria == null) {
        return null;
      }
      categoria.setEliminado(eliminado);
      return categoria;
    }
  }

  private static class FakeProductoRepository extends ProductoRepository {
    private final Map<Long, Producto> store = new HashMap<>();
    private long nextId = 1L;
    private boolean ultimoGuardadoLlegoSinId;
    private int guardarLlamadas;

    void add(Producto producto) {
      store.put(producto.getId(), producto);
      nextId = Math.max(nextId, producto.getId() + 1);
    }

    @Override
    public Producto guardar(Producto entity) {
      guardarLlamadas++;
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
    public Producto cambiarEstadoEliminado(Long id, boolean eliminado) {
      Producto producto = store.get(id);
      if (producto == null) {
        return null;
      }
      producto.setEliminado(eliminado);
      return producto;
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
