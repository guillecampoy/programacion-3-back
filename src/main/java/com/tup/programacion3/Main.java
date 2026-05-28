package com.tup.programacion3;

import java.util.Scanner;

public class Main {
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
        System.out.println("Pendiente: crear 2 usuarios, 3 pedidos, 3 categorias y 10 productos.");
    }

    private static void mostrarProducto() {
        System.out.println("Pendiente: mostrar un producto usando toString().");
    }

    private static void listarProductos() {
        System.out.println("Pendiente: mostrar el listado de productos cargados.");
    }

    private static void mostrarPedidosDelUsuarioConMasPedidos() {
        System.out.println("Pendiente: mostrar los pedidos del usuario que mas pedidos tenga.");
    }

    private static void compararProductoConColeccion() {
        System.out.println("Pendiente: comparar un producto nuevo contra la coleccion usando equals().");
    }
}
