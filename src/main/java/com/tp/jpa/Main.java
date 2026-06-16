package com.tp.jpa;

import ar.edu.tup.programacion3.entities.Categoria;
import ar.edu.tup.programacion3.utils.EntradaValidada;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.util.JPAUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import static ar.edu.tup.programacion3.utils.ConsolaUtils.SEPARADOR;
import static ar.edu.tup.programacion3.utils.ConsolaUtils.imprimirError;
import static ar.edu.tup.programacion3.utils.ConsolaUtils.imprimirMensaje;
import static ar.edu.tup.programacion3.utils.ConsolaUtils.imprimirOpcion;
import static ar.edu.tup.programacion3.utils.ConsolaUtils.imprimirTitulo;
import static ar.edu.tup.programacion3.utils.ConsolaUtils.prompt;

public class Main {
    private final Scanner scanner;
    private final EntradaValidada entrada;
    private final CategoriaRepository categoriaRepository;

    public Main(Scanner scanner, CategoriaRepository categoriaRepository) {
        this.scanner = scanner;
        this.entrada = new EntradaValidada(scanner);
        this.categoriaRepository = categoriaRepository;
    }

    Main(EntradaValidada entrada, CategoriaRepository categoriaRepository) {
        this.scanner = null;
        this.entrada = entrada;
        this.categoriaRepository = categoriaRepository;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new Main(scanner, new CategoriaRepository()).ejecutar();
        } finally {
            JPAUtil.close();
        }
    }

    private void ejecutar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1"));
            switch (opcion) {
                case "1" -> menuCategorias();
                case "0" -> salir = true;
                default -> imprimirError("Opcion invalida.");
            }
        }
    }

    private void menuCategorias() {
        boolean volver = false;
        while (!volver) {
            mostrarMenuCategorias();
            String opcion = entrada.leerOpcion(prompt("Seleccione una opcion"), Set.of("0", "1", "2", "3"));
            switch (opcion) {
                case "1" -> altaCategoria();
                case "2" -> modificarCategoria();
                case "3" -> bajaCategoria();
                case "0" -> volver = true;
                default -> imprimirError("Opcion invalida.");
            }
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
            Categoria categoria = new Categoria();
            categoria.setId(generarId());
            categoria.setNombre(nombre.trim());
            categoria.setDescripcion(descripcion.trim());
            categoria.setEliminado(false);
            categoria.setCreatedAt(LocalDateTime.now());

            Categoria guardada = categoriaRepository.guardar(categoria);
            imprimirMensaje("Categoria creada correctamente. ID generado: " + guardada.getId());
        } catch (RuntimeException exception) {
            imprimirError("No se guardo la categoria: " + exception.getMessage());
        }
    }

    private void modificarCategoria() {
        imprimirTitulo("Modificar categoria");
        List<Categoria> categorias = categoriaRepository.listarActivos();
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

        Categoria categoria = categoriaRepository.buscarPorId(id).orElse(null);
        if (categoria == null || Boolean.TRUE.equals(categoria.getEliminado())) {
            imprimirError("Error: no existe una categoria activa con el ID indicado.");
            return;
        }

        System.out.println("Valores actuales:");
        System.out.println("Nombre actual: " + categoria.getNombre());
        System.out.println("Descripcion actual: " + categoria.getDescripcion());

        String nombre = leerLinea(prompt("Nuevo nombre (enter para conservar)"));
        String descripcion = leerLinea(prompt("Nueva descripcion (enter para conservar)"));
        if (!nombre.isBlank()) {
            categoria.setNombre(nombre.trim());
        }
        if (!descripcion.isBlank()) {
            categoria.setDescripcion(descripcion.trim());
        }

        try {
            categoriaRepository.guardar(categoria);
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

        Categoria categoria = categoriaRepository.buscarPorId(id).orElse(null);
        if (categoria == null) {
            imprimirError("Error: no existe una categoria activa con el ID indicado.");
            return;
        }
        if (Boolean.TRUE.equals(categoria.getEliminado())) {
            imprimirError("Error: la categoria ya se encuentra dada de baja.");
            return;
        }

        String nombre = categoria.getNombre();
        if (categoriaRepository.eliminarLogico(id)) {
            imprimirMensaje("Categoria dada de baja correctamente: " + nombre);
        }
    }

    private String leerLinea(String prompt) {
        if (scanner == null) {
            return "";
        }
        System.out.print(prompt);
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

    private long generarId() {
        return System.currentTimeMillis();
    }

    private void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println(SEPARADOR);
        System.out.println("Sistema JPA - Categorias y Productos");
        System.out.println(SEPARADOR);
        imprimirOpcion("1", "Categorias");
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
        imprimirOpcion("0", "Volver");
        System.out.println(SEPARADOR);
    }
}
