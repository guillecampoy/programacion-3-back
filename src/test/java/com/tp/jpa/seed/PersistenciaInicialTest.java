package com.tp.jpa.seed;

import com.tp.jpa.seed.PersistenciaInicial;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaInicialTest {
    @TempDir
    private Path tempDir;

    @Test
    void creaBaseLocalYPersisteSemillaCuandoNoExiste() {
        ConfiguracionTemporal configuracion = crearConfiguracionTemporal();

        assertFalse(Files.exists(configuracion.archivoBase()));

        try (PersistenciaInicial persistenciaInicial = PersistenciaInicial.inicializar(
                configuracion.archivoBase(),
                configuracion.archivosBase(),
                configuracion.jdbcUrl()
        )) {
            PersistenciaInicial.ResumenPersistencia resumen = persistenciaInicial.contarDatos();

            assertFalse(persistenciaInicial.isBaseLocalExistia());
            assertTrue(persistenciaInicial.isDatosInicialesPersistidos());
            assertEquals(2L, resumen.usuarios());
            assertEquals(3L, resumen.categorias());
            assertEquals(10L, resumen.productos());
            assertEquals(10L, resumen.productosActivos());
            assertEquals(0L, resumen.productosEliminados());
            assertEquals(3L, resumen.pedidos());
        }

        assertTrue(Files.exists(configuracion.archivoBase()));
    }

    @Test
    void actualizaProductosPersistidos() {
        ConfiguracionTemporal configuracion = crearConfiguracionTemporal();

        try (PersistenciaInicial persistenciaInicial = PersistenciaInicial.inicializar(
                configuracion.archivoBase(),
                configuracion.archivosBase(),
                configuracion.jdbcUrl()
        )) {
            Long cafeId = buscarProductoPorNombre(persistenciaInicial, "Cafe molido").id();
            Long yerbaId = buscarProductoPorNombre(persistenciaInicial, "Yerba mate").id();
            Long bebidasId = buscarCategoriaPorNombre(persistenciaInicial, "Bebidas").id();

            PersistenciaInicial.ProductoResumen cafe = persistenciaInicial.actualizarProducto(
                    PersistenciaInicial.ProductoActualizacion.nombre(cafeId, "Cafe actualizado")
            );
            PersistenciaInicial.ProductoResumen yerba = persistenciaInicial.actualizarProducto(
                    PersistenciaInicial.ProductoActualizacion.categoria(yerbaId, bebidasId)
            );

            assertEquals("Cafe actualizado", cafe.nombre());
            assertEquals(bebidasId, yerba.categoriaId());
            assertEquals("Bebidas", yerba.categoria());
        }
    }

    @Test
    void buscaUsuarioPorIdYPorMailParcial() {
        ConfiguracionTemporal configuracion = crearConfiguracionTemporal();

        try (PersistenciaInicial persistenciaInicial = PersistenciaInicial.inicializar(
                configuracion.archivoBase(),
                configuracion.archivosBase(),
                configuracion.jdbcUrl()
        )) {
            PersistenciaInicial.UsuarioResumen ana = persistenciaInicial.buscarUsuariosPorMail("anagarcia")
                    .get(0);
            PersistenciaInicial.UsuarioResumen usuarioPorId = persistenciaInicial.buscarUsuarioPorId(ana.id())
                    .orElseThrow();
            List<PersistenciaInicial.UsuarioResumen> usuariosPorGmail = persistenciaInicial.buscarUsuariosPorMail("gmail");
            List<PersistenciaInicial.UsuarioResumen> usuariosPorApellido = persistenciaInicial.buscarUsuariosPorMail("juarez");

            assertEquals("Ana", usuarioPorId.nombre());
            assertEquals("anagarcia@gmail.com", usuarioPorId.mail());
            assertEquals(2, usuariosPorGmail.size());
            assertEquals(1, usuariosPorApellido.size());
            assertTrue(usuariosPorApellido.get(0).id() > 0);
            assertEquals("Bruno", usuariosPorApellido.get(0).nombre());
            assertTrue(persistenciaInicial.buscarUsuarioPorId(999L).isEmpty());
            assertTrue(persistenciaInicial.buscarUsuariosPorMail("noexiste").isEmpty());
        }
    }

    @Test
    void borraProductoLogicamenteYPermiteRestaurarlo() {
        ConfiguracionTemporal configuracion = crearConfiguracionTemporal();

        try (PersistenciaInicial persistenciaInicial = PersistenciaInicial.inicializar(
                configuracion.archivoBase(),
                configuracion.archivosBase(),
                configuracion.jdbcUrl()
        )) {
            Long productoId = buscarProductoPorNombre(persistenciaInicial, "Cafe molido").id();

            PersistenciaInicial.ProductoResumen productoBorrado = persistenciaInicial.borrarProducto(productoId);
            PersistenciaInicial.ResumenPersistencia resumen = persistenciaInicial.contarDatos();
            List<PersistenciaInicial.ProductoResumen> productosActivos = persistenciaInicial.listarProductos();
            List<PersistenciaInicial.ProductoResumen> productosEliminados = persistenciaInicial.listarProductosEliminados();

            assertTrue(productoBorrado.eliminado());
            assertEquals(10L, resumen.productos());
            assertEquals(9L, resumen.productosActivos());
            assertEquals(1L, resumen.productosEliminados());
            assertEquals(9, productosActivos.size());
            assertFalse(productosActivos.stream().anyMatch(producto -> producto.id().equals(productoId)));
            assertEquals(1, productosEliminados.size());
            assertEquals(productoId, productosEliminados.get(0).id());

            PersistenciaInicial.ProductoResumen productoRestaurado = persistenciaInicial.restaurarProducto(productoId);
            PersistenciaInicial.ResumenPersistencia resumenRestaurado = persistenciaInicial.contarDatos();

            assertFalse(productoRestaurado.eliminado());
            assertEquals(10L, resumenRestaurado.productosActivos());
            assertEquals(0L, resumenRestaurado.productosEliminados());
            assertTrue(persistenciaInicial.listarProductos().stream().anyMatch(producto -> producto.id().equals(productoId)));
            assertTrue(persistenciaInicial.listarProductosEliminados().isEmpty());
        }
    }

    private PersistenciaInicial.ProductoResumen buscarProductoPorNombre(
            PersistenciaInicial persistenciaInicial,
            String nombre
    ) {
        return persistenciaInicial.listarProductos().stream()
                .filter(producto -> producto.nombre().equals(nombre))
                .findFirst()
                .orElseThrow();
    }

    private PersistenciaInicial.CategoriaResumen buscarCategoriaPorNombre(
            PersistenciaInicial persistenciaInicial,
            String nombre
    ) {
        return persistenciaInicial.listarCategorias().stream()
                .filter(categoria -> categoria.nombre().equals(nombre))
                .findFirst()
                .orElseThrow();
    }

    private ConfiguracionTemporal crearConfiguracionTemporal() {
        Path baseSinExtension = tempDir.resolve("jpa_db");
        Path archivoBase = tempDir.resolve("jpa_db.mv.db");
        List<Path> archivosBase = List.of(
                archivoBase,
                tempDir.resolve("jpa_db.trace.db"),
                tempDir.resolve("jpa_db.lock.db")
        );
        String jdbcUrl = "jdbc:h2:file:" + baseSinExtension.toAbsolutePath();

        return new ConfiguracionTemporal(archivoBase, archivosBase, jdbcUrl);
    }

    private record ConfiguracionTemporal(Path archivoBase, List<Path> archivosBase, String jdbcUrl) {
    }
}
