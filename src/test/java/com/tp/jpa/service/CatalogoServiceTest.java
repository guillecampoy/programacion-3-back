package com.tp.jpa.service;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.jpa.dtos.CategoriaAltaDTO;
import com.tp.jpa.dtos.ProductoAltaDTO;
import com.tp.jpa.dtos.UsuarioAltaDTO;
import com.tp.jpa.dtos.UsuarioModificacionDTO;
import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
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
        new CatalogoService(
            categoriaRepository, new FakeProductoRepository(), new FakeUsuarioRepository());

    Categoria categoria = service.crearCategoria("Bebidas", "Bebidas varias");

    assertTrue(categoriaRepository.ultimoGuardadoLlegoSinId);
    assertEquals(1L, categoria.getId());
    assertEquals("Bebidas", categoria.getNombre());
    assertFalse(categoria.getEliminado());
  }

  @Test
  void crearCategoriaConDtoDelegaIdALRepositorio() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    CatalogoService service =
        new CatalogoService(
            categoriaRepository, new FakeProductoRepository(), new FakeUsuarioRepository());

    Categoria categoria = service.crearCategoria(new CategoriaAltaDTO("Bebidas", "Bebidas varias"));

    assertTrue(categoriaRepository.ultimoGuardadoLlegoSinId);
    assertEquals("Bebidas", categoria.getNombre());
    assertEquals("Bebidas varias", categoria.getDescripcion());
  }

  @Test
  void crearProductoValidaCategoriaActivaYDelegaIdAlRepositorio() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(7L, "Bebidas", false);
    categoriaRepository.add(categoria);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Producto producto =
        service.crearProducto(7L, "Cafe", "Cafe molido", 1500.0, 10, "cafe.png", false);

    assertTrue(productoRepository.ultimoGuardadoLlegoSinId);
    assertEquals(1L, producto.getId());
    assertEquals("Cafe", producto.getNombre());
    assertEquals(7L, producto.getCategoria().getId());
    assertEquals("cafe.png", producto.getImagen());
    assertFalse(producto.getDisponible());
  }

  @Test
  void crearProductoConDtoValidaCategoriaActivaYDelegaIdAlRepositorio() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(7L, "Bebidas", false);
    categoriaRepository.add(categoria);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Producto producto =
        service.crearProducto(
            new ProductoAltaDTO(7L, "Cafe", "Cafe molido", 1500.0, 10, "cafe.png", false));

    assertTrue(productoRepository.ultimoGuardadoLlegoSinId);
    assertEquals(1L, producto.getId());
    assertEquals("Cafe", producto.getNombre());
    assertEquals(7L, producto.getCategoria().getId());
    assertEquals("cafe.png", producto.getImagen());
    assertFalse(producto.getDisponible());
  }

  @Test
  void crearCategoriaRechazaEntradaInvalidaAntesDeGuardar() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    CatalogoService service =
        new CatalogoService(
            categoriaRepository, new FakeProductoRepository(), new FakeUsuarioRepository());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.crearCategoria(" ", "Descripcion"));

    assertEquals("Error: El nombre de la categoria es obligatorio.", exception.getMessage());
    assertEquals(0, categoriaRepository.guardarLlamadas);
  }

  @Test
  void crearCategoriaPermiteDescripcionVacia() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    CatalogoService service =
        new CatalogoService(
            categoriaRepository, new FakeProductoRepository(), new FakeUsuarioRepository());

    Categoria categoria = service.crearCategoria("Bebidas", "   ");

    assertEquals("", categoria.getDescripcion());
    assertFalse(categoria.getEliminado());
    assertEquals(1, categoriaRepository.guardarLlamadas);
  }

  @Test
  void modificarCategoriaPermiteActualizarNombreYConservarDescripcionEnBlanco() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    categoriaRepository.add(crearCategoria(1L, "Bebidas", false));
    CatalogoService service =
        new CatalogoService(
            categoriaRepository, new FakeProductoRepository(), new FakeUsuarioRepository());

    Categoria modificada = service.modificarCategoria(1L, "  Bebidas frias  ", "   ");

    assertEquals("Bebidas frias", modificada.getNombre());
    assertEquals("Desc Bebidas", modificada.getDescripcion());
    assertEquals(1, categoriaRepository.guardarLlamadas);
  }

  @Test
  void modificarCategoriaRechazaCategoriaInactiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    categoriaRepository.add(crearCategoria(1L, "Bebidas", true));
    CatalogoService service =
        new CatalogoService(
            categoriaRepository, new FakeProductoRepository(), new FakeUsuarioRepository());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.modificarCategoria(1L, "A", ""));

    assertEquals(
        "Error: no existe una categoria activa con el ID indicado.", exception.getMessage());
    assertEquals(0, categoriaRepository.guardarLlamadas);
  }

  @Test
  void crearProductoRechazaPrecioYStockInvalidosAntesDeGuardar() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    categoriaRepository.add(crearCategoria(1L, "Bebidas", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException precioException =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.crearProducto(1L, "Cafe", "Cafe molido", 0.0, 10, "cafe.png", true));
    IllegalArgumentException stockException =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.crearProducto(1L, "Cafe", "Cafe molido", 1500.0, -1, "cafe.png", true));

    assertEquals("Error: el precio debe ser mayor a 0.", precioException.getMessage());
    assertEquals("Error: el stock debe ser mayor o igual a 0.", stockException.getMessage());
    assertEquals(0, productoRepository.guardarLlamadas);
  }

  @Test
  void crearProductoRechazaImagenVaciaAntesDeGuardar() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    categoriaRepository.add(crearCategoria(1L, "Bebidas", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.crearProducto(1L, "Cafe", "Cafe molido", 1500.0, 10, " ", true));

    assertEquals("Error: La imagen del producto es obligatorio.", exception.getMessage());
    assertEquals(0, productoRepository.guardarLlamadas);
  }

  @Test
  void modificarProductoRechazaEntradaInvalidaSinMutarEntidad() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, false);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

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
  void modificarProductoConCamposVaciosConservaLosValoresPrevios() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, false);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Producto modificado = service.modificarProducto(1L, "  ", null, null, null);

    assertEquals("Cafe", modificado.getNombre());
    assertEquals(100.0, modificado.getPrecio());
    assertEquals(5, modificado.getStock());
    assertEquals(1L, modificado.getCategoria().getId());
    assertEquals(1, productoRepository.guardarLlamadas);
  }

  @Test
  void modificarProductoPermiteReasignarCategoriaActiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoriaOrigen = crearCategoria(1L, "Bebidas", false);
    Categoria categoriaDestino = crearCategoria(2L, "Snacks", false);
    Producto producto = crearProducto(1L, "Cafe", categoriaOrigen, false);
    categoriaRepository.add(categoriaOrigen);
    categoriaRepository.add(categoriaDestino);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Producto modificado = service.modificarProducto(1L, "Cafe premium", null, null, 2L);

    assertEquals("Cafe premium", modificado.getNombre());
    assertEquals(2L, modificado.getCategoria().getId());
    assertTrue(productoRepository.guardarLlamadas > 0);
  }

  @Test
  void modificarProductoRechazaCategoriaInactiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoriaOrigen = crearCategoria(1L, "Bebidas", false);
    Categoria categoriaDestino = crearCategoria(2L, "Snacks", true);
    Producto producto = crearProducto(1L, "Cafe", categoriaOrigen, false);
    categoriaRepository.add(categoriaOrigen);
    categoriaRepository.add(categoriaDestino);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

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
  void modificarProductoRechazaProductoDadoDeBaja() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.modificarProducto(1L, "Cafe premium", 200.0, 8, null));

    assertEquals("Error: no existe un producto activo con el ID indicado.", exception.getMessage());
    assertEquals("Cafe", productoRepository.buscarPorId(1L).orElseThrow().getNombre());
    assertEquals(0, productoRepository.guardarLlamadas);
  }

  @Test
  void operacionesRechazanIdsInvalidosAntesDeConsultarRepositorio() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, new FakeUsuarioRepository());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.obtenerCategoriaActiva(0L));

    assertEquals("Error: el ID de categoria debe ser mayor a 0.", exception.getMessage());
    assertEquals(0, categoriaRepository.buscarPorIdLlamadas);
  }

  @Test
  void bajaProductoInactivoInformaEstadoCorrecto() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.bajaProducto(1L));

    assertEquals("Error: el producto ya se encuentra dado de baja.", exception.getMessage());
  }

  @Test
  void bajaProductoMarcaEliminadoYNoApareceEnActivos() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, false);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Producto dadoDeBaja = service.bajaProducto(1L);

    assertTrue(dadoDeBaja.getEliminado());
    assertTrue(productoRepository.buscarPorId(1L).orElseThrow().getEliminado());
    assertTrue(productoRepository.listarActivos().stream().noneMatch(p -> p.getId().equals(1L)));
  }

  @Test
  void bajaProductoRechazaSegundoIntento() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.bajaProducto(1L));

    assertEquals("Error: el producto ya se encuentra dado de baja.", exception.getMessage());
  }

  @Test
  void bajaCategoriaSoloDesactivaLaCategoriaYConservaLosProductos() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto activo = crearProducto(1L, "Cafe", categoria, false);
    Producto yaEliminado = crearProducto(2L, "Te", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(activo);
    productoRepository.add(yaEliminado);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    CatalogoService.BajaCategoriaResultado resultado = service.bajaCategoria(1L);

    assertTrue(resultado.categoria().getEliminado());
    assertTrue(resultado.productosDadosDeBaja().isEmpty());
    assertFalse(productoRepository.buscarPorId(1L).orElseThrow().getEliminado());
    assertTrue(productoRepository.buscarPorId(2L).orElseThrow().getEliminado());
  }

  @Test
  void bajaCategoriaRechazaUnaCategoriaYaDadaDeBaja() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    categoriaRepository.add(crearCategoria(1L, "Bebidas", true));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.bajaCategoria(1L));

    assertEquals("Error: la categoria ya se encuentra dada de baja.", exception.getMessage());
  }

  @Test
  void restaurarCategoriaYProductoValidanEstadoYReactivanEntidad() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", true);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

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
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", true);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

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
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria categoria = crearCategoria(1L, "Bebidas", false);
    Producto producto = crearProducto(1L, "Cafe", categoria, true);
    categoriaRepository.add(categoria);
    productoRepository.add(producto);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Producto productoRestaurado = service.restaurarProducto(1L);

    assertFalse(productoRestaurado.getEliminado());
  }

  @Test
  void buscarProductosActivosPorCategoriaDevuelveSoloActivosDeLaCategoria() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    Categoria bebidas = crearCategoria(1L, "Bebidas", false);
    Categoria comidas = crearCategoria(2L, "Comidas", false);
    Producto cafe = crearProducto(1L, "Cafe", bebidas, false);
    Producto te = crearProducto(2L, "Te", bebidas, true);
    Producto pan = crearProducto(3L, "Pan", comidas, false);
    categoriaRepository.add(bebidas);
    categoriaRepository.add(comidas);
    productoRepository.add(cafe);
    productoRepository.add(te);
    productoRepository.add(pan);
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    List<Producto> productos = service.buscarProductosActivosPorCategoria(1L);

    assertEquals(1, productos.size());
    assertEquals("Cafe", productos.get(0).getNombre());
    assertEquals(1L, productos.get(0).getCategoria().getId());
  }

  @Test
  void reporteRechazaCategoriaInactiva() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    categoriaRepository.add(crearCategoria(3L, "Archivada", true));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.buscarProductosActivosPorCategoria(3L));

    assertEquals(
        "Error: no existe una categoria activa con el ID indicado.", exception.getMessage());
  }

  @Test
  void crearUsuarioDelegaIdYLimpiaBanderas() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Usuario usuario =
        service.crearUsuario("Ana", "Gomez", "ana@example.com", "1234", "Clave123", Rol.ADMIN);

    assertTrue(usuarioRepository.ultimoGuardadoLlegoSinId);
    assertEquals(1L, usuario.getId());
    assertEquals("Ana", usuario.getNombre());
    assertEquals(Rol.ADMIN, usuario.getRol());
    assertFalse(usuario.getEliminado());
  }

  @Test
  void crearUsuarioConDtoDelegaIdYLimpiaBanderas() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Usuario usuario =
        service.crearUsuario(
            new UsuarioAltaDTO("Ana", "Gomez", "ana@example.com", "1234", "Clave123", Rol.ADMIN));

    assertTrue(usuarioRepository.ultimoGuardadoLlegoSinId);
    assertEquals("Ana", usuario.getNombre());
    assertEquals(Rol.ADMIN, usuario.getRol());
    assertFalse(usuario.getEliminado());
  }

  @Test
  void crearUsuarioRechazaMailActivoDuplicado() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () ->
                service.crearUsuario(
                    "Ana 2", "Gomez", "ana@example.com", "1234", "Clave123", Rol.USUARIO));

    assertEquals("Error: ya existe un usuario activo con ese mail.", exception.getMessage());
    assertEquals(0, usuarioRepository.guardarLlamadas);
  }

  @Test
  void crearUsuarioRechazaRolNuloAntesDeGuardar() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                service.crearUsuario("Ana", "Gomez", "ana2@example.com", "1234", "Clave123", null));

    assertEquals("Error: el rol del usuario es obligatorio.", exception.getMessage());
    assertEquals(0, usuarioRepository.guardarLlamadas);
  }

  @Test
  void modificarUsuarioPermiteActualizarCamposYConservarBlancos() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Usuario usuario =
        service.modificarUsuario(
            1L, "Ana Maria", "", "ana.nueva@example.com", "", "NuevaClave", Rol.ADMIN);

    assertEquals("Ana Maria", usuario.getNombre());
    assertEquals("Apellido Ana", usuario.getApellido());
    assertEquals("ana.nueva@example.com", usuario.getMail());
    assertEquals("1234", usuario.getCelular());
    assertEquals("NuevaClave", usuario.getContrasenia());
    assertEquals(Rol.ADMIN, usuario.getRol());
    assertEquals(1, usuarioRepository.guardarLlamadas);
  }

  @Test
  void modificarUsuarioConDtoPermiteActualizarCamposYConservarBlancos() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Usuario usuario =
        service.modificarUsuario(
            new UsuarioModificacionDTO(
                1L, "Ana Maria", "", "ana.nueva@example.com", "", "NuevaClave", Rol.ADMIN));

    assertEquals("Ana Maria", usuario.getNombre());
    assertEquals("Apellido Ana", usuario.getApellido());
    assertEquals("ana.nueva@example.com", usuario.getMail());
    assertEquals("1234", usuario.getCelular());
    assertEquals("NuevaClave", usuario.getContrasenia());
    assertEquals(Rol.ADMIN, usuario.getRol());
    assertEquals(1, usuarioRepository.guardarLlamadas);
  }

  @Test
  void modificarUsuarioRechazaMailDeOtroActivo() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    usuarioRepository.add(crearUsuario(2L, "Bruno", "bruno@example.com", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> service.modificarUsuario(1L, "", "", "bruno@example.com", "", "", null));

    assertEquals("Error: ya existe un usuario activo con ese mail.", exception.getMessage());
    assertEquals("ana@example.com", usuarioRepository.buscarPorId(1L).orElseThrow().getMail());
    assertEquals(0, usuarioRepository.guardarLlamadas);
  }

  @Test
  void modificarUsuarioRechazaUsuarioEliminado() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", true));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.modificarUsuario(1L, "Ana Maria", "", "", "", "", Rol.USUARIO));

    assertEquals("Error: no existe un usuario activo con el ID indicado.", exception.getMessage());
    assertEquals(0, usuarioRepository.guardarLlamadas);
  }

  @Test
  void bajaUsuarioMarcaEliminadoYConservaElRegistro() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", false));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    Usuario dadoDeBaja = service.bajaUsuario(1L);

    assertTrue(dadoDeBaja.getEliminado());
    assertTrue(usuarioRepository.buscarPorId(1L).orElseThrow().getEliminado());
    assertTrue(usuarioRepository.listarActivos().isEmpty());
    assertEquals(0, usuarioRepository.guardarLlamadas);
  }

  @Test
  void bajaUsuarioRechazaUsuarioYaEliminado() {
    FakeCategoriaRepository categoriaRepository = new FakeCategoriaRepository();
    FakeProductoRepository productoRepository = new FakeProductoRepository();
    FakeUsuarioRepository usuarioRepository = new FakeUsuarioRepository();
    usuarioRepository.add(crearUsuario(1L, "Ana", "ana@example.com", true));
    CatalogoService service =
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.bajaUsuario(1L));

    assertEquals("Error: no existe un usuario activo con el ID indicado.", exception.getMessage());
    assertEquals(0, usuarioRepository.guardarLlamadas);
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

  private static Usuario crearUsuario(Long id, String nombre, String mail, boolean eliminado) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setNombre(nombre);
    usuario.setApellido("Apellido " + nombre);
    usuario.setMail(mail);
    usuario.setCelular("1234");
    usuario.setContrasenia("Clave123");
    usuario.setRol(Rol.USUARIO);
    usuario.setEliminado(eliminado);
    usuario.setCreatedAt(LocalDateTime.now());
    return usuario;
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

  private static class FakeUsuarioRepository extends UsuarioRepository {
    private final Map<Long, Usuario> store = new HashMap<>();
    private long nextId = 1L;
    private boolean ultimoGuardadoLlegoSinId;
    private int guardarLlamadas;

    void add(Usuario usuario) {
      store.put(usuario.getId(), usuario);
      nextId = Math.max(nextId, usuario.getId() + 1);
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
          .toList();
    }

    @Override
    public Optional<Usuario> buscarPorMail(String mail) {
      if (mail == null || mail.isBlank()) {
        return Optional.empty();
      }
      return store.values().stream()
          .filter(usuario -> !Boolean.TRUE.equals(usuario.getEliminado()))
          .filter(
              usuario ->
                  usuario.getMail() != null && usuario.getMail().equalsIgnoreCase(mail.trim()))
          .findFirst();
    }

    @Override
    public Usuario cambiarEstadoEliminado(Long id, boolean eliminado) {
      Usuario usuario = store.get(id);
      if (usuario == null) {
        return null;
      }
      usuario.setEliminado(eliminado);
      return usuario;
    }
  }
}
