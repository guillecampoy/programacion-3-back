package com.tp.jpa.service;

import com.tp.jpa.dtos.CategoriaAltaDTO;
import com.tp.jpa.dtos.CategoriaModificacionDTO;
import com.tp.jpa.dtos.ProductoAltaDTO;
import com.tp.jpa.dtos.ProductoModificacionDTO;
import com.tp.jpa.dtos.UsuarioAltaDTO;
import com.tp.jpa.dtos.UsuarioModificacionDTO;
import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CatalogoService {
  private final CategoriaRepository categoriaRepository;
  private final ProductoRepository productoRepository;
  private final UsuarioRepository usuarioRepository;
  private final PedidoRepository pedidoRepository;

  public record BajaCategoriaResultado(Categoria categoria, List<Producto> productosDadosDeBaja) {}

  public record LineaPedidoSolicitud(Long productoId, int cantidad) {}

  public CatalogoService(
      CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
    this(categoriaRepository, productoRepository, new UsuarioRepository(), new PedidoRepository());
  }

  public CatalogoService(
      CategoriaRepository categoriaRepository,
      ProductoRepository productoRepository,
      UsuarioRepository usuarioRepository) {
    this(categoriaRepository, productoRepository, usuarioRepository, new PedidoRepository());
  }

  public CatalogoService(
      CategoriaRepository categoriaRepository,
      ProductoRepository productoRepository,
      UsuarioRepository usuarioRepository,
      PedidoRepository pedidoRepository) {
    this.categoriaRepository = categoriaRepository;
    this.productoRepository = productoRepository;
    this.usuarioRepository = usuarioRepository;
    this.pedidoRepository = pedidoRepository;
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

  public List<Producto> listarProductosDisponiblesParaPedido() {
    return productoRepository.listarActivos().stream()
        .filter(producto -> Boolean.TRUE.equals(producto.getDisponible()))
        .filter(producto -> producto.getStock() > 0)
        .sorted((producto1, producto2) -> Long.compare(producto1.getId(), producto2.getId()))
        .toList();
  }

  public List<Producto> listarProductosEliminados() {
    return productoRepository.listarEliminados();
  }

  public List<Usuario> listarUsuariosActivos() {
    return usuarioRepository.listarActivos();
  }

  public Pedido crearPedido(
      Long usuarioId, FormaPago formaPago, List<LineaPedidoSolicitud> lineasPedido) {
    validarId(usuarioId, "usuario");
    validarFormaPago(formaPago);
    if (lineasPedido == null || lineasPedido.isEmpty()) {
      throw new IllegalArgumentException("Error: el pedido debe tener al menos un detalle.");
    }

    EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
    try {
      entityManager.getTransaction().begin();

      Usuario usuario = entityManager.find(Usuario.class, usuarioId);
      if (usuario == null || Boolean.TRUE.equals(usuario.getEliminado())) {
        throw new IllegalArgumentException(
            "Error: no existe un usuario activo con el ID indicado.");
      }

      Pedido pedido = new Pedido();
      pedido.setFecha(LocalDate.now());
      pedido.setEstado(Estado.PENDIENTE);
      pedido.setFormaPago(formaPago);
      pedido.setUsuario(usuario);
      pedido.setTotal(0.0);
      pedido.setEliminado(false);
      pedido.setCreatedAt(LocalDateTime.now());

      for (LineaPedidoSolicitud lineaPedido : lineasPedido) {
        if (lineaPedido == null) {
          throw new IllegalArgumentException("Error: el pedido debe tener al menos un detalle.");
        }
        validarId(lineaPedido.productoId(), "producto");
        if (lineaPedido.cantidad() < 1) {
          throw new IllegalArgumentException("Error: la cantidad debe ser mayor o igual a 1.");
        }

        Producto producto = entityManager.find(Producto.class, lineaPedido.productoId());
        if (producto == null || Boolean.TRUE.equals(producto.getEliminado())) {
          throw new IllegalArgumentException(
              "Error: no existe un producto activo con el ID indicado.");
        }
        if (!Boolean.TRUE.equals(producto.getDisponible())) {
          throw new IllegalStateException(
              "Error: el producto no se encuentra disponible para pedidos.");
        }
        if (producto.getStock() < lineaPedido.cantidad()) {
          throw new IllegalStateException(
              "Error: stock insuficiente para el producto " + producto.getNombre() + ".");
        }

        producto.setStock(producto.getStock() - lineaPedido.cantidad());
        pedido.addDetallePedido(lineaPedido.cantidad(), producto);
      }

      pedido.calcularTotal();
      entityManager.persist(pedido);
      entityManager.getTransaction().commit();
      return pedido;
    } catch (RuntimeException exception) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  public Pedido obtenerPedidoActivo(Long id) {
    validarId(id, "pedido");
    Pedido pedido =
        pedidoRepository
            .buscarPorId(id)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Error: no existe un pedido activo con el ID indicado."));
    if (Boolean.TRUE.equals(pedido.getEliminado())) {
      throw new IllegalArgumentException("Error: no existe un pedido activo con el ID indicado.");
    }
    return pedido;
  }

  public Pedido cambiarEstadoPedido(Long id, Estado nuevoEstado) {
    validarId(id, "pedido");
    validarEstadoPedido(nuevoEstado);
    Pedido pedido = obtenerPedidoActivo(id);
    pedido.setEstado(nuevoEstado);
    return pedidoRepository.guardar(pedido);
  }

  public List<Pedido> listarPedidosActivosPorUsuario(Long usuarioId) {
    validarId(usuarioId, "usuario");
    obtenerUsuarioActivo(usuarioId);
    return pedidoRepository.buscarPorUsuario(usuarioId);
  }

  public List<Pedido> listarPedidosActivosPorEstado(Estado estado) {
    validarEstadoPedido(estado);
    return pedidoRepository.buscarPorEstado(estado);
  }

  public double totalFacturadoTerminados() {
    return pedidoRepository.buscarPorEstado(Estado.TERMINADO).stream()
        .mapToDouble(pedido -> pedido.getTotal() == null ? 0.0 : pedido.getTotal())
        .sum();
  }

  public Pedido bajaPedido(Long id) {
    validarId(id, "pedido");
    Pedido pedido = obtenerPedidoActivo(id);
    if (Boolean.TRUE.equals(pedido.getEliminado())) {
      throw new IllegalStateException("Error: el pedido ya se encuentra dado de baja.");
    }
    if (!pedidoRepository.eliminarLogico(id)) {
      throw new IllegalStateException("Error: no existe un pedido activo con el ID indicado.");
    }
    return pedido;
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
    return crearUsuario(new UsuarioAltaDTO(nombre, apellido, mail, celular, contrasenia, rol));
  }

  public Usuario crearUsuario(UsuarioAltaDTO dto) {
    validarDtoNoNulo(dto, "usuario");
    String nombreNormalizado = requerirTexto(dto.nombre(), "El nombre del usuario");
    String apellidoNormalizado = requerirTexto(dto.apellido(), "El apellido del usuario");
    String mailNormalizado = requerirTexto(dto.mail(), "El mail del usuario");
    String celularNormalizado = requerirTexto(dto.celular(), "El celular del usuario");
    String contraseniaNormalizada = requerirTexto(dto.contrasenia(), "La contrasenia del usuario");
    validarRol(dto.rol());
    validarMailUnico(mailNormalizado, null);

    Usuario usuario = new Usuario();
    usuario.setNombre(nombreNormalizado);
    usuario.setApellido(apellidoNormalizado);
    usuario.setMail(mailNormalizado);
    usuario.setCelular(celularNormalizado);
    usuario.setContrasenia(contraseniaNormalizada);
    usuario.setRol(dto.rol());
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
    return modificarUsuario(
        new UsuarioModificacionDTO(id, nombre, apellido, mail, celular, contrasenia, rol));
  }

  public Usuario modificarUsuario(UsuarioModificacionDTO dto) {
    validarDtoNoNulo(dto, "modificacion de usuario");
    validarId(dto.id(), "usuario");
    Usuario usuario = obtenerUsuarioActivo(dto.id());
    if (dto.nombre() != null && !dto.nombre().isBlank()) {
      usuario.setNombre(requerirTexto(dto.nombre(), "El nombre del usuario"));
    }
    if (dto.apellido() != null && !dto.apellido().isBlank()) {
      usuario.setApellido(requerirTexto(dto.apellido(), "El apellido del usuario"));
    }
    if (dto.mail() != null && !dto.mail().isBlank()) {
      String mailNormalizado = requerirTexto(dto.mail(), "El mail del usuario");
      if (!mailNormalizado.equalsIgnoreCase(usuario.getMail())) {
        validarMailUnico(mailNormalizado, dto.id());
      }
      usuario.setMail(mailNormalizado);
    }
    if (dto.celular() != null && !dto.celular().isBlank()) {
      usuario.setCelular(requerirTexto(dto.celular(), "El celular del usuario"));
    }
    if (dto.contrasenia() != null && !dto.contrasenia().isBlank()) {
      usuario.setContrasenia(requerirTexto(dto.contrasenia(), "La contrasenia del usuario"));
    }
    if (dto.rol() != null) {
      validarRol(dto.rol());
      usuario.setRol(dto.rol());
    }
    return usuarioRepository.guardar(usuario);
  }

  public Usuario bajaUsuario(Long id) {
    validarId(id, "usuario");
    Usuario usuario = obtenerUsuarioActivo(id);
    if (Boolean.TRUE.equals(usuario.getEliminado())) {
      throw new IllegalStateException("Error: el usuario ya se encuentra dado de baja.");
    }
    return usuarioRepository.cambiarEstadoEliminado(id, true);
  }

  public Categoria crearCategoria(String nombre, String descripcion) {
    return crearCategoria(new CategoriaAltaDTO(nombre, descripcion));
  }

  public Categoria crearCategoria(CategoriaAltaDTO dto) {
    validarDtoNoNulo(dto, "categoria");
    String nombreNormalizado = requerirTexto(dto.nombre(), "El nombre de la categoria");
    String descripcionNormalizada = dto.descripcion() == null ? "" : dto.descripcion().trim();

    Categoria categoria = new Categoria();
    categoria.setNombre(nombreNormalizado);
    categoria.setDescripcion(descripcionNormalizada);
    categoria.setEliminado(false);
    categoria.setCreatedAt(LocalDateTime.now());
    return categoriaRepository.guardar(categoria);
  }

  public Categoria modificarCategoria(Long id, String nombre, String descripcion) {
    return modificarCategoria(new CategoriaModificacionDTO(id, nombre, descripcion));
  }

  public Categoria modificarCategoria(CategoriaModificacionDTO dto) {
    validarDtoNoNulo(dto, "modificacion de categoria");
    validarId(dto.id(), "categoria");
    Categoria categoria = obtenerCategoriaActiva(dto.id());
    if (dto.nombre() != null && !dto.nombre().isBlank()) {
      categoria.setNombre(requerirTexto(dto.nombre(), "El nombre de la categoria"));
    }
    if (dto.descripcion() != null && !dto.descripcion().isBlank()) {
      categoria.setDescripcion(requerirTexto(dto.descripcion(), "La descripcion de la categoria"));
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
    return crearProducto(
        new ProductoAltaDTO(categoriaId, nombre, descripcion, precio, stock, imagen, disponible));
  }

  public Producto crearProducto(ProductoAltaDTO dto) {
    validarDtoNoNulo(dto, "producto");
    validarId(dto.categoriaId(), "categoria");
    String nombreNormalizado = requerirTexto(dto.nombre(), "El nombre del producto");
    String descripcionNormalizada = requerirTexto(dto.descripcion(), "La descripcion del producto");
    String imagenNormalizada = requerirTexto(dto.imagen(), "La imagen del producto");
    validarPrecio(dto.precio());
    validarStock(dto.stock());

    Categoria categoria = obtenerCategoriaActiva(dto.categoriaId());
    Producto producto = new Producto();
    producto.setNombre(nombreNormalizado);
    producto.setDescripcion(descripcionNormalizada);
    producto.setPrecio(dto.precio());
    producto.setStock(dto.stock());
    producto.setImagen(imagenNormalizada);
    producto.setDisponible(dto.disponible());
    producto.setEliminado(false);
    producto.setCreatedAt(LocalDateTime.now());
    producto.setCategoria(referenciaCategoria(categoria));
    return productoRepository.guardar(producto);
  }

  public Producto modificarProducto(
      Long id, String nombre, Double precio, Integer stock, Long categoriaId) {
    return modificarProducto(new ProductoModificacionDTO(id, nombre, precio, stock, categoriaId));
  }

  public Producto modificarProducto(ProductoModificacionDTO dto) {
    validarDtoNoNulo(dto, "modificacion de producto");
    validarId(dto.id(), "producto");
    if (dto.nombre() != null && !dto.nombre().isBlank()) {
      requerirTexto(dto.nombre(), "El nombre del producto");
    }
    if (dto.precio() != null) {
      validarPrecio(dto.precio());
    }
    if (dto.stock() != null) {
      validarStock(dto.stock());
    }
    Producto producto = obtenerProductoActivo(dto.id());
    Categoria nuevaCategoria = null;
    if (dto.categoriaId() != null) {
      nuevaCategoria = obtenerCategoriaActiva(dto.categoriaId());
    }
    if (dto.nombre() != null && !dto.nombre().isBlank()) {
      producto.setNombre(dto.nombre().trim());
    }
    if (dto.precio() != null) {
      producto.setPrecio(dto.precio());
    }
    if (dto.stock() != null) {
      producto.setStock(dto.stock());
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

  private void validarEstadoPedido(Estado estado) {
    if (estado == null) {
      throw new IllegalArgumentException("Error: el estado del pedido es obligatorio.");
    }
  }

  private void validarFormaPago(FormaPago formaPago) {
    if (formaPago == null) {
      throw new IllegalArgumentException("Error: la forma de pago es obligatoria.");
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

  private void validarDtoNoNulo(Object dto, String entidad) {
    if (dto == null) {
      throw new IllegalArgumentException("Error: el DTO de " + entidad + " no puede ser nulo.");
    }
  }
}
