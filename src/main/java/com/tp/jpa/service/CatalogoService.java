package com.tp.jpa.service;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CatalogoService {
  private final CategoriaRepository categoriaRepository;
  private final ProductoRepository productoRepository;
  private final UsuarioRepository usuarioRepository;

  public record BajaCategoriaResultado(Categoria categoria, List<Producto> productosDadosDeBaja) {}

  public CatalogoService(
      CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
    this(categoriaRepository, productoRepository, new UsuarioRepository());
  }

  public CatalogoService(
      CategoriaRepository categoriaRepository,
      ProductoRepository productoRepository,
      UsuarioRepository usuarioRepository) {
    this.categoriaRepository = categoriaRepository;
    this.productoRepository = productoRepository;
    this.usuarioRepository = usuarioRepository;
  }

  public List<Categoria> listarCategoriasActivas() {
    return categoriaRepository.listarActivos();
  }

  public List<Categoria> listarCategoriasEliminadas() {
    return categoriaRepository.listarEliminados();
  }

  public List<Producto> listarProductosActivos() {
    return productoRepository.listarActivos();
  }

  public List<Producto> listarProductosEliminados() {
    return productoRepository.listarEliminados();
  }

  public List<Usuario> listarUsuariosActivos() {
    return usuarioRepository.listarActivos();
  }

  public Usuario obtenerUsuarioActivo(Long id) {
    validarId(id, "usuario");
    Usuario usuario = obtenerUsuario(id);
    if (Boolean.TRUE.equals(usuario.getEliminado())) {
      throw new IllegalArgumentException("Error: no existe un usuario activo con el ID indicado.");
    }
    return usuario;
  }

  public Usuario crearUsuario(
      String nombre, String apellido, String mail, String celular, String contrasenia, Rol rol) {
    String nombreNormalizado = requerirTexto(nombre, "El nombre del usuario");
    String apellidoNormalizado = requerirTexto(apellido, "El apellido del usuario");
    String mailNormalizado = requerirTexto(mail, "El mail del usuario");
    String celularNormalizado = requerirTexto(celular, "El celular del usuario");
    String contraseniaNormalizada = requerirTexto(contrasenia, "La contrasenia del usuario");
    validarRol(rol);
    validarMailUnico(mailNormalizado, null);

    Usuario usuario = new Usuario();
    usuario.setNombre(nombreNormalizado);
    usuario.setApellido(apellidoNormalizado);
    usuario.setMail(mailNormalizado);
    usuario.setCelular(celularNormalizado);
    usuario.setContrasenia(contraseniaNormalizada);
    usuario.setRol(rol);
    usuario.setEliminado(false);
    usuario.setCreatedAt(LocalDateTime.now());
    return usuarioRepository.guardar(usuario);
  }

  public Usuario modificarUsuario(
      Long id,
      String nombre,
      String apellido,
      String mail,
      String celular,
      String contrasenia,
      Rol rol) {
    validarId(id, "usuario");
    Usuario usuario = obtenerUsuarioActivo(id);
    if (nombre != null && !nombre.isBlank()) {
      usuario.setNombre(requerirTexto(nombre, "El nombre del usuario"));
    }
    if (apellido != null && !apellido.isBlank()) {
      usuario.setApellido(requerirTexto(apellido, "El apellido del usuario"));
    }
    if (mail != null && !mail.isBlank()) {
      String mailNormalizado = requerirTexto(mail, "El mail del usuario");
      if (!mailNormalizado.equalsIgnoreCase(usuario.getMail())) {
        validarMailUnico(mailNormalizado, id);
      }
      usuario.setMail(mailNormalizado);
    }
    if (celular != null && !celular.isBlank()) {
      usuario.setCelular(requerirTexto(celular, "El celular del usuario"));
    }
    if (contrasenia != null && !contrasenia.isBlank()) {
      usuario.setContrasenia(requerirTexto(contrasenia, "La contrasenia del usuario"));
    }
    if (rol != null) {
      validarRol(rol);
      usuario.setRol(rol);
    }
    return usuarioRepository.guardar(usuario);
  }

  public Categoria crearCategoria(String nombre, String descripcion) {
    String nombreNormalizado = requerirTexto(nombre, "El nombre de la categoria");
    String descripcionNormalizada = descripcion == null ? "" : descripcion.trim();

    Categoria categoria = new Categoria();
    categoria.setNombre(nombreNormalizado);
    categoria.setDescripcion(descripcionNormalizada);
    categoria.setEliminado(false);
    categoria.setCreatedAt(LocalDateTime.now());
    return categoriaRepository.guardar(categoria);
  }

  public Categoria modificarCategoria(Long id, String nombre, String descripcion) {
    validarId(id, "categoria");
    Categoria categoria = obtenerCategoriaActiva(id);
    if (nombre != null && !nombre.isBlank()) {
      categoria.setNombre(requerirTexto(nombre, "El nombre de la categoria"));
    }
    if (descripcion != null && !descripcion.isBlank()) {
      categoria.setDescripcion(requerirTexto(descripcion, "La descripcion de la categoria"));
    }
    return categoriaRepository.guardar(categoria);
  }

  public BajaCategoriaResultado bajaCategoria(Long id) {
    validarId(id, "categoria");
    Categoria categoria = obtenerCategoria(id);
    if (Boolean.TRUE.equals(categoria.getEliminado())) {
      throw new IllegalStateException("Error: la categoria ya se encuentra dada de baja.");
    }
    Categoria categoriaBaja = categoriaRepository.cambiarEstadoEliminado(id, true);
    return new BajaCategoriaResultado(categoriaBaja, List.of());
  }

  public Categoria restaurarCategoria(Long id) {
    validarId(id, "categoria");
    Categoria categoria = obtenerCategoriaPorId(id);
    if (!Boolean.TRUE.equals(categoria.getEliminado())) {
      throw new IllegalStateException("Error: la categoria ya se encuentra activa.");
    }
    return categoriaRepository.cambiarEstadoEliminado(id, false);
  }

  public Categoria obtenerCategoriaActiva(Long id) {
    validarId(id, "categoria");
    Categoria categoria = obtenerCategoria(id);
    if (Boolean.TRUE.equals(categoria.getEliminado())) {
      throw new IllegalArgumentException(
          "Error: no existe una categoria activa con el ID indicado.");
    }
    return categoria;
  }

  public Producto crearProducto(
      Long categoriaId,
      String nombre,
      String descripcion,
      double precio,
      int stock,
      String imagen,
      boolean disponible) {
    validarId(categoriaId, "categoria");
    String nombreNormalizado = requerirTexto(nombre, "El nombre del producto");
    String descripcionNormalizada = requerirTexto(descripcion, "La descripcion del producto");
    String imagenNormalizada = requerirTexto(imagen, "La imagen del producto");
    validarPrecio(precio);
    validarStock(stock);

    Categoria categoria = obtenerCategoriaActiva(categoriaId);
    Producto producto = new Producto();
    producto.setNombre(nombreNormalizado);
    producto.setDescripcion(descripcionNormalizada);
    producto.setPrecio(precio);
    producto.setStock(stock);
    producto.setImagen(imagenNormalizada);
    producto.setDisponible(disponible);
    producto.setEliminado(false);
    producto.setCreatedAt(LocalDateTime.now());
    producto.setCategoria(referenciaCategoria(categoria));
    return productoRepository.guardar(producto);
  }

  public Producto modificarProducto(
      Long id, String nombre, Double precio, Integer stock, Long categoriaId) {
    validarId(id, "producto");
    if (nombre != null && !nombre.isBlank()) {
      requerirTexto(nombre, "El nombre del producto");
    }
    if (precio != null) {
      validarPrecio(precio);
    }
    if (stock != null) {
      validarStock(stock);
    }
    Producto producto = obtenerProductoActivo(id);
    Categoria nuevaCategoria = null;
    if (categoriaId != null) {
      nuevaCategoria = obtenerCategoriaActiva(categoriaId);
    }
    if (nombre != null && !nombre.isBlank()) {
      producto.setNombre(nombre.trim());
    }
    if (precio != null) {
      producto.setPrecio(precio);
    }
    if (stock != null) {
      producto.setStock(stock);
    }
    if (nuevaCategoria != null) {
      producto.setCategoria(referenciaCategoria(nuevaCategoria));
    }
    return productoRepository.guardar(producto);
  }

  public Producto bajaProducto(Long id) {
    validarId(id, "producto");
    Producto producto = obtenerProducto(id);
    if (Boolean.TRUE.equals(producto.getEliminado())) {
      throw new IllegalStateException("Error: el producto ya se encuentra dado de baja.");
    }
    return productoRepository.cambiarEstadoEliminado(id, true);
  }

  public Producto restaurarProducto(Long id) {
    validarId(id, "producto");
    Producto producto = obtenerProductoPorId(id);
    if (!Boolean.TRUE.equals(producto.getEliminado())) {
      throw new IllegalStateException("Error: el producto ya se encuentra activo.");
    }
    if (producto.getCategoria() != null
        && Boolean.TRUE.equals(producto.getCategoria().getEliminado())) {
      throw new IllegalStateException(
          "Error: no se puede restaurar el producto porque su categoria se encuentra dada de baja.");
    }
    return productoRepository.cambiarEstadoEliminado(id, false);
  }

  public Producto obtenerProductoActivo(Long id) {
    validarId(id, "producto");
    Producto producto = obtenerProducto(id);
    if (Boolean.TRUE.equals(producto.getEliminado())) {
      throw new IllegalArgumentException("Error: no existe un producto activo con el ID indicado.");
    }
    return producto;
  }

  public List<Producto> buscarProductosActivosPorCategoria(Long categoriaId) {
    validarId(categoriaId, "categoria");
    obtenerCategoriaActiva(categoriaId);
    return productoRepository.buscarPorCategoria(categoriaId);
  }

  private Categoria obtenerCategoriaPorId(Long id) {
    return categoriaRepository
        .buscarPorId(id)
        .orElseThrow(
            () ->
                new IllegalArgumentException("Error: no existe una categoria con el ID indicado."));
  }

  private Producto obtenerProductoPorId(Long id) {
    return productoRepository
        .buscarPorId(id)
        .orElseThrow(
            () -> new IllegalArgumentException("Error: no existe un producto con el ID indicado."));
  }

  private Categoria obtenerCategoria(Long id) {
    validarId(id, "categoria");
    return categoriaRepository
        .buscarPorId(id)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Error: no existe una categoria activa con el ID indicado."));
  }

  private Producto obtenerProducto(Long id) {
    validarId(id, "producto");
    return productoRepository
        .buscarPorId(id)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Error: no existe un producto activo con el ID indicado."));
  }

  private Usuario obtenerUsuario(Long id) {
    validarId(id, "usuario");
    return usuarioRepository
        .buscarPorId(id)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Error: no existe un usuario activo con el ID indicado."));
  }

  private Categoria referenciaCategoria(Categoria categoria) {
    Categoria referencia = new Categoria();
    referencia.setId(categoria.getId());
    referencia.setNombre(categoria.getNombre());
    referencia.setDescripcion(categoria.getDescripcion());
    referencia.setEliminado(categoria.getEliminado());
    referencia.setCreatedAt(categoria.getCreatedAt());
    return referencia;
  }

  private String requerirTexto(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException("Error: " + campo + " es obligatorio.");
    }
    return valor.trim();
  }

  private void validarPrecio(Double precio) {
    if (precio == null || precio <= 0) {
      throw new IllegalArgumentException("Error: el precio debe ser mayor a 0.");
    }
  }

  private void validarStock(Integer stock) {
    if (stock == null || stock < 0) {
      throw new IllegalArgumentException("Error: el stock debe ser mayor o igual a 0.");
    }
  }

  private void validarRol(Rol rol) {
    if (rol == null) {
      throw new IllegalArgumentException("Error: el rol del usuario es obligatorio.");
    }
  }

  private void validarMailUnico(String mail, Long usuarioIdActual) {
    Optional<Usuario> usuarioExistente = usuarioRepository.buscarPorMail(mail);
    if (usuarioExistente.isPresent()
        && usuarioExistente.get().getMail() != null
        && usuarioExistente.get().getMail().equalsIgnoreCase(mail)
        && (usuarioIdActual == null || !usuarioIdActual.equals(usuarioExistente.get().getId()))) {
      throw new IllegalStateException("Error: ya existe un usuario activo con ese mail.");
    }
  }

  private void validarId(Long id, String entidad) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Error: el ID de " + entidad + " debe ser mayor a 0.");
    }
  }
}
