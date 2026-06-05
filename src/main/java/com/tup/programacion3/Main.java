package com.tup.programacion3;

import com.tup.programacion3.entities.Pedido;
import com.tup.programacion3.entities.Producto;
import com.tup.programacion3.entities.Usuario;
import com.tup.programacion3.seed.DatosSemilla;
import com.tup.programacion3.seed.DatosSemillaFactory;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static DatosSemilla datos;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            System.out.print("Seleccione una opcion: ");
            if (!scanner.hasNextLine()) {
                System.out.println();
                System.out.println("No se recibio una opcion. Saliendo del programa.");
                break;
            }

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    instanciarDatos();
                    break;
                case "2":
                    mostrarProducto();
                    break;
                case "3":
                    listarProductos();
                    break;
                case "4":
                    mostrarPedidosDelUsuarioConMasPedidos();
                    break;
                case "5":
                    compararProductoConColeccion();
                    break;
                case "0":
                    salir = true;
                    System.out.println("Saliendo del programa.");
                    break;
                default:
                    System.out.println("Opcion invalida. Intente nuevamente.");
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("=== TP ToString - Colecciones ===");
        System.out.println("1. Instanciar datos");
        System.out.println("2. Mostrar un producto usando toString()");
        System.out.println("3. Listar productos cargados");
        System.out.println("4. Mostrar pedidos del usuario con mas pedidos");
        System.out.println("5. Comparar producto nuevo contra la coleccion");
        System.out.println("0. Salir");
    }

    private static void instanciarDatos() {
        datos = DatosSemillaFactory.crear();
        System.out.println("Datos instanciados correctamente.");
        System.out.println("Usuarios: " + datos.usuarios().size());
        System.out.println("Categorias: " + datos.categorias().size());
        System.out.println("Productos: " + datos.productos().size());
        System.out.println("Pedidos: " + datos.pedidos().size());
    }

    private static void mostrarProducto() {
        DatosSemilla datosSemilla = obtenerDatos();
        Producto producto = datosSemilla.productoParaMostrar();

        System.out.println("Producto seleccionado:");
        System.out.println(producto);
    }

    private static void listarProductos() {
        DatosSemilla datosSemilla = obtenerDatos();

        System.out.println("Productos cargados:");
        int numero = 1;
        for (Producto producto : datosSemilla.productos()) {
            System.out.println(numero + ". " + producto);
            numero++;
        }
    }

    private static void mostrarPedidosDelUsuarioConMasPedidos() {
        DatosSemilla datosSemilla = obtenerDatos();
        Optional<Usuario> usuarioConMasPedidos = datosSemilla.usuarios().stream()
                .max((usuario1, usuario2) -> Integer.compare(
                        usuario1.getPedidos().size(),
                        usuario2.getPedidos().size()
                ));

        if (usuarioConMasPedidos.isEmpty()) {
            System.out.println("No hay usuarios cargados.");
            return;
        }

        Usuario usuario = usuarioConMasPedidos.get();
        System.out.println("Usuario con mas pedidos:");
        System.out.println(usuario);
        System.out.println("Pedidos:");
        for (Pedido pedido : usuario.getPedidos()) {
            System.out.println(pedido);
        }
    }

    private static void compararProductoConColeccion() {
        DatosSemilla datosSemilla = obtenerDatos();
        Producto productoNuevo = datosSemilla.productoParaComparar();

        System.out.println("Producto nuevo a comparar:");
        System.out.println(productoNuevo);
        System.out.println("Resultados contra la coleccion:");

        boolean encontrado = false;
        for (Producto producto : datosSemilla.productos()) {
            boolean sonIguales = productoNuevo.equals(producto);
            System.out.println("- " + sonIguales + " -> " + producto);
            if (sonIguales) {
                encontrado = true;
            }
        }

        System.out.println("La coleccion contiene un producto equivalente: " + encontrado);
        System.out.println("Resultado de Set.contains(productoNuevo): " + datosSemilla.productos().contains(productoNuevo));
    }

    private static DatosSemilla obtenerDatos() {
        if (datos == null) {
            datos = DatosSemillaFactory.crear();
            System.out.println("No habia datos cargados. Se instanciaron datos semilla automaticamente.");
            System.out.println();
        }
        return datos;
    }
}
