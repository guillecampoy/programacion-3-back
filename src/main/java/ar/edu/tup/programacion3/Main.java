package ar.edu.tup.programacion3;

import ar.edu.tup.programacion3.utils.EntradaValidada;
import ar.edu.tup.programacion3.seed.PersistenciaInicial;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiFunction;

public class Main {
    private static final String JDBC_H2_LOCAL = "jdbc:h2:file:./data/jpa_db;AUTO_SERVER=TRUE";
    private static final String SEPARADOR_MENU = "------------------------------------------------------------";
    private static final String PREFIJO_MENSAJE = "  > ";
    private static final String PREFIJO_ERROR = "  ! ";
    private static final int CANTIDAD_PRODUCTOS_A_ACTUALIZAR = 2;
    private static final List<CampoProductoEditable> CAMPOS_PRODUCTO_EDITABLES = List.of(
            new CampoProductoEditable("1", "Nombre", Main::leerActualizacionNombre),
            new CampoProductoEditable("2", "Precio", Main::leerActualizacionPrecio),
            new CampoProductoEditable("3", "Descripcion", Main::leerActualizacionDescripcion),
            new CampoProductoEditable("4", "Stock", Main::leerActualizacionStock),
            new CampoProductoEditable("5", "Imagen", Main::leerActualizacionImagen),
            new CampoProductoEditable("6", "Disponible", Main::leerActualizacionDisponible),
            new CampoProductoEditable("7", "Categoria", Main::leerActualizacionCategoria)
    );

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            EntradaValidada entradaValidada = new EntradaValidada(scanner);
            PersistenciaInicial persistenciaInicial = PersistenciaInicial.inicializar();
            H2LocalConsole h2LocalConsole = H2LocalConsole.iniciar();

            mostrarEstado(persistenciaInicial, h2LocalConsole);
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                String opcion;
                try {
                    opcion = entradaValidada.leerOpcion(prompt("Seleccione opcion"), Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8"));
                } catch (IllegalStateException exception) {
                    break;
                }

                switch (opcion) {
                    case "1" -> mostrarEstado(persistenciaInicial, h2LocalConsole);
                    case "2" -> {
                        h2LocalConsole.close();
                        persistenciaInicial.close();
                        PersistenciaInicial.borrarBaseLocal();
                        persistenciaInicial = PersistenciaInicial.inicializar();
                        h2LocalConsole = H2LocalConsole.iniciar();
                        imprimirMensaje("Base local borrada y semilla persistida nuevamente.");
                        mostrarEstado(persistenciaInicial, h2LocalConsole);
                    }
                    case "3" -> actualizarDosProductos(persistenciaInicial, entradaValidada);
                    case "4" -> buscarUsuarioPorId(persistenciaInicial, entradaValidada);
                    case "5" -> buscarUsuarioPorMail(persistenciaInicial, entradaValidada);
                    case "6" -> borrarProducto(persistenciaInicial, entradaValidada);
                    case "7" -> listarProductosEliminados(persistenciaInicial);
                    case "8" -> restaurarProducto(persistenciaInicial, entradaValidada);
                    case "0" -> salir = true;
                    default -> throw new IllegalStateException("Opcion no contemplada: " + opcion);
                }
            }

            h2LocalConsole.close();
            persistenciaInicial.close();
        }
    }

    private static void mostrarEstado(PersistenciaInicial persistenciaInicial, H2LocalConsole h2LocalConsole) {
        PersistenciaInicial.ResumenPersistencia resumen = persistenciaInicial.contarDatos();

        imprimirTitulo("ESTADO GENERAL");
        imprimirDato("BD local existente al iniciar", persistenciaInicial.isBaseLocalExistia() ? "si" : "no");
        imprimirDato("Datos iniciales persistidos", persistenciaInicial.isDatosInicialesPersistidos() ? "si" : "no");
        imprimirDato("Usuarios", resumen.usuarios());
        imprimirDato("Categorias", resumen.categorias());
        imprimirDato("Productos activos", resumen.productosActivos());
        imprimirDato("Productos eliminados", resumen.productosEliminados());
        imprimirDato("Pedidos", resumen.pedidos());
        imprimirDato("Consola H2", h2LocalConsole.getUrl());
        imprimirDato("JDBC URL", JDBC_H2_LOCAL);
        imprimirDato("Usuario", "sa");
        imprimirDato("Password", "<vacio>");
        System.out.println(SEPARADOR_MENU);
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println(SEPARADOR_MENU);
        System.out.println("MENU PRINCIPAL");
        System.out.println(SEPARADOR_MENU);
        imprimirOpcionMenu("1", "Mostrar estado");
        imprimirOpcionMenu("2", "Borrar base local y reinstanciar semilla");
        imprimirOpcionMenu("3", "Actualizar " + CANTIDAD_PRODUCTOS_A_ACTUALIZAR + " productos");
        imprimirOpcionMenu("4", "Buscar usuario por id");
        imprimirOpcionMenu("5", "Buscar usuario por mail parcial");
        imprimirOpcionMenu("6", "Borrar 1 producto");
        imprimirOpcionMenu("7", "Listar productos con borrado logico");
        imprimirOpcionMenu("8", "Revertir borrado logico de producto");
        System.out.println(SEPARADOR_MENU);
        imprimirOpcionMenu("0", "Salir");
        System.out.println(SEPARADOR_MENU);
    }

    private static void imprimirOpcionMenu(String opcion, String descripcion) {
        System.out.printf("  %s) %-50s%n", opcion, descripcion);
    }

    private static void imprimirTitulo(String titulo) {
        System.out.println();
        System.out.println(SEPARADOR_MENU);
        System.out.println(titulo);
        System.out.println(SEPARADOR_MENU);
    }

    private static void imprimirSubtitulo(String titulo) {
        System.out.println();
        System.out.println(titulo);
        System.out.println(SEPARADOR_MENU);
    }

    private static void imprimirDato(String etiqueta, Object valor) {
        System.out.printf("  %-35s %s%n", etiqueta + ":", valor);
    }

    private static void imprimirMensaje(String mensaje) {
        System.out.println(PREFIJO_MENSAJE + mensaje);
    }

    private static void imprimirError(String mensaje) {
        System.out.println(PREFIJO_ERROR + mensaje);
    }

    private static String prompt(String etiqueta) {
        return PREFIJO_MENSAJE + etiqueta + ": ";
    }

    private static void buscarUsuarioPorId(
            PersistenciaInicial persistenciaInicial,
            EntradaValidada entradaValidada
    ) {
        imprimirTitulo("BUSQUEDA DE USUARIO POR ID");
        long usuarioId = entradaValidada.leerLong(
                prompt("Id de usuario"),
                id -> id > 0,
                "Ingrese un id numerico mayor a 0."
        );
        mostrarResultadoUsuario(persistenciaInicial.buscarUsuarioPorId(usuarioId));
    }

    private static void buscarUsuarioPorMail(
            PersistenciaInicial persistenciaInicial,
            EntradaValidada entradaValidada
    ) {
        imprimirTitulo("BUSQUEDA DE USUARIO POR MAIL");
        String mailParcial = entradaValidada.leerTexto(
                prompt("Texto a buscar en mail"),
                entrada -> !entrada.isBlank() && !entrada.matches(".*\\s+.*"),
                "Ingrese un texto no vacio y sin espacios."
        );
        mostrarResultadoUsuarios(persistenciaInicial.buscarUsuariosPorMail(mailParcial));
    }

    private static void mostrarResultadoUsuario(Optional<PersistenciaInicial.UsuarioResumen> usuario) {
        if (usuario.isEmpty()) {
            imprimirMensaje("Usuario no encontrado.");
            return;
        }
        imprimirSubtitulo("RESULTADO");
        imprimirUsuario(usuario.get());
    }

    private static void mostrarResultadoUsuarios(List<PersistenciaInicial.UsuarioResumen> usuarios) {
        if (usuarios.isEmpty()) {
            imprimirMensaje("No se encontraron usuarios.");
            return;
        }

        imprimirSubtitulo("USUARIOS ENCONTRADOS");
        mostrarUsuarios(usuarios);
    }

    private static void borrarProducto(
            PersistenciaInicial persistenciaInicial,
            EntradaValidada entradaValidada
    ) {
        List<PersistenciaInicial.ProductoResumen> productos = persistenciaInicial.listarProductos();
        if (productos.isEmpty()) {
            imprimirMensaje("No hay productos activos para borrar.");
            return;
        }

        imprimirTitulo("BORRADO LOGICO DE PRODUCTO");
        Set<Long> idsValidos = new HashSet<>();
        productos.forEach(producto -> idsValidos.add(producto.id()));
        mostrarProductos(productos);

        long productoId = entradaValidada.leerLong(
                prompt("Id de producto a borrar"),
                idsValidos::contains,
                "Ingrese un id de producto activo existente."
        );
        PersistenciaInicial.ProductoResumen productoBorrado = persistenciaInicial.borrarProducto(productoId);

        imprimirSubtitulo("PRODUCTO BORRADO LOGICAMENTE");
        imprimirProducto(productoBorrado);
    }

    private static void listarProductosEliminados(PersistenciaInicial persistenciaInicial) {
        List<PersistenciaInicial.ProductoResumen> productosEliminados = persistenciaInicial.listarProductosEliminados();
        if (productosEliminados.isEmpty()) {
            imprimirMensaje("No hay productos con borrado logico.");
            return;
        }

        imprimirTitulo("PRODUCTOS CON BORRADO LOGICO");
        mostrarProductos(productosEliminados);
    }

    private static void restaurarProducto(
            PersistenciaInicial persistenciaInicial,
            EntradaValidada entradaValidada
    ) {
        List<PersistenciaInicial.ProductoResumen> productosEliminados = persistenciaInicial.listarProductosEliminados();
        if (productosEliminados.isEmpty()) {
            imprimirMensaje("No hay productos con borrado logico para restaurar.");
            return;
        }

        imprimirTitulo("REVERTIR BORRADO LOGICO");
        Set<Long> idsValidos = new HashSet<>();
        productosEliminados.forEach(producto -> idsValidos.add(producto.id()));
        mostrarProductos(productosEliminados);

        long productoId = entradaValidada.leerLong(
                prompt("Id de producto a restaurar"),
                idsValidos::contains,
                "Ingrese un id de producto eliminado existente."
        );
        PersistenciaInicial.ProductoResumen productoRestaurado = persistenciaInicial.restaurarProducto(productoId);

        imprimirSubtitulo("PRODUCTO RESTAURADO");
        imprimirProducto(productoRestaurado);
    }

    private static void actualizarDosProductos(
            PersistenciaInicial persistenciaInicial,
            EntradaValidada entradaValidada
    ) {
        List<PersistenciaInicial.ProductoResumen> productos = persistenciaInicial.listarProductos();
        if (productos.size() < CANTIDAD_PRODUCTOS_A_ACTUALIZAR) {
            imprimirError("Debe haber al menos " + CANTIDAD_PRODUCTOS_A_ACTUALIZAR + " productos para ejecutar esta opcion.");
            return;
        }

        imprimirTitulo("ACTUALIZACION DE PRODUCTOS");
        Set<Long> idsValidos = new HashSet<>();
        productos.forEach(producto -> idsValidos.add(producto.id()));

        Set<Long> idsActualizados = new HashSet<>();
        for (int i = 1; i <= CANTIDAD_PRODUCTOS_A_ACTUALIZAR; i++) {
            imprimirSubtitulo("PRODUCTO " + i + " DE " + CANTIDAD_PRODUCTOS_A_ACTUALIZAR);
            mostrarProductos(productos);

            long productoId = entradaValidada.leerLong(
                    prompt("Id de producto"),
                    id -> idsValidos.contains(id) && !idsActualizados.contains(id),
                    "Ingrese un id de producto existente y no repetido."
            );
            PersistenciaInicial.ProductoResumen productoSeleccionado = buscarProducto(productos, productoId);
            imprimirSubtitulo("PRODUCTO SELECCIONADO");
            imprimirProducto(productoSeleccionado);

            CampoProductoEditable campo = leerCampoProductoEditable(entradaValidada);
            PersistenciaInicial.ProductoActualizacion actualizacion = campo.leerActualizacion(
                    entradaValidada,
                    new ContextoActualizacion(persistenciaInicial, productoSeleccionado)
            );
            PersistenciaInicial.ProductoResumen productoActualizado = persistenciaInicial.actualizarProducto(actualizacion);
            idsActualizados.add(productoId);
            productos = persistenciaInicial.listarProductos();

            imprimirSubtitulo("PRODUCTO ACTUALIZADO");
            imprimirProducto(productoActualizado);
        }
    }

    private static CampoProductoEditable leerCampoProductoEditable(EntradaValidada entradaValidada) {
        imprimirSubtitulo("CAMPO A EDITAR");
        CAMPOS_PRODUCTO_EDITABLES.forEach(campo ->
                imprimirOpcionMenu(campo.opcion(), campo.nombre())
        );
        Set<String> opciones = new HashSet<>();
        CAMPOS_PRODUCTO_EDITABLES.forEach(campo -> opciones.add(campo.opcion()));

        String opcion = entradaValidada.leerOpcion(prompt("Campo"), opciones);
        return CAMPOS_PRODUCTO_EDITABLES.stream()
                .filter(campo -> campo.opcion().equals(opcion))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Campo no contemplado: " + opcion));
    }

    private static PersistenciaInicial.ProductoResumen buscarProducto(
            List<PersistenciaInicial.ProductoResumen> productos,
            long productoId
    ) {
        return productos.stream()
                .filter(producto -> producto.id() == productoId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionNombre(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        imprimirDato("Valor actual", contexto.producto().nombre());
        return PersistenciaInicial.ProductoActualizacion.nombre(
                contexto.producto().id(),
                entradaValidada.leerTextoNoVacio(prompt("Nuevo nombre"))
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionPrecio(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        imprimirDato("Valor actual", contexto.producto().precio());
        return PersistenciaInicial.ProductoActualizacion.precio(
                contexto.producto().id(),
                entradaValidada.leerDecimal(prompt("Nuevo precio"), 0.01)
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionDescripcion(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        imprimirDato("Valor actual", contexto.producto().descripcion());
        return PersistenciaInicial.ProductoActualizacion.descripcion(
                contexto.producto().id(),
                entradaValidada.leerTextoNoVacio(prompt("Nueva descripcion"))
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionStock(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        imprimirDato("Valor actual", contexto.producto().stock());
        return PersistenciaInicial.ProductoActualizacion.stock(
                contexto.producto().id(),
                entradaValidada.leerEntero(prompt("Nuevo stock"), 0)
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionImagen(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        imprimirDato("Valor actual", contexto.producto().imagen());
        return PersistenciaInicial.ProductoActualizacion.imagen(
                contexto.producto().id(),
                entradaValidada.leerTextoNoVacio(prompt("Nueva imagen"))
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionDisponible(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        imprimirDato("Valor actual", Boolean.TRUE.equals(contexto.producto().disponible()) ? "si" : "no");
        return PersistenciaInicial.ProductoActualizacion.disponible(
                contexto.producto().id(),
                entradaValidada.leerBooleano(prompt("Disponible (si/no)"))
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionCategoria(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        List<PersistenciaInicial.CategoriaResumen> categorias = contexto.persistenciaInicial().listarCategorias();
        Set<Long> idsCategorias = new HashSet<>();
        imprimirSubtitulo("CATEGORIAS DISPONIBLES");
        categorias.forEach(categoria -> {
            idsCategorias.add(categoria.id());
            imprimirOpcionMenu(String.valueOf(categoria.id()), categoria.nombre());
        });

        imprimirDato("Valor actual", contexto.producto().categoria());
        long categoriaId = entradaValidada.leerLong(
                prompt("Nueva categoria id"),
                idsCategorias::contains,
                "Ingrese un id de categoria existente."
        );
        return PersistenciaInicial.ProductoActualizacion.categoria(contexto.producto().id(), categoriaId);
    }

    private static void mostrarProductos(List<PersistenciaInicial.ProductoResumen> productos) {
        productos.forEach(Main::imprimirProducto);
    }

    private static void mostrarUsuarios(List<PersistenciaInicial.UsuarioResumen> usuarios) {
        usuarios.forEach(Main::imprimirUsuario);
    }

    private static void imprimirProducto(PersistenciaInicial.ProductoResumen producto) {
        System.out.println("  - " + formatearProducto(producto));
    }

    private static void imprimirUsuario(PersistenciaInicial.UsuarioResumen usuario) {
        System.out.println("  - " + formatearUsuario(usuario));
    }

    private static String formatearProducto(PersistenciaInicial.ProductoResumen producto) {
        return producto.id()
                + " - "
                + producto.nombre()
                + " | precio: "
                + producto.precio()
                + " | descripcion: "
                + producto.descripcion()
                + " | stock: "
                + producto.stock()
                + " | imagen: "
                + producto.imagen()
                + " | disponible: "
                + (Boolean.TRUE.equals(producto.disponible()) ? "si" : "no")
                + " | eliminado: "
                + (Boolean.TRUE.equals(producto.eliminado()) ? "si" : "no")
                + " | categoria: "
                + producto.categoria();
    }

    private static String formatearUsuario(PersistenciaInicial.UsuarioResumen usuario) {
        return usuario.id()
                + " - "
                + usuario.nombre()
                + " "
                + usuario.apellido()
                + " | mail: "
                + usuario.mail()
                + " | celular: "
                + usuario.celular()
                + " | rol: "
                + usuario.rol();
    }

    private record CampoProductoEditable(
            String opcion,
            String nombre,
            BiFunction<EntradaValidada, ContextoActualizacion, PersistenciaInicial.ProductoActualizacion> lector
    ) {
        private PersistenciaInicial.ProductoActualizacion leerActualizacion(
                EntradaValidada entradaValidada,
                ContextoActualizacion contexto
        ) {
            return lector.apply(entradaValidada, contexto);
        }
    }

    private record ContextoActualizacion(
            PersistenciaInicial persistenciaInicial,
            PersistenciaInicial.ProductoResumen producto
    ) {
    }
}
