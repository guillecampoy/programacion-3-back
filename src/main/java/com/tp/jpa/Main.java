package com.tp.jpa;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.util.EntradaValidada;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.service.CatalogoService;
import com.tp.jpa.util.JPAUtil;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

import static com.tp.jpa.util.ConsolaUtils.SEPARADOR;
import static com.tp.jpa.util.ConsolaUtils.imprimirError;
import static com.tp.jpa.util.ConsolaUtils.imprimirMensaje;
import static com.tp.jpa.util.ConsolaUtils.imprimirOpcion;
import static com.tp.jpa.util.ConsolaUtils.imprimirTitulo;
import static com.tp.jpa.util.ConsolaUtils.prompt;

public class Main {
    private final Scanner scanner;
    private final EntradaValidada entrada;
    private final CatalogoService catalogoService;

    public Main(Scanner scanner, CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this(scanner, new CatalogoService(categoriaRepository, productoRepository));
    }

    Main(Scanner scanner, CatalogoService catalogoService) {
        this.scanner = scanner;
        this.entrada = new EntradaValidada(scanner);
        this.catalogoService = catalogoService;
    }

    Main(EntradaValidada entrada, CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.scanner = null;
        this.entrada = entrada;
        this.catalogoService = new CatalogoService(categoriaRepository, productoRepository);
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new Main(scanner, new CategoriaRepository(), new ProductoRepository()).ejecutar();
        } finally {
            JPAUtil.close();
        }
    }

    private void ejecutar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3"));
            switch (opcion) {
                case "1" -> menuCategorias();
                case "2" -> menuProductos();
                case "3" -> menuReportes();
                case "0" -> salir = true;
                default -> imprimirError("Opcion invalida.");
            }
        }
    }

    private void menuReportes() {
        boolean volver = false;
        while (!volver) {
            mostrarMenuReportes();
            String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1"));
            switch (opcion) {
                case "1" -> productosPorCategoria();
                case "0" -> volver = true;
                default -> imprimirError("Opcion invalida.");
            }
        }
    }

    private void menuProductos() {
        boolean volver = false;
        while (!volver) {
            mostrarMenuProductos();
            String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3", "4"));
            switch (opcion) {
                case "1" -> altaProducto();
                case "2" -> modificarProducto();
                case "3" -> bajaProducto();
                case "4" -> listarProductosActivos();
                case "0" -> volver = true;
                default -> imprimirError("Opcion invalida.");
            }
        }
    }

    private void menuCategorias() {
        boolean volver = false;
        while (!volver) {
            mostrarMenuCategorias();
            String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3", "4"));
            switch (opcion) {
                case "1" -> altaCategoria();
                case "2" -> modificarCategoria();
                case "3" -> bajaCategoria();
                case "4" -> listarCategoriasActivas();
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
        categorias.forEach(this::imprimirCategoria);
    }

    private void listarProductosActivos() {
        imprimirTitulo("Productos activos");
        List<Producto> productos = catalogoService.listarProductosActivos();
        if (productos.isEmpty()) {
            imprimirMensaje("No hay productos activos para mostrar.");
            return;
        }
        productos.forEach(this::imprimirProducto);
    }

    private void productosPorCategoria() {
        imprimirTitulo("Productos por categoria");
        List<Categoria> categorias = catalogoService.listarCategoriasActivas();
        if (categorias.isEmpty()) {
            imprimirMensaje("No hay categorias activas disponibles.");
            return;
        }

        categorias.forEach(this::imprimirCategoria);
        Set<Long> idsValidos = categorias.stream()
                .map(Categoria::getId)
                .collect(java.util.stream.Collectors.toSet());
        long categoriaId = entrada.leerLong(
                prompt("Seleccione ID de categoria"),
                idsValidos::contains,
                "Error: no existe una categoria activa con el ID indicado."
        );

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
        productos.forEach(this::imprimirProductoReporte);
    }

    private void modificarProducto() {
        imprimirTitulo("Modificar producto");
        List<Producto> productos = catalogoService.listarProductosActivos();
        if (productos.isEmpty()) {
            imprimirMensaje("No hay productos activos para modificar.");
            return;
        }

        productos.forEach(this::imprimirProducto);
        Set<Long> idsValidos = productos.stream()
                .map(Producto::getId)
                .collect(java.util.stream.Collectors.toSet());
        long id = entrada.leerLong(
                prompt("Ingrese ID de producto"),
                idsValidos::contains,
                "Error: no existe un producto activo con el ID indicado."
        );

        Producto producto;
        try {
            producto = catalogoService.obtenerProductoActivo(id);
        } catch (RuntimeException exception) {
            imprimirError(exception.getMessage());
            return;
        }

        System.out.println("Valores actuales:");
        System.out.println("Nombre actual: " + producto.getNombre());
        System.out.println("Precio actual: " + producto.getPrecio());
        System.out.println("Stock actual: " + producto.getStock());

        String nombre = leerLinea(prompt("Nuevo nombre (enter para conservar)"));
        String precioTexto = leerLinea(prompt("Nuevo precio (enter para conservar)"));
        String stockTexto = leerLinea(prompt("Nuevo stock (enter para conservar)"));

        Double nuevoPrecio = null;
        Integer nuevoStock = null;
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

        try {
            catalogoService.modificarProducto(id, nombre, nuevoPrecio, nuevoStock);
            imprimirMensaje("Producto modificado correctamente.");
        } catch (RuntimeException exception) {
            imprimirError("No se modifico el producto: " + exception.getMessage());
        }
    }

    private void bajaProducto() {
        imprimirTitulo("Baja logica de producto");
        long id = entrada.leerLong(
                prompt("Ingrese ID de producto"),
                valor -> valor > 0,
                "Error: ingrese un ID numerico mayor a 0."
        );

        try {
            Producto producto = catalogoService.bajaProducto(id);
            imprimirMensaje("Producto dado de baja correctamente: " + producto.getNombre());
        } catch (RuntimeException exception) {
            imprimirError(exception.getMessage());
        }
    }

    private void altaCategoria() {
        imprimirTitulo("Alta de categoria");
        String nombre = entrada.leerTexto(
                prompt("Nombre"),
                texto -> !texto.isBlank(),
                "Error: el nombre de la categoria es obligatorio. No se guardo la categoria."
        );
        String descripcion = entrada.leerTexto(
                prompt("Descripcion"),
                texto -> !texto.isBlank(),
                "Error: la descripcion de la categoria es obligatoria para el modelo actual."
        );

        try {
            Categoria guardada = catalogoService.crearCategoria(nombre, descripcion);
            imprimirMensaje("Categoria creada correctamente. ID generado: " + guardada.getId());
        } catch (RuntimeException exception) {
            imprimirError("No se guardo la categoria: " + mensajePersistencia(exception));
        }
    }

    private void modificarCategoria() {
        imprimirTitulo("Modificar categoria");
        List<Categoria> categorias = catalogoService.listarCategoriasActivas();
        if (categorias.isEmpty()) {
            imprimirMensaje("No hay categorias activas para modificar.");
            return;
        }

        categorias.forEach(this::imprimirCategoria);
        Set<Long> idsValidos = categorias.stream()
                .map(Categoria::getId)
                .collect(java.util.stream.Collectors.toSet());
        long id = entrada.leerLong(
                prompt("Ingrese ID de categoria"),
                idsValidos::contains,
                "Error: no existe una categoria activa con el ID indicado."
        );

        Categoria categoria;
        try {
            categoria = catalogoService.obtenerCategoriaActiva(id);
        } catch (RuntimeException exception) {
            imprimirError(exception.getMessage());
            return;
        }

        System.out.println("Valores actuales:");
        System.out.println("Nombre actual: " + categoria.getNombre());
        System.out.println("Descripcion actual: " + categoria.getDescripcion());

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
        long id = entrada.leerLong(
                prompt("Ingrese ID de categoria"),
                valor -> valor > 0,
                "Error: ingrese un ID numerico mayor a 0."
        );

        try {
            Categoria categoria = catalogoService.bajaCategoria(id);
            imprimirMensaje("Categoria dada de baja correctamente: " + categoria.getNombre());
        } catch (RuntimeException exception) {
            imprimirError(exception.getMessage());
        }
    }

    private void altaProducto() {
        imprimirTitulo("Alta de producto");
        List<Categoria> categorias = catalogoService.listarCategoriasActivas();
        if (categorias.isEmpty()) {
            imprimirMensaje("No hay categorias activas disponibles. Debe crear una categoria antes de cargar productos.");
            return;
        }

        categorias.forEach(this::imprimirCategoria);
        Set<Long> idsValidos = categorias.stream()
                .map(Categoria::getId)
                .collect(java.util.stream.Collectors.toSet());
        long categoriaId = entrada.leerLong(
                prompt("Seleccione ID de categoria"),
                idsValidos::contains,
                "Error: no existe una categoria activa con el ID indicado."
        );

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

        try {
            Producto guardado = catalogoService.crearProducto(categoriaId, nombre, descripcion, precio, stock);
            imprimirMensaje("Producto creado correctamente. ID generado: "
                    + guardado.getId()
                    + " | Categoria: "
                    + categoria.getNombre());
        } catch (RuntimeException exception) {
            imprimirError("No se guardo el producto: " + mensajePersistencia(exception));
        }
    }

    private String mensajePersistencia(RuntimeException exception) {
        String mensaje = exception.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return "ocurrio un error de persistencia.";
        }
        if (mensaje.contains("NULL not allowed for column \"ID\"")
                || mensaje.contains("La columna \"ID\" no permite valores nulos")
                || mensaje.contains("insert into Categoria")
                || mensaje.contains("insert into Producto")) {
            return "la base local tiene un esquema anterior para IDs. "
                    + "Recree la base H2 local o ejecute los tests con H2 en memoria.";
        }
        return mensaje;
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

    private void imprimirCategoria(Categoria categoria) {
        System.out.println("ID: " + categoria.getId()
                + " | Nombre: " + categoria.getNombre()
                + " | Descripcion: " + categoria.getDescripcion());
    }

    private void imprimirProducto(Producto producto) {
        String categoria = producto.getCategoria() == null ? "" : producto.getCategoria().getNombre();
        System.out.println("ID: " + producto.getId()
                + " | Nombre: " + producto.getNombre()
                + " | Precio: " + producto.getPrecio()
                + " | Stock: " + producto.getStock()
                + " | Categoria: " + categoria);
    }

    private void imprimirProductoReporte(Producto producto) {
        System.out.println("ID: " + producto.getId()
                + " | Nombre: " + producto.getNombre()
                + " | Precio: " + producto.getPrecio()
                + " | Stock: " + producto.getStock());
    }

    private void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println(SEPARADOR);
        System.out.println("Sistema JPA - Categorias y Productos");
        System.out.println(SEPARADOR);
        imprimirOpcion("1", "Categorias");
        imprimirOpcion("2", "Productos");
        imprimirOpcion("3", "Reportes");
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
        imprimirOpcion("0", "Volver");
        System.out.println(SEPARADOR);
    }

    private void mostrarMenuReportes() {
        System.out.println();
        System.out.println(SEPARADOR);
        System.out.println("Reportes");
        System.out.println(SEPARADOR);
        imprimirOpcion("1", "Productos por categoria");
        imprimirOpcion("0", "Volver");
        System.out.println(SEPARADOR);
    }
}
