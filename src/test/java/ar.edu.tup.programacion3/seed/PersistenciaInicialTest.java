package ar.edu.tup.programacion3.seed;

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
            PersistenciaInicial.ProductoResumen cafe = persistenciaInicial.actualizarProducto(
                    PersistenciaInicial.ProductoActualizacion.nombre(1L, "Cafe actualizado")
            );
            PersistenciaInicial.ProductoResumen yerba = persistenciaInicial.actualizarProducto(
                    PersistenciaInicial.ProductoActualizacion.categoria(2L, 2L)
            );

            assertEquals("Cafe actualizado", cafe.nombre());
            assertEquals(2L, yerba.categoriaId());
            assertEquals("Bebidas", yerba.categoria());
        }
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
