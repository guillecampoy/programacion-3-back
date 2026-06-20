package com.tp.jpa;

import static com.tp.jpa.util.ConsolaUtils.SEPARADOR;
import static com.tp.jpa.util.ConsolaUtils.imprimirError;
import static com.tp.jpa.util.ConsolaUtils.imprimirMensaje;
import static com.tp.jpa.util.ConsolaUtils.imprimirOpcion;
import static com.tp.jpa.util.ConsolaUtils.imprimirTabla;
import static com.tp.jpa.util.ConsolaUtils.imprimirTitulo;
import static com.tp.jpa.util.ConsolaUtils.prompt;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.seed.PersistenciaInicial;
import com.tp.jpa.service.CatalogoService;
import com.tp.jpa.util.EntradaValidada;
import com.tp.jpa.util.JPAUtil;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;

public class Main {
  private final Scanner scanner;
  private final EntradaValidada entrada;
  private final CatalogoService catalogoService;
  private final UsuarioRepository usuarioRepository;
  private final Supplier<PersistenciaInicial.ResumenPersistencia> regeneradorDatos;

  public Main(
      Scanner scanner,
      CategoriaRepository categoriaRepository,
      ProductoRepository productoRepository) {
    this(
        scanner,
        new CatalogoService(categoriaRepository, productoRepository),
        new UsuarioRepository(),
        PersistenciaInicial::regenerarBaseLocal);
  }

  public Main(
      Scanner scanner,
      CategoriaRepository categoriaRepository,
      ProductoRepository productoRepository,
      UsuarioRepository usuarioRepository) {
    this(
        scanner,
        new CatalogoService(categoriaRepository, productoRepository, usuarioRepository),
        usuarioRepository);
  }

  Main(Scanner scanner, CatalogoService catalogoService) {
    this(scanner, catalogoService, new UsuarioRepository(), PersistenciaInicial::regenerarBaseLocal);
  }

  Main(
      Scanner scanner,
      CatalogoService catalogoService,
      Supplier<PersistenciaInicial.ResumenPersistencia> regeneradorDatos) {
    this(scanner, catalogoService, new UsuarioRepository(), regeneradorDatos);
  }

  Main(Scanner scanner, CatalogoService catalogoService, UsuarioRepository usuarioRepository) {
    this(scanner, catalogoService, usuarioRepository, PersistenciaInicial::regenerarBaseLocal);
  }

  Main(
      Scanner scanner,
      CatalogoService catalogoService,
      UsuarioRepository usuarioRepository,
      Supplier<PersistenciaInicial.ResumenPersistencia> regeneradorDatos) {
    this.scanner = scanner;
    this.entrada = new EntradaValidada(scanner);
    this.catalogoService = catalogoService;
    this.usuarioRepository = usuarioRepository;
    this.regeneradorDatos = regeneradorDatos;
  }

  Main(
      EntradaValidada entrada,
      CategoriaRepository categoriaRepository,
      ProductoRepository productoRepository) {
    this.scanner = null;
    this.entrada = entrada;
    this.catalogoService = new CatalogoService(categoriaRepository, productoRepository);
    this.usuarioRepository = new UsuarioRepository();
    this.regeneradorDatos = PersistenciaInicial::regenerarBaseLocal;
  }

  public static void main(String[] args) {
    try (PersistenciaInicial ignored = PersistenciaInicial.inicializar()) {
      // La inicializacion crea la base local y aplica datos semilla solo cuando corresponde.
    }
    try (Scanner scanner = new Scanner(System.in)) {
      new Main(scanner, new CategoriaRepository(), new ProductoRepository(), new UsuarioRepository())
          .ejecutar();
    } finally {
      JPAUtil.close();
    }
  }

  private void ejecutar() {
    boolean salir = false;
    while (!salir) {
      mostrarMenuPrincipal();
      String opcion =
          entrada.leerOpcion(
              prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3", "4", "5", "6"));
      switch (opcion) {
        case "1" -> menuCategorias();
        case "2" -> menuProductos();
        case "3" -> menuReportes();
        case "4" -> regenerarDatos();
        case "5" -> menuUsuarios();
        case "6" -> menuPedidos();
        case "0" -> salir = true;
        default -> imprimirError("Opcion invalida.");
      }
    }
  }

  private void regenerarDatos() {
    imprimirTitulo("Regenerar datos");
    String confirmacion =
        entrada.leerOpcion(
            prompt(
                "Esta operacion borra la base local y vuelve a cargar la semilla. Confirma? (s/n)"),
            Set.of("s", "n"));
    if ("n".equals(confirmacion)) {
      imprimirMensaje("Operacion cancelada.");
      return;
    }

    try {
      PersistenciaInicial.ResumenPersistencia resumen = regeneradorDatos.get();
      imprimirMensaje("Base local regenerada correctamente.");
      imprimirMensaje(
          "Usuarios: "
              + resumen.usuarios()
              + " | Categorias: "
              + resumen.categorias()
              + " | Productos: "
              + resumen.productos()
              + " | Pedidos: "
              + resumen.pedidos());
    } catch (RuntimeException exception) {
      imprimirError("No se pudo regenerar la base local: " + exception.getMessage());
    }
  }

  private void menuReportes() {
    boolean volver = false;
    while (!volver) {
      mostrarMenuReportes();
      String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3"));
      switch (opcion) {
        case "1" -> productosPorCategoria();
        case "2" -> pedidosPorUsuario();
        case "3" -> pedidosPorEstado();
        case "0" -> volver = true;
        default -> imprimirError("Opcion invalida.");
      }
    }
  }

  private void menuUsuarios() {
    boolean volver = false;
    while (!volver) {
      mostrarMenuUsuarios();
      String opcion =
          entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3", "4"));
      switch (opcion) {
        case "1" -> altaUsuario();
        case "2" -> modificarUsuario();
        case "3" -> bajaUsuario();
        case "4" -> buscarUsuarioPorMail();
        case "0" -> volver = true;
        default -> imprimirError("Opcion invalida.");
      }
    }
  }

  private void menuPedidos() {
    boolean volver = false;
    while (!volver) {
      mostrarMenuPedidos();
      String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3"));
      switch (opcion) {
        case "1" -> altaPedido();
        case "2" -> cambiarEstadoPedido();
        case "3" -> bajaPedido();
        case "0" -> volver = true;
        default -> imprimirError("Opcion invalida.");
      }
    }
  }

  private void menuProductos() {
    boolean volver = false;
    while (!volver) {
      mostrarMenuProductos();
      String opcion =
          entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3", "4", "5"));
      switch (opcion) {
        case "1" -> altaProducto();
        case "2" -> modificarProducto();
        case "3" -> bajaProducto();
        case "4" -> listarProductosActivos();
        case "5" -> restaurarProducto();
        case "0" -> volver = true;
        default -> imprimirError("Opcion invalida.");
      }
    }
  }

  private void menuCategorias() {
    boolean volver = false;
    while (!volver) {
      mostrarMenuCategorias();
      String opcion =
          entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3", "4", "5"));
      switch (opcion) {
        case "1" -> altaCategoria();
        case "2" -> modificarCategoria();
        case "3" -> bajaCategoria();
        case "4" -> listarCategoriasActivas();
        case "5" -> restaurarCategoria();
        case "0" -> volver = true;
        default -> imprimirError("Opcion invalida.");
      }
    }
  }

  private void listarCategoriasActivas() {
    imprimirTitulo("Categorias activas");
    List<Categoria> categorias = catalogoService.listarCategoriasActivas();
    if (categorias.isEmpty()) {
      imprimirMensaje("No hay categorias activas para mostrar.");
      return;
    }
    imprimirCategorias(categorias);
  }

  private void listarProductosActivos() {
    imprimirTitulo("Productos activos");
    List<Producto> productos = catalogoService.listarProductosActivos();
    if (productos.isEmpty()) {
      imprimirMensaje("No hay productos activos para mostrar.");
      return;
    }
    imprimirProductos(productos);
  }

  private void restaurarCategoria() {
    imprimirTitulo("Revertir baja logica de categoria");
    List<Categoria> categorias = catalogoService.listarCategoriasEliminadas();
    if (categorias.isEmpty()) {
      imprimirMensaje("No hay categorias eliminadas para restaurar.");
      return;
    }

    imprimirCategorias(categorias);
    Set<Long> idsValidos =
        categorias.stream().map(Categoria::getId).collect(java.util.stream.Collectors.toSet());
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de categoria eliminada"),
            idsValidos::contains,
            "Error: no existe una categoria eliminada con el ID indicado.");

    try {
      Categoria categoria = catalogoService.restaurarCategoria(id);
      imprimirMensaje("Categoria restaurada correctamente: " + categoria.getNombre());
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
    }
  }

  private void restaurarProducto() {
    imprimirTitulo("Revertir baja logica de producto");
    List<Producto> productos = catalogoService.listarProductosEliminados();
    if (productos.isEmpty()) {
      imprimirMensaje("No hay productos eliminados para restaurar.");
      return;
    }

    imprimirProductos(productos);
    Set<Long> idsValidos =
        productos.stream().map(Producto::getId).collect(java.util.stream.Collectors.toSet());
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de producto eliminado"),
            idsValidos::contains,
            "Error: no existe un producto eliminado con el ID indicado.");

    try {
      Producto producto = catalogoService.restaurarProducto(id);
      imprimirMensaje("Producto restaurado correctamente: " + producto.getNombre());
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
    }
  }

  private void productosPorCategoria() {
    imprimirTitulo("Productos por categoria");
    List<Categoria> categorias = catalogoService.listarCategoriasActivas();
    if (categorias.isEmpty()) {
      imprimirMensaje("No hay categorias activas disponibles.");
      return;
    }

    imprimirCategorias(categorias);
    Set<Long> idsValidos =
        categorias.stream().map(Categoria::getId).collect(java.util.stream.Collectors.toSet());
    long categoriaId =
        entrada.leerLong(
            prompt("Seleccione ID de categoria"),
            idsValidos::contains,
            "Error: no existe una categoria activa con el ID indicado.");

    Categoria categoria;
    List<Producto> productos;
    try {
      categoria = catalogoService.obtenerCategoriaActiva(categoriaId);
      productos = catalogoService.buscarProductosActivosPorCategoria(categoriaId);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }
    if (productos.isEmpty()) {
      imprimirMensaje("No hay productos activos para la categoria seleccionada.");
      return;
    }

    System.out.println("Productos activos de la categoria " + categoria.getNombre() + ":");
    imprimirProductosReporte(productos);
  }

  private void pedidosPorUsuario() {
    imprimirTitulo("Pedidos por usuario");
    List<Usuario> usuarios = catalogoService.listarUsuariosActivos();
    if (usuarios.isEmpty()) {
      imprimirMensaje("No hay usuarios activos disponibles.");
      return;
    }

    imprimirUsuarios(usuarios);
    Set<Long> idsValidos =
        usuarios.stream().map(Usuario::getId).collect(java.util.stream.Collectors.toSet());
    long usuarioId =
        entrada.leerLong(
            prompt("Seleccione ID de usuario"),
            idsValidos::contains,
            "Error: no existe un usuario activo con el ID indicado.");

    Usuario usuario;
    List<Pedido> pedidos;
    try {
      usuario = catalogoService.obtenerUsuarioActivo(usuarioId);
      pedidos = catalogoService.listarPedidosActivosPorUsuario(usuarioId);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }
    if (pedidos.isEmpty()) {
      imprimirMensaje("No hay pedidos activos para el usuario seleccionado.");
      return;
    }

    System.out.println(
        "Pedidos activos del usuario " + usuario.getNombre() + " " + usuario.getApellido() + ":");
    imprimirPedidosReporte(pedidos);
  }

  private void pedidosPorEstado() {
    imprimirTitulo("Pedidos por estado");
    System.out.println("Estados disponibles:");
    imprimirOpcion("1", Estado.PENDIENTE.name());
    imprimirOpcion("2", Estado.CONFIRMADO.name());
    imprimirOpcion("3", Estado.TERMINADO.name());
    imprimirOpcion("4", Estado.CANCELADO.name());

    Estado estado =
        switch (entrada.leerOpcion(prompt("Seleccione estado"), Set.of("1", "2", "3", "4"))) {
          case "1" -> Estado.PENDIENTE;
          case "2" -> Estado.CONFIRMADO;
          case "3" -> Estado.TERMINADO;
          default -> Estado.CANCELADO;
        };

    List<Pedido> pedidos;
    try {
      pedidos = catalogoService.listarPedidosActivosPorEstado(estado);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }
    if (pedidos.isEmpty()) {
      imprimirMensaje("No hay pedidos activos con el estado seleccionado.");
      return;
    }

    System.out.println("Pedidos activos con estado " + estado.name() + ":");
    imprimirPedidosPorEstadoReporte(pedidos);
  }

  private void modificarProducto() {
    imprimirTitulo("Modificar producto");
    List<Producto> productos = catalogoService.listarProductosActivos();
    if (productos.isEmpty()) {
      imprimirMensaje("No hay productos activos para modificar.");
      return;
    }

    imprimirProductos(productos);
    Set<Long> idsValidos =
        productos.stream().map(Producto::getId).collect(java.util.stream.Collectors.toSet());
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de producto"),
            idsValidos::contains,
            "Error: no existe un producto activo con el ID indicado.");

    Producto producto;
    try {
      producto = catalogoService.obtenerProductoActivo(id);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }

    imprimirValoresActuales(
        new String[][] {
          {"Nombre", producto.getNombre()},
          {"Descripcion", producto.getDescripcion()},
          {"Precio", producto.getPrecio().toString()},
          {"Stock", String.valueOf(producto.getStock())},
          {"Categoria", producto.getCategoria() == null ? "" : producto.getCategoria().getNombre()}
        });

    List<Categoria> categoriasActivas = catalogoService.listarCategoriasActivas();
    if (categoriasActivas.isEmpty()) {
      imprimirMensaje(
          "No hay categorias activas disponibles para reasignar. Deje el campo vacio para conservar la categoria actual.");
    } else {
      imprimirMensaje("Categorias activas disponibles para reasignar:");
      imprimirCategorias(categoriasActivas);
    }
    Set<Long> idsCategoriasActivas =
        categoriasActivas.stream()
            .map(Categoria::getId)
            .collect(java.util.stream.Collectors.toSet());

    String nombre = leerLinea(prompt("Nuevo nombre (enter para conservar)"));
    String precioTexto = leerLinea(prompt("Nuevo precio (enter para conservar)"));
    String stockTexto = leerLinea(prompt("Nuevo stock (enter para conservar)"));
    String categoriaTexto = leerLinea(prompt("Nueva categoria (enter para conservar)"));

    Double nuevoPrecio = null;
    Integer nuevoStock = null;
    Long nuevaCategoriaId = null;
    if (!precioTexto.isBlank()) {
      try {
        nuevoPrecio = Double.parseDouble(precioTexto.trim());
      } catch (NumberFormatException exception) {
        imprimirError("Error: el precio debe ser mayor a 0.");
        return;
      }
      if (nuevoPrecio <= 0) {
        imprimirError("Error: el precio debe ser mayor a 0.");
        return;
      }
    }
    if (!stockTexto.isBlank()) {
      try {
        nuevoStock = Integer.parseInt(stockTexto.trim());
      } catch (NumberFormatException exception) {
        imprimirError("Error: el stock debe ser mayor o igual a 0.");
        return;
      }
      if (nuevoStock < 0) {
        imprimirError("Error: el stock debe ser mayor o igual a 0.");
        return;
      }
    }
    if (!categoriaTexto.isBlank()) {
      try {
        nuevaCategoriaId = Long.parseLong(categoriaTexto.trim());
      } catch (NumberFormatException exception) {
        imprimirError("Error: ingrese un ID numerico mayor a 0 para la categoria.");
        return;
      }
      if (nuevaCategoriaId <= 0) {
        imprimirError("Error: ingrese un ID numerico mayor a 0 para la categoria.");
        return;
      }
      if (!idsCategoriasActivas.contains(nuevaCategoriaId)) {
        imprimirError("Error: no existe una categoria activa con el ID indicado.");
        return;
      }
    }

    try {
      catalogoService.modificarProducto(id, nombre, nuevoPrecio, nuevoStock, nuevaCategoriaId);
      imprimirMensaje("Producto modificado correctamente.");
    } catch (RuntimeException exception) {
      imprimirError("No se modifico el producto: " + exception.getMessage());
    }
  }

  private void bajaProducto() {
    imprimirTitulo("Baja logica de producto");
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de producto"),
            valor -> valor > 0,
            "Error: ingrese un ID numerico mayor a 0.");

    try {
      Producto producto = catalogoService.bajaProducto(id);
      imprimirMensaje("Producto dado de baja correctamente: " + producto.getNombre());
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
    }
  }

  private void altaCategoria() {
    imprimirTitulo("Alta de categoria");
    String nombre =
        entrada.leerTexto(
            prompt("Nombre"),
            texto -> !texto.isBlank(),
            "Error: el nombre de la categoria es obligatorio. No se guardo la categoria.");
    String descripcion = leerLinea(prompt("Descripcion"));

    try {
      Categoria guardada = catalogoService.crearCategoria(nombre, descripcion);
      imprimirMensaje("Categoria creada correctamente. ID generado: " + guardada.getId());
    } catch (RuntimeException exception) {
      imprimirError("No se guardo la categoria: " + exception.getMessage());
    }
  }

  private void altaUsuario() {
    imprimirTitulo("Alta de usuario");
    String nombre = entrada.leerTextoNoVacio(prompt("Nombre"));
    String apellido = entrada.leerTextoNoVacio(prompt("Apellido"));
    String mail = entrada.leerTextoNoVacio(prompt("Mail"));
    String celular = entrada.leerTextoNoVacio(prompt("Celular"));
    String contrasenia = entrada.leerTextoNoVacio(prompt("Contrasenia"));

    System.out.println("Rol");
    imprimirOpcion("1", "ADMIN");
    imprimirOpcion("2", "USUARIO");
    String rolTexto = entrada.leerOpcion(prompt("Seleccione rol"), Set.of("1", "2"));
    Rol rol = "1".equals(rolTexto) ? Rol.ADMIN : Rol.USUARIO;

    try {
      var guardado =
          catalogoService.crearUsuario(nombre, apellido, mail, celular, contrasenia, rol);
      imprimirMensaje("Usuario creado correctamente. ID generado: " + guardado.getId());
    } catch (RuntimeException exception) {
      imprimirError("No se guardo el usuario: " + exception.getMessage());
    }
  }

  private void modificarUsuario() {
    imprimirTitulo("Modificar usuario");
    List<Usuario> usuarios = catalogoService.listarUsuariosActivos();
    if (usuarios.isEmpty()) {
      imprimirMensaje("No hay usuarios activos para modificar.");
      return;
    }

    imprimirUsuarios(usuarios);
    Set<Long> idsValidos =
        usuarios.stream().map(Usuario::getId).collect(java.util.stream.Collectors.toSet());
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de usuario"),
            idsValidos::contains,
            "Error: no existe un usuario activo con el ID indicado.");

    Usuario usuario;
    try {
      usuario = catalogoService.obtenerUsuarioActivo(id);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }

    imprimirValoresActuales(
        new String[][] {
          {"Nombre", usuario.getNombre()},
          {"Apellido", usuario.getApellido()},
          {"Mail", usuario.getMail()},
          {"Celular", usuario.getCelular()},
          {"Rol", usuario.getRol() == null ? "" : usuario.getRol().name()},
          {"Contrasenia", "********"}
        });

    String nombre = leerLinea(prompt("Nuevo nombre (enter para conservar)"));
    String apellido = leerLinea(prompt("Nuevo apellido (enter para conservar)"));
    String mail = leerLinea(prompt("Nuevo mail (enter para conservar)"));
    String celular = leerLinea(prompt("Nuevo celular (enter para conservar)"));
    String contrasenia = leerLinea(prompt("Nueva contrasenia (enter para conservar)"));

    System.out.println("Rol");
    imprimirOpcion("1", "ADMIN");
    imprimirOpcion("2", "USUARIO");
    String rolTexto = leerLinea(prompt("Nuevo rol (ADMIN/USUARIO, enter para conservar)"));
    Rol rol = null;
    if (!rolTexto.isBlank()) {
      if ("ADMIN".equalsIgnoreCase(rolTexto.trim())) {
        rol = Rol.ADMIN;
      } else if ("USUARIO".equalsIgnoreCase(rolTexto.trim())) {
        rol = Rol.USUARIO;
      } else {
        imprimirError("Error: el rol ingresado no es valido.");
        return;
      }
    }

    try {
      catalogoService.modificarUsuario(id, nombre, apellido, mail, celular, contrasenia, rol);
      imprimirMensaje("Usuario modificado correctamente.");
    } catch (RuntimeException exception) {
      imprimirError("No se modifico el usuario: " + exception.getMessage());
    }
  }

  private void bajaUsuario() {
    imprimirTitulo("Baja logica de usuario");
    List<Usuario> usuarios = catalogoService.listarUsuariosActivos();
    if (usuarios.isEmpty()) {
      imprimirMensaje("No hay usuarios activos para dar de baja.");
      return;
    }

    imprimirUsuarios(usuarios);
    Set<Long> idsValidos =
        usuarios.stream().map(Usuario::getId).collect(java.util.stream.Collectors.toSet());
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de usuario"),
            idsValidos::contains,
            "Error: no existe un usuario activo con el ID indicado.");

    try {
      Usuario dadoDeBaja = catalogoService.bajaUsuario(id);
      imprimirMensaje(
          "Usuario dado de baja correctamente: "
              + dadoDeBaja.getNombre()
              + " "
              + dadoDeBaja.getApellido());
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
    }
  }

  private void altaPedido() {
    imprimirTitulo("Alta de pedido");
    List<Usuario> usuarios = catalogoService.listarUsuariosActivos();
    if (usuarios.isEmpty()) {
      imprimirMensaje("No hay usuarios activos para crear pedidos.");
      return;
    }

    imprimirUsuarios(usuarios);
    Set<Long> idsUsuarios =
        usuarios.stream().map(Usuario::getId).collect(java.util.stream.Collectors.toSet());
    long usuarioId =
        entrada.leerLong(
            prompt("Ingrese ID de usuario"),
            idsUsuarios::contains,
            "Error: no existe un usuario activo con el ID indicado.");

    List<Producto> productos = catalogoService.listarProductosDisponiblesParaPedido();
    if (productos.isEmpty()) {
      imprimirMensaje("No hay productos disponibles para crear pedidos.");
      return;
    }

    imprimirProductosParaPedido(productos);
    Set<Long> idsProductos =
        productos.stream().map(Producto::getId).collect(java.util.stream.Collectors.toSet());

    System.out.println("Forma de pago");
    imprimirOpcion("1", "TARJETA");
    imprimirOpcion("2", "TRANSFERENCIA");
    imprimirOpcion("3", "EFECTIVO");
    FormaPago formaPago =
        switch (entrada.leerOpcion(prompt("Seleccione forma de pago"), Set.of("1", "2", "3"))) {
          case "1" -> FormaPago.TARJETA;
          case "2" -> FormaPago.TRANSFERENCIA;
          default -> FormaPago.EFECTIVO;
        };

    List<CatalogoService.LineaPedidoSolicitud> lineas = new java.util.ArrayList<>();
    boolean seguir = true;
    while (seguir) {
      long productoId =
          entrada.leerLong(
              prompt("Ingrese ID de producto (0 para finalizar)"),
              valor -> valor >= 0,
              "Error: ingrese un ID numerico mayor o igual a 0.");
      if (productoId == 0) {
        seguir = false;
        continue;
      }
      if (!idsProductos.contains(productoId)) {
        imprimirError("Error: no existe un producto disponible con el ID indicado.");
        continue;
      }
      int cantidad = entrada.leerEntero(prompt("Cantidad"), 1);
      lineas.add(new CatalogoService.LineaPedidoSolicitud(productoId, cantidad));
    }

    if (lineas.isEmpty()) {
      imprimirError("Error: el pedido debe tener al menos un detalle.");
      return;
    }

    try {
      Pedido pedido = catalogoService.crearPedido(usuarioId, formaPago, lineas);
      imprimirMensaje(
          "Pedido creado correctamente. ID generado: "
              + pedido.getId()
              + " | Total: "
              + pedido.getTotal()
              + " | Usuario: "
              + pedido.getUsuario().getNombre()
              + " "
              + pedido.getUsuario().getApellido());
      imprimirDetallePedido(pedido);
    } catch (RuntimeException exception) {
      imprimirError("No se pudo crear el pedido: " + exception.getMessage());
    }
  }

  private void cambiarEstadoPedido() {
    imprimirTitulo("Cambiar estado de pedido");
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de pedido"),
            valor -> valor > 0,
            "Error: ingrese un ID numerico mayor a 0.");

    Pedido pedido;
    try {
      pedido = catalogoService.obtenerPedidoActivo(id);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }

    imprimirMensaje("Estado actual: " + pedido.getEstado().name());
    imprimirValoresActuales(new String[][] {{"Estado", pedido.getEstado().name()}});
    System.out.println("Estados disponibles:");
    imprimirOpcion("1", Estado.PENDIENTE.name());
    imprimirOpcion("2", Estado.CONFIRMADO.name());
    imprimirOpcion("3", Estado.TERMINADO.name());
    imprimirOpcion("4", Estado.CANCELADO.name());

    Estado nuevoEstado =
        switch (entrada.leerOpcion(prompt("Seleccione nuevo estado"), Set.of("1", "2", "3", "4"))) {
          case "1" -> Estado.PENDIENTE;
          case "2" -> Estado.CONFIRMADO;
          case "3" -> Estado.TERMINADO;
          default -> Estado.CANCELADO;
        };

    try {
      Pedido actualizado = catalogoService.cambiarEstadoPedido(id, nuevoEstado);
      imprimirMensaje(
          "Pedido actualizado correctamente. ID: "
              + actualizado.getId()
              + " | Estado: "
              + actualizado.getEstado().name());
    } catch (RuntimeException exception) {
      imprimirError("No se pudo cambiar el estado del pedido: " + exception.getMessage());
    }
  }

  private void bajaPedido() {
    imprimirTitulo("Baja logica de pedido");
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de pedido"),
            valor -> valor > 0,
            "Error: ingrese un ID numerico mayor a 0.");

    Pedido pedido;
    try {
      pedido = catalogoService.obtenerPedidoActivo(id);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }

    try {
      Pedido dadoDeBaja = catalogoService.bajaPedido(id);
      imprimirMensaje(
          "Pedido dado de baja correctamente. ID: "
              + dadoDeBaja.getId()
              + " | Total: "
              + pedido.getTotal());
    } catch (RuntimeException exception) {
      imprimirError("No se pudo dar de baja el pedido: " + exception.getMessage());
    }
  }

  private void buscarUsuarioPorMail() {
    imprimirTitulo("Buscar usuario por mail");
    String mail = entrada.leerTextoNoVacio(prompt("Mail"));

    var usuario = usuarioRepository.buscarPorMail(mail);
    if (usuario.isEmpty()) {
      imprimirMensaje("No existe usuario activo con ese mail.");
      return;
    }

    imprimirUsuarios(List.of(usuario.orElseThrow()));
  }

  private void modificarCategoria() {
    imprimirTitulo("Modificar categoria");
    List<Categoria> categorias = catalogoService.listarCategoriasActivas();
    if (categorias.isEmpty()) {
      imprimirMensaje("No hay categorias activas para modificar.");
      return;
    }

    imprimirCategorias(categorias);
    Set<Long> idsValidos =
        categorias.stream().map(Categoria::getId).collect(java.util.stream.Collectors.toSet());
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de categoria"),
            idsValidos::contains,
            "Error: no existe una categoria activa con el ID indicado.");

    Categoria categoria;
    try {
      categoria = catalogoService.obtenerCategoriaActiva(id);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }

    imprimirValoresActuales(
        new String[][] {
          {"Nombre", categoria.getNombre()},
          {"Descripcion", categoria.getDescripcion()}
        });

    String nombre = leerLinea(prompt("Nuevo nombre (enter para conservar)"));
    String descripcion = leerLinea(prompt("Nueva descripcion (enter para conservar)"));

    try {
      catalogoService.modificarCategoria(id, nombre, descripcion);
      imprimirMensaje("Categoria modificada correctamente.");
    } catch (RuntimeException exception) {
      imprimirError("No se modifico la categoria: " + exception.getMessage());
    }
  }

  private void bajaCategoria() {
    imprimirTitulo("Baja logica de categoria");
    long id =
        entrada.leerLong(
            prompt("Ingrese ID de categoria"),
            valor -> valor > 0,
            "Error: ingrese un ID numerico mayor a 0.");

    try {
      CatalogoService.BajaCategoriaResultado resultado = catalogoService.bajaCategoria(id);
      imprimirMensaje("Categoria dada de baja correctamente: " + resultado.categoria().getNombre());
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
    }
  }

  private void altaProducto() {
    imprimirTitulo("Alta de producto");
    List<Categoria> categorias = catalogoService.listarCategoriasActivas();
    if (categorias.isEmpty()) {
      imprimirMensaje(
          "No hay categorias activas disponibles. Debe crear una categoria antes de cargar productos.");
      return;
    }

    imprimirCategorias(categorias);
    Set<Long> idsValidos =
        categorias.stream().map(Categoria::getId).collect(java.util.stream.Collectors.toSet());
    long categoriaId =
        entrada.leerLong(
            prompt("Seleccione ID de categoria"),
            idsValidos::contains,
            "Error: no existe una categoria activa con el ID indicado.");

    Categoria categoria;
    try {
      categoria = catalogoService.obtenerCategoriaActiva(categoriaId);
    } catch (RuntimeException exception) {
      imprimirError(exception.getMessage());
      return;
    }

    String nombre = entrada.leerTextoNoVacio(prompt("Nombre"));
    String descripcion = entrada.leerTextoNoVacio(prompt("Descripcion"));
    double precio = entrada.leerDecimal(prompt("Precio"), 0.01);
    int stock = entrada.leerEntero(prompt("Stock"), 0);
    String imagen = entrada.leerTextoNoVacio(prompt("Imagen"));
    boolean disponible = entrada.leerBooleano(prompt("Disponible (s/n)"));

    try {
      Producto guardado =
          catalogoService.crearProducto(
              categoriaId, nombre, descripcion, precio, stock, imagen, disponible);
      imprimirMensaje(
          "Producto creado correctamente. ID generado: "
              + guardado.getId()
              + " | Categoria: "
              + categoria.getNombre());
    } catch (RuntimeException exception) {
      imprimirError("No se guardo el producto: " + exception.getMessage());
    }
  }

  private String leerLinea(String prompt) {
    if (scanner == null) {
      return "";
    }
    System.out.print(prompt);
    System.out.flush();
    if (!scanner.hasNextLine()) {
      throw new IllegalStateException("No hay mas entrada disponible.");
    }
    return scanner.nextLine();
  }

  private void imprimirCategorias(List<Categoria> categorias) {
    imprimirTabla(
        new String[] {"ID", "Nombre", "Descripcion"},
        categorias.stream()
            .map(c -> new String[] {c.getId().toString(), c.getNombre(), c.getDescripcion()})
            .toList());
  }

  private void imprimirProductos(List<Producto> productos) {
    imprimirTabla(
        new String[] {"ID", "Nombre", "Descripcion", "Precio", "Stock", "Categoria"},
        productos.stream()
            .map(
                p ->
                    new String[] {
                      p.getId().toString(),
                      p.getNombre(),
                      p.getDescripcion(),
                      p.getPrecio().toString(),
                      String.valueOf(p.getStock()),
                      p.getCategoria() == null ? "" : p.getCategoria().getNombre()
                    })
            .toList());
  }

  private void imprimirProductosParaPedido(List<Producto> productos) {
    imprimirTabla(
        new String[] {"ID", "Nombre", "Precio", "Stock"},
        productos.stream()
            .map(
                producto ->
                    new String[] {
                      producto.getId().toString(),
                      producto.getNombre(),
                      producto.getPrecio().toString(),
                      String.valueOf(producto.getStock())
                    })
            .toList());
  }

  private void imprimirUsuarios(List<Usuario> usuarios) {
    imprimirTabla(
        new String[] {"ID", "Nombre", "Apellido", "Mail", "Celular", "Rol"},
        usuarios.stream()
            .map(
                usuario ->
                    new String[] {
                      usuario.getId().toString(),
                      usuario.getNombre(),
                      usuario.getApellido(),
                      usuario.getMail(),
                      usuario.getCelular(),
                      usuario.getRol() == null ? "" : usuario.getRol().name()
                    })
            .toList());
  }

  private void imprimirDetallePedido(Pedido pedido) {
    System.out.println("Detalle del pedido:");
    imprimirTabla(
        new String[] {"Producto", "Cantidad", "Subtotal"},
        pedido.getDetallePedidos().stream()
            .sorted(
                java.util.Comparator.comparing(
                    detalle -> detalle.getProducto().getId()))
            .map(
                detalle ->
                    new String[] {
                      detalle.getProducto().getNombre(),
                      String.valueOf(detalle.getCantidad()),
                      detalle.getSubtotal().toString()
                    })
            .toList());
  }

  private void imprimirProductosReporte(List<Producto> productos) {
    imprimirTabla(
        new String[] {"ID", "Nombre", "Descripcion", "Precio", "Stock"},
        productos.stream()
            .map(
                p ->
                    new String[] {
                      p.getId().toString(),
                      p.getNombre(),
                      p.getDescripcion(),
                      p.getPrecio().toString(),
                      String.valueOf(p.getStock())
                    })
            .toList());
  }

  private void imprimirPedidosReporte(List<Pedido> pedidos) {
    imprimirTabla(
        new String[] {"ID", "Fecha", "Estado", "Forma de pago", "Total"},
        pedidos.stream()
            .sorted(java.util.Comparator.comparing(Pedido::getId))
            .map(
                pedido ->
                    new String[] {
                      pedido.getId().toString(),
                      String.valueOf(pedido.getFecha()),
                      pedido.getEstado() == null ? "" : pedido.getEstado().name(),
                      pedido.getFormaPago() == null ? "" : pedido.getFormaPago().name(),
                      pedido.getTotal().toString()
                    })
            .toList());
  }

  private void imprimirPedidosPorEstadoReporte(List<Pedido> pedidos) {
    imprimirTabla(
        new String[] {"ID", "Fecha", "Usuario", "Total"},
        pedidos.stream()
            .sorted(java.util.Comparator.comparing(Pedido::getId))
            .map(
                pedido ->
                    new String[] {
                      pedido.getId().toString(),
                      String.valueOf(pedido.getFecha()),
                      pedido.getUsuario() == null ? "" : pedido.getUsuario().getNombre(),
                      pedido.getTotal().toString()
                    })
            .toList());
  }

  private void imprimirValoresActuales(String[][] filas) {
    System.out.println("Valores actuales:");
    imprimirTabla(new String[] {"Campo", "Valor"}, List.of(filas));
  }

  private void mostrarMenuPrincipal() {
    System.out.println();
    System.out.println(SEPARADOR);
    System.out.println("Sistema JPA - Categorias y Productos");
    System.out.println(SEPARADOR);
    imprimirOpcion("1", "Categorias");
    imprimirOpcion("2", "Productos");
    imprimirOpcion("3", "Reportes");
    imprimirOpcion("4", "Regenerar datos");
    imprimirOpcion("5", "Usuarios");
    imprimirOpcion("6", "Pedidos");
    imprimirOpcion("0", "Salir");
    System.out.println(SEPARADOR);
  }

  private void mostrarMenuCategorias() {
    System.out.println();
    System.out.println(SEPARADOR);
    System.out.println("Categorias");
    System.out.println(SEPARADOR);
    imprimirOpcion("1", "Alta de categoria");
    imprimirOpcion("2", "Modificar categoria");
    imprimirOpcion("3", "Baja logica de categoria");
    imprimirOpcion("4", "Listar categorias activas");
    imprimirOpcion("5", "Revertir baja logica");
    imprimirOpcion("0", "Volver");
    System.out.println(SEPARADOR);
  }

  private void mostrarMenuProductos() {
    System.out.println();
    System.out.println(SEPARADOR);
    System.out.println("Productos");
    System.out.println(SEPARADOR);
    imprimirOpcion("1", "Alta de producto");
    imprimirOpcion("2", "Modificar producto");
    imprimirOpcion("3", "Baja logica de producto");
    imprimirOpcion("4", "Listar productos activos");
    imprimirOpcion("5", "Revertir baja logica");
    imprimirOpcion("0", "Volver");
    System.out.println(SEPARADOR);
  }

  private void mostrarMenuReportes() {
    System.out.println();
    System.out.println(SEPARADOR);
    System.out.println("Reportes");
    System.out.println(SEPARADOR);
    imprimirOpcion("1", "Productos por categoria");
    imprimirOpcion("2", "Pedidos por usuario");
    imprimirOpcion("3", "Pedidos por estado");
    imprimirOpcion("0", "Volver");
    System.out.println(SEPARADOR);
  }

  private void mostrarMenuUsuarios() {
    System.out.println();
    System.out.println(SEPARADOR);
    System.out.println("Usuarios");
    System.out.println(SEPARADOR);
    imprimirOpcion("1", "Alta de usuario");
    imprimirOpcion("2", "Modificar usuario");
    imprimirOpcion("3", "Baja logica de usuario");
    imprimirOpcion("4", "Buscar usuario por mail");
    imprimirOpcion("0", "Volver");
    System.out.println(SEPARADOR);
  }

  private void mostrarMenuPedidos() {
    System.out.println();
    System.out.println(SEPARADOR);
    System.out.println("Pedidos");
    System.out.println(SEPARADOR);
    imprimirOpcion("1", "Alta de pedido");
    imprimirOpcion("2", "Cambiar estado de pedido");
    imprimirOpcion("3", "Baja logica de pedido");
    imprimirOpcion("0", "Volver");
    System.out.println(SEPARADOR);
  }
}
