# Programacion III - Backend JPA

Backend de consola para el TPI de Programacion III con Java, Gradle, JPA/Hibernate y H2 en archivo.

Esta rama documenta una evolucion sobre la entrega previa: el proyecto ya consolida el nucleo de catalogo y persistencia, mantiene el modelo de dominio alineado con el UML de `docs/diagrama.puml` y deja preparada la base para las historias siguientes sin implementar nuevas historias de usuario en este paso.

## Estado actual

Lo que hoy expone la aplicacion desde consola es:

1. ABM de categorias.
2. ABM de productos.
3. Listado de categorias activas.
4. Listado de productos activos.
5. Baja logica y restauracion de categorias.
6. Baja logica y restauracion de productos.
7. Reporte de productos activos por categoria.
8. Regeneracion de la base local con semilla inicial.

El dominio ya incluye `Usuario`, `Pedido` y `DetallePedido` para sostener la evolucion del modelo, la semilla y los tests de relacion, aunque esas entidades no forman parte del menu operativo actual.

## Documentacion del proyecto

La carpeta `docs/` funciona como guia de trabajo y backlog.

1. [`docs/README.md`](docs/README.md): orden recomendado de historias y validacion de entrega.
2. [`docs/00_contexto_restricciones.md`](docs/00_contexto_restricciones.md): reglas tecnicas, estructura esperada y restricciones de implementacion.
3. [`docs/historias/`](docs/historias/): historias de usuario individuales para el recorrido incremental del backend.
4. [`docs/diagrama.puml`](docs/diagrama.puml): UML de referencia del modelo. Se conserva sin cambios de relaciones.

## Estructura actual

```text
src/main/java/com/tp/jpa/
  Main.java                 # menu de consola
  H2LocalConsole.java       # consola web de H2
  model/
    Base.java
    Categoria.java
    Producto.java
    Usuario.java
    Pedido.java
    DetallePedido.java
    Calculable.java
    enums/
      Estado.java
      FormaPago.java
      Rol.java
  repository/
    BaseRepository.java
    CategoriaRepository.java
    ProductoRepository.java
  service/
    CatalogoService.java
  seed/
    DatosSemilla.java
    DatosSemillaFactory.java
    PersistenciaInicial.java
  util/
    JPAUtil.java
    ConsolaUtils.java
    EntradaValidada.java

src/test/java/com/tp/jpa/
  MainTest.java
  integration/JpaIntegrationTest.java
  model/RelacionesTest.java
  repository/CategoriaRepositoryTest.java
  repository/ProductoRepositoryTest.java
  seed/PersistenciaInicialTest.java
  service/CatalogoServiceTest.java
  util/EntradaValidadaTest.java
```

## Modelo de dominio

Las entidades principales respetan la base conceptual del UML:

1. `Base` centraliza `id`, `eliminado` y `createdAt`.
2. `Categoria` agrupa productos.
3. `Producto` referencia una categoria.
4. `Usuario` se relaciona con pedidos.
5. `Pedido` implementa `Calculable` y compone detalles.
6. `DetallePedido` referencia un producto.
7. `UsuarioDTO` existe como representacion de lectura.

La relacion entre clases se mantiene en el marco de lo que ya esta alcanzado por el UML del proyecto. Este README solo describe la estructura actual; el diagrama de clases no se modifica aqui.

## Repositorios

HU-01 queda implementada en `BaseRepository<T>`, que resuelve la infraestructura comun:

1. Guardado con transaccion propia.
2. Busqueda por id.
3. Listado de activos.
4. Borrado logico con `eliminarLogico()`.
5. Compatibilidad con `cambiarEstadoEliminado()` para el resto del proyecto.
6. Obtencion del siguiente id logico cuando hace falta para pruebas o utilidades.

HU-02 queda implementada en los repositorios concretos:

1. `CategoriaRepository` extiende `BaseRepository<Categoria>`.
2. `ProductoRepository` extiende `BaseRepository<Producto>`.
3. `ProductoRepository.buscarPorCategoria(Long)` ejecuta JPQL tipado con `:catId` y `eliminado = false`.
4. La consulta devuelve solo productos activos de la categoria indicada.

HU-03 queda implementada en `UsuarioRepository`:

1. `UsuarioRepository` extiende `BaseRepository<Usuario>`.
2. `buscarPorMail(String)` devuelve `Optional<Usuario>`.
3. La busqueda soporta coincidencia parcial y excluye usuarios eliminados.
4. La consulta usa JPQL tipado con `:mail`, `getResultList()` y cierre explicito del `EntityManager`.

HU-04 queda implementada en `PedidoRepository`:

1. `PedidoRepository` extiende `BaseRepository<Pedido>`.
2. `buscarPorUsuario(Long)` filtra pedidos activos por `usuario.id`.
3. `buscarPorEstado(Estado)` filtra pedidos activos por estado.
4. Ambas consultas usan JPQL tipado con parámetros nombrados y cierre explicito del `EntityManager`.

HU-05 queda implementada en el alta de categorias:

1. La consola solicita nombre y descripcion.
2. El nombre sigue siendo obligatorio.
3. La descripcion es opcional y se guarda vacia si el operador no ingresa texto.
4. La categoria se persiste con `eliminado = false` y se muestra el ID generado.

HU-06 queda implementada en la modificacion de categorias:

1. La consola lista categorias activas antes de pedir el ID.
2. El operador ve los valores actuales antes de editar.
3. Los campos en blanco conservan el valor previo.
4. La categoria modificada se persiste en la base.

HU-07 queda implementada en la baja logica de categorias:

1. La consola usa la opcion de baja del submenu de categorias.
2. La categoria se marca como eliminada sin borrar productos asociados.
3. La confirmacion muestra el nombre de la categoria dada de baja.
4. La categoria deja de aparecer en los listados activos.

HU-08 queda implementada en el alta de productos:

1. La consola lista categorias activas antes de pedir los datos del producto.
2. El alta solicita nombre, descripcion, precio, stock, imagen y disponible.
3. La categoria seleccionada se asocia al producto guardado.
4. El producto se persiste con `eliminado = false` y se muestra el ID generado.

HU-09 queda implementada en la modificacion de productos:

1. La consola lista productos activos antes de pedir el ID.
2. El operador ve los valores actuales antes de editar.
3. Los campos en blanco conservan el valor previo.
4. La modificacion valida precio y stock antes de persistir.

HU-10 queda implementada en la baja de productos:

1. La consola usa la opcion de baja del submenu de productos.
2. El producto se marca como eliminado sin borrar el registro.
3. La confirmacion muestra el nombre del producto dado de baja.
4. El producto deja de aparecer en los listados activos.

## Capa de servicio

`CatalogoService` concentra la logica de negocio que usa la consola:

1. Crear y modificar categorias.
2. Crear y modificar productos.
3. Ejecutar bajas logicas.
4. Restaurar registros eliminados.
5. Validar ids, textos, precio y stock.
6. Resolver el impacto de eliminar una categoria sobre sus productos activos.
7. Buscar productos activos por categoria.

La consola delega en esta capa para evitar mezclar input de usuario con reglas de negocio.

## Consola

Menu principal actual:

```text
Sistema JPA - Categorias y Productos
1. Categorias
2. Productos
3. Reportes
4. Regenerar datos
0. Salir
```

Submenu de categorias:

```text
1. Alta de categoria
2. Modificar categoria
3. Baja logica de categoria
4. Listar categorias activas
5. Revertir baja logica
0. Volver
```

Submenu de productos:

```text
1. Alta de producto
2. Modificar producto
3. Baja logica de producto
4. Listar productos activos
5. Revertir baja logica
0. Volver
```

Submenu de reportes:

```text
1. Productos por categoria
0. Volver
```

## Persistencia

La unidad de persistencia es `miUnidad`, definida en `src/main/resources/META-INF/persistence.xml`.

La base local usa H2 en archivo:

```text
jdbc:h2:file:./data/jpa_db
```

Puntos clave:

1. Hibernate administra el esquema con `hbm2ddl.auto=update` en ejecucion local.
2. `JPAUtil` mantiene una `EntityManagerFactory` unica.
3. `PersistenciaInicial` carga datos solo si corresponde.
4. La opcion `Regenerar datos` elimina la base local y vuelve a aplicar la semilla.

## Semilla y datos

La semilla inicial prepara datos reales para trabajar con la consola y para sostener los tests de integracion.

1. `PersistenciaInicial` decide si hay que cargar datos.
2. `DatosSemillaFactory` construye el escenario inicial.
3. La carga se apoya en relaciones JPA reales, no en ids manuales sueltos.

## Validacion

La base del proyecto ya pasa la suite de tests:

```bash
./gradlew test
```

La validacion cubre el contrato de HU-01 con pruebas sobre guardado nuevo, guardado con id existente, busqueda, listado activo y borrado logico.

Y la aplicacion puede ejecutarse desde consola con:

```bash
./gradlew run
```

## Proximo tramo

La documentacion de `docs/historias/` marca el backlog incremental que sigue sobre esta base. El objetivo de esa carpeta es guiar la evolucion por historias sin reescribir la estructura ya consolidada ni tocar el UML de referencia.
