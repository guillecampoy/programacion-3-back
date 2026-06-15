package ar.edu.tup.programacion3;

import ar.edu.tup.programacion3.utils.EntradaValidada;
import ar.edu.tup.programacion3.seed.PersistenciaInicial;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiFunction;

public class Main {
    private static final String JDBC_H2_LOCAL = "jdbc:h2:file:./data/jpa_db;AUTO_SERVER=TRUE";
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
                    opcion = entradaValidada.leerOpcion("Opcion: ", Set.of("0", "1", "2", "3"));
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
                        System.out.println("Base local borrada y semilla persistida nuevamente.");
                        mostrarEstado(persistenciaInicial, h2LocalConsole);
                    }
                    case "3" -> actualizarDosProductos(persistenciaInicial, entradaValidada);
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

        System.out.println();
        System.out.println("=== TP JPA ===");
        System.out.println("BD local existente al iniciar: " + (persistenciaInicial.isBaseLocalExistia() ? "si" : "no"));
        System.out.println("Datos iniciales persistidos: " + (persistenciaInicial.isDatosInicialesPersistidos() ? "si" : "no"));
        System.out.println("Usuarios: " + resumen.usuarios());
        System.out.println("Categorias: " + resumen.categorias());
        System.out.println("Productos: " + resumen.productos());
        System.out.println("Pedidos: " + resumen.pedidos());
        System.out.println("Consola H2: " + h2LocalConsole.getUrl());
        System.out.println("JDBC URL: " + JDBC_H2_LOCAL);
        System.out.println("Usuario: sa");
        System.out.println("Password: <vacio>");
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("1 - Mostrar estado");
        System.out.println("2 - Borrar base local y reinstanciar semilla");
        System.out.println("3 - Actualizar " + CANTIDAD_PRODUCTOS_A_ACTUALIZAR + " productos");
        System.out.println("0 - Salir");
    }

    private static void actualizarDosProductos(
            PersistenciaInicial persistenciaInicial,
            EntradaValidada entradaValidada
    ) {
        List<PersistenciaInicial.ProductoResumen> productos = persistenciaInicial.listarProductos();
        if (productos.size() < CANTIDAD_PRODUCTOS_A_ACTUALIZAR) {
            System.out.println("Debe haber al menos " + CANTIDAD_PRODUCTOS_A_ACTUALIZAR + " productos para ejecutar esta opcion.");
            return;
        }

        Set<Long> idsValidos = new HashSet<>();
        productos.forEach(producto -> idsValidos.add(producto.id()));

        Set<Long> idsActualizados = new HashSet<>();
        for (int i = 1; i <= CANTIDAD_PRODUCTOS_A_ACTUALIZAR; i++) {
            System.out.println();
            System.out.println("Producto " + i + " de " + CANTIDAD_PRODUCTOS_A_ACTUALIZAR);
            mostrarProductos(productos);

            long productoId = entradaValidada.leerLong(
                    "Id de producto: ",
                    id -> idsValidos.contains(id) && !idsActualizados.contains(id),
                    "Ingrese un id de producto existente y no repetido."
            );
            PersistenciaInicial.ProductoResumen productoSeleccionado = buscarProducto(productos, productoId);
            System.out.println("Seleccionado: " + formatearProducto(productoSeleccionado));

            CampoProductoEditable campo = leerCampoProductoEditable(entradaValidada);
            PersistenciaInicial.ProductoActualizacion actualizacion = campo.leerActualizacion(
                    entradaValidada,
                    new ContextoActualizacion(persistenciaInicial, productoSeleccionado)
            );
            PersistenciaInicial.ProductoResumen productoActualizado = persistenciaInicial.actualizarProducto(actualizacion);
            idsActualizados.add(productoId);
            productos = persistenciaInicial.listarProductos();

            System.out.println("Producto actualizado: " + formatearProducto(productoActualizado));
        }
    }

    private static CampoProductoEditable leerCampoProductoEditable(EntradaValidada entradaValidada) {
        System.out.println("Campo a editar:");
        CAMPOS_PRODUCTO_EDITABLES.forEach(campo ->
                System.out.println(campo.opcion() + " - " + campo.nombre())
        );
        Set<String> opciones = new HashSet<>();
        CAMPOS_PRODUCTO_EDITABLES.forEach(campo -> opciones.add(campo.opcion()));

        String opcion = entradaValidada.leerOpcion("Campo: ", opciones);
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
        System.out.println("Actual: " + contexto.producto().nombre());
        return PersistenciaInicial.ProductoActualizacion.nombre(
                contexto.producto().id(),
                entradaValidada.leerTextoNoVacio("Nuevo nombre: ")
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionPrecio(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        System.out.println("Actual: " + contexto.producto().precio());
        return PersistenciaInicial.ProductoActualizacion.precio(
                contexto.producto().id(),
                entradaValidada.leerDecimal("Nuevo precio: ", 0.01)
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionDescripcion(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        System.out.println("Actual: " + contexto.producto().descripcion());
        return PersistenciaInicial.ProductoActualizacion.descripcion(
                contexto.producto().id(),
                entradaValidada.leerTextoNoVacio("Nueva descripcion: ")
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionStock(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        System.out.println("Actual: " + contexto.producto().stock());
        return PersistenciaInicial.ProductoActualizacion.stock(
                contexto.producto().id(),
                entradaValidada.leerEntero("Nuevo stock: ", 0)
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionImagen(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        System.out.println("Actual: " + contexto.producto().imagen());
        return PersistenciaInicial.ProductoActualizacion.imagen(
                contexto.producto().id(),
                entradaValidada.leerTextoNoVacio("Nueva imagen: ")
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionDisponible(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        System.out.println("Actual: " + (Boolean.TRUE.equals(contexto.producto().disponible()) ? "si" : "no"));
        return PersistenciaInicial.ProductoActualizacion.disponible(
                contexto.producto().id(),
                entradaValidada.leerBooleano("Disponible (si/no): ")
        );
    }

    private static PersistenciaInicial.ProductoActualizacion leerActualizacionCategoria(
            EntradaValidada entradaValidada,
            ContextoActualizacion contexto
    ) {
        List<PersistenciaInicial.CategoriaResumen> categorias = contexto.persistenciaInicial().listarCategorias();
        Set<Long> idsCategorias = new HashSet<>();
        categorias.forEach(categoria -> {
            idsCategorias.add(categoria.id());
            System.out.println(categoria.id() + " - " + categoria.nombre());
        });

        System.out.println("Actual: " + contexto.producto().categoria());
        long categoriaId = entradaValidada.leerLong(
                "Nueva categoria id: ",
                idsCategorias::contains,
                "Ingrese un id de categoria existente."
        );
        return PersistenciaInicial.ProductoActualizacion.categoria(contexto.producto().id(), categoriaId);
    }

    private static void mostrarProductos(List<PersistenciaInicial.ProductoResumen> productos) {
        productos.forEach(producto -> System.out.println(formatearProducto(producto)));
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
                + " | categoria: "
                + producto.categoria();
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
