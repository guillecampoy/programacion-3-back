package com.tp.jpa.seed;

import com.tp.jpa.entities.Categoria;
import com.tp.jpa.entities.Pedido;
import com.tp.jpa.entities.Producto;
import com.tp.jpa.entities.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.RollbackException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class PersistenciaInicial implements AutoCloseable {
    private static final Path BASE_LOCAL = Path.of("data", "jpa_db.mv.db");
    private static final List<Path> ARCHIVOS_BASE_LOCAL = List.of(
            Path.of("data", "jpa_db.mv.db"),
            Path.of("data", "jpa_db.trace.db"),
            Path.of("data", "jpa_db.lock.db")
    );
    private static final String UNIDAD_PERSISTENCIA = "miUnidad";

    private final EntityManagerFactory entityManagerFactory;
    private final boolean baseLocalExistia;
    private boolean datosInicialesPersistidos;

    private PersistenciaInicial(Path baseLocal, List<Path> archivosBaseLocal, String jdbcUrl) {
        this.baseLocalExistia = existeBaseLocal(baseLocal);
        this.entityManagerFactory = Persistence.createEntityManagerFactory(
                UNIDAD_PERSISTENCIA,
                Map.of("jakarta.persistence.jdbc.url", jdbcUrl)
        );
    }

    public static PersistenciaInicial inicializar() {
        PersistenciaInicial persistenciaInicial = new PersistenciaInicial(
                BASE_LOCAL,
                ARCHIVOS_BASE_LOCAL,
                "jdbc:h2:file:./data/jpa_db;AUTO_SERVER=TRUE"
        );
        persistenciaInicial.persistirDatosInicialesSiCorresponde();
        return persistenciaInicial;
    }

    static PersistenciaInicial inicializar(Path baseLocal, List<Path> archivosBaseLocal, String jdbcUrl) {
        PersistenciaInicial persistenciaInicial = new PersistenciaInicial(baseLocal, archivosBaseLocal, jdbcUrl);
        persistenciaInicial.persistirDatosInicialesSiCorresponde();
        return persistenciaInicial;
    }

    public static boolean existeBaseLocal() {
        return existeBaseLocal(BASE_LOCAL);
    }

    public static void borrarBaseLocal() {
        ARCHIVOS_BASE_LOCAL.forEach(PersistenciaInicial::borrarSiExiste);
    }

    public boolean isBaseLocalExistia() {
        return baseLocalExistia;
    }

    public boolean isDatosInicialesPersistidos() {
        return datosInicialesPersistidos;
    }

    public ResumenPersistencia contarDatos() {
        return consultar(entityManager -> new ResumenPersistencia(
                contar(entityManager, Usuario.class),
                contar(entityManager, Categoria.class),
                contar(entityManager, Producto.class),
                contarProductosPorEliminado(entityManager, false),
                contarProductosPorEliminado(entityManager, true),
                contar(entityManager, Pedido.class)
        ));
    }

    public List<ProductoResumen> listarProductos() {
        return consultar(entityManager ->
                entityManager.createQuery(
                                "select p from Producto p where p.eliminado = false order by p.id",
                                Producto.class
                        )
                        .getResultList()
                        .stream()
                        .map(ProductoResumen::from)
                        .toList()
        );
    }

    public List<ProductoResumen> listarProductosEliminados() {
        return consultar(entityManager ->
                entityManager.createQuery(
                                "select p from Producto p where p.eliminado = true order by p.id",
                                Producto.class
                        )
                        .getResultList()
                        .stream()
                        .map(ProductoResumen::from)
                        .toList()
        );
    }

    public List<CategoriaResumen> listarCategorias() {
        return consultar(entityManager ->
                entityManager.createQuery("select c from Categoria c order by c.id", Categoria.class)
                        .getResultList()
                        .stream()
                        .map(CategoriaResumen::from)
                        .toList()
        );
    }

    public Optional<UsuarioResumen> buscarUsuarioPorId(Long id) {
        return consultar(entityManager -> Optional.ofNullable(entityManager.find(Usuario.class, id))
                .map(UsuarioResumen::from)
        );
    }

    public List<UsuarioResumen> buscarUsuariosPorMail(String mailParcial) {
        return consultar(entityManager ->
                entityManager.createQuery(
                                "select u from Usuario u where lower(u.mail) like lower(:mail) order by u.id",
                                Usuario.class
                        )
                        .setParameter("mail", "%" + mailParcial + "%")
                        .getResultList()
                        .stream()
                        .map(UsuarioResumen::from)
                        .toList()
        );
    }

    public ProductoResumen actualizarProducto(ProductoActualizacion actualizacion) {
        return ejecutarEnTransaccion(entityManager -> {
            Producto producto = entityManager.find(Producto.class, actualizacion.id());
            if (producto == null) {
                throw new IllegalArgumentException("No existe producto con id " + actualizacion.id());
            }

            if (actualizacion.nombre() != null) {
                producto.setNombre(actualizacion.nombre());
            }
            if (actualizacion.precio() != null) {
                producto.setPrecio(actualizacion.precio());
            }
            if (actualizacion.descripcion() != null) {
                producto.setDescripcion(actualizacion.descripcion());
            }
            if (actualizacion.stock() != null) {
                producto.setStock(actualizacion.stock());
            }
            if (actualizacion.imagen() != null) {
                producto.setImagen(actualizacion.imagen());
            }
            if (actualizacion.disponible() != null) {
                producto.setDisponible(actualizacion.disponible());
            }
            if (actualizacion.categoriaId() != null) {
                Categoria categoria = entityManager.find(Categoria.class, actualizacion.categoriaId());
                if (categoria == null) {
                    throw new IllegalArgumentException("No existe categoria con id " + actualizacion.categoriaId());
                }
                producto.setCategoria(categoria);
            }

            return ProductoResumen.from(producto);
        });
    }

    public ProductoResumen borrarProducto(Long id) {
        return ejecutarEnTransaccion(entityManager -> {
            Producto producto = entityManager.find(Producto.class, id);
            if (producto == null || Boolean.TRUE.equals(producto.getEliminado())) {
                throw new IllegalArgumentException("No existe producto activo con id " + id);
            }

            producto.setEliminado(true);
            return ProductoResumen.from(producto);
        });
    }

    public ProductoResumen restaurarProducto(Long id) {
        return ejecutarEnTransaccion(entityManager -> {
            Producto producto = entityManager.find(Producto.class, id);
            if (producto == null || !Boolean.TRUE.equals(producto.getEliminado())) {
                throw new IllegalArgumentException("No existe producto eliminado con id " + id);
            }

            producto.setEliminado(false);
            return ProductoResumen.from(producto);
        });
    }

    private void persistirDatosInicialesSiCorresponde() {
        if (!debePersistirDatosIniciales()) {
            return;
        }

        DatosSemilla datosSemilla = DatosSemillaFactory.crear();
        ejecutarEnTransaccion(entityManager -> {
            datosSemilla.categorias().forEach(entityManager::persist);
            datosSemilla.usuarios().forEach(entityManager::persist);
        });
        datosInicialesPersistidos = true;
    }

    private boolean debePersistirDatosIniciales() {
        ResumenPersistencia resumen = contarDatos();
        return !baseLocalExistia || resumen.totalRegistros() == 0;
    }

    private static boolean existeBaseLocal(Path baseLocal) {
        return Files.exists(baseLocal);
    }

    private long contar(EntityManager entityManager, Class<?> entityClass) {
        return entityManager.createQuery(
                        "select count(e) from " + entityClass.getSimpleName() + " e",
                        Long.class
                )
                .getSingleResult();
    }

    private long contarProductosPorEliminado(EntityManager entityManager, boolean eliminado) {
        return entityManager.createQuery(
                        "select count(p) from Producto p where p.eliminado = :eliminado",
                        Long.class
                )
                .setParameter("eliminado", eliminado)
                .getSingleResult();
    }

    private void ejecutarEnTransaccion(Consumer<EntityManager> consumer) {
        ejecutarEnTransaccion(entityManager -> {
            consumer.accept(entityManager);
            return null;
        });
    }

    private <T> T ejecutarEnTransaccion(Function<EntityManager, T> function) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            T resultado = function.apply(entityManager);
            entityManager.getTransaction().commit();
            return resultado;
        } catch (NoResultException exception) {
            rollbackSiActivo(entityManager);
            throw errorConsultaSinResultados(exception);
        } catch (NonUniqueResultException exception) {
            rollbackSiActivo(entityManager);
            throw errorConsultaConResultadosDuplicados(exception);
        } catch (RollbackException exception) {
            rollbackSiActivo(entityManager);
            throw errorRollback(exception);
        } catch (PersistenceException exception) {
            rollbackSiActivo(entityManager);
            throw errorPersistencia(exception);
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private <T> T consultar(Function<EntityManager, T> function) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return function.apply(entityManager);
        } catch (NoResultException exception) {
            throw errorConsultaSinResultados(exception);
        } catch (NonUniqueResultException exception) {
            throw errorConsultaConResultadosDuplicados(exception);
        } catch (PersistenceException exception) {
            throw errorPersistencia(exception);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }

    private static void borrarSiExiste(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo borrar " + path, exception);
        }
    }

    private void rollbackSiActivo(EntityManager entityManager) {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }

    private IllegalStateException errorConsultaSinResultados(NoResultException exception) {
        return new IllegalStateException("La consulta JPA no devolvio resultados.", exception);
    }

    private IllegalStateException errorConsultaConResultadosDuplicados(NonUniqueResultException exception) {
        return new IllegalStateException("La consulta JPA devolvio mas de un resultado.", exception);
    }

    private IllegalStateException errorRollback(RollbackException exception) {
        return new IllegalStateException("No se pudo confirmar la transaccion JPA.", exception);
    }

    private IllegalStateException errorPersistencia(PersistenceException exception) {
        return new IllegalStateException("Ocurrio un error de persistencia JPA.", exception);
    }

    public record ResumenPersistencia(
            long usuarios,
            long categorias,
            long productos,
            long productosActivos,
            long productosEliminados,
            long pedidos
    ) {
        public long totalRegistros() {
            return usuarios + categorias + productos + pedidos;
        }
    }

    public record ProductoActualizacion(
            Long id,
            String nombre,
            Double precio,
            String descripcion,
            Integer stock,
            String imagen,
            Boolean disponible,
            Long categoriaId
    ) {
        public static ProductoActualizacion nombre(Long id, String nombre) {
            return new ProductoActualizacion(id, nombre, null, null, null, null, null, null);
        }

        public static ProductoActualizacion precio(Long id, Double precio) {
            return new ProductoActualizacion(id, null, precio, null, null, null, null, null);
        }

        public static ProductoActualizacion descripcion(Long id, String descripcion) {
            return new ProductoActualizacion(id, null, null, descripcion, null, null, null, null);
        }

        public static ProductoActualizacion stock(Long id, Integer stock) {
            return new ProductoActualizacion(id, null, null, null, stock, null, null, null);
        }

        public static ProductoActualizacion imagen(Long id, String imagen) {
            return new ProductoActualizacion(id, null, null, null, null, imagen, null, null);
        }

        public static ProductoActualizacion disponible(Long id, Boolean disponible) {
            return new ProductoActualizacion(id, null, null, null, null, null, disponible, null);
        }

        public static ProductoActualizacion categoria(Long id, Long categoriaId) {
            return new ProductoActualizacion(id, null, null, null, null, null, null, categoriaId);
        }
    }

    public record ProductoResumen(
            Long id,
            String nombre,
            Double precio,
            String descripcion,
            int stock,
            String imagen,
            Boolean disponible,
            Boolean eliminado,
            Long categoriaId,
            String categoria
    ) {
        public static ProductoResumen from(Producto producto) {
            return new ProductoResumen(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getDescripcion(),
                    producto.getStock(),
                    producto.getImagen(),
                    producto.getDisponible(),
                    producto.getEliminado(),
                    producto.getCategoria() == null ? null : producto.getCategoria().getId(),
                    producto.getCategoria() == null ? "" : producto.getCategoria().getNombre()
            );
        }
    }

    public record CategoriaResumen(Long id, String nombre) {
        public static CategoriaResumen from(Categoria categoria) {
            return new CategoriaResumen(categoria.getId(), categoria.getNombre());
        }
    }

    public record UsuarioResumen(
            Long id,
            String nombre,
            String apellido,
            String mail,
            String celular,
            String rol
    ) {
        public static UsuarioResumen from(Usuario usuario) {
            return new UsuarioResumen(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getMail(),
                    usuario.getCelular(),
                    usuario.getRol() == null ? "" : usuario.getRol().name()
            );
        }
    }
}
