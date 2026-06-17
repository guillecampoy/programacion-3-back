# Parcial 2 - Programacion III - JPA

Entrega final del parcial de Java Persistence API sobre repositorios, ABM de categorias/productos y consulta JPQL por categoria.

El proyecto queda unificado bajo la paqueteria `com.tp.jpa`, con una unica clase de entrada `com.tp.jpa.Main`. El modelo respeta las relaciones del UML de `docs/diagrama.puml`; ese archivo no fue modificado.

## Funcionalidades

La aplicacion de consola permite:

1. ABM de categorias.
2. ABM de productos.
3. Listado de categorias activas.
4. Listado de productos activos.
5. Baja logica mediante el campo `eliminado`.
6. Reporte de productos activos por categoria usando JPQL.
7. Persistencia JPA/Hibernate con H2.
8. Carga inicial de datos de ejemplo.

## Estructura

```text
src/main/java/com/tp/jpa/
  Main.java                      # unica entrada de consola
  H2LocalConsole.java            # consola web H2 opcional
  model/
    Base.java                    # clase base con id, eliminado y createdAt
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
    CatalogoService.java         # reglas de negocio y validaciones
  seed/
    DatosSemillaFactory.java
    PersistenciaInicial.java
  util/
    JPAUtil.java
    ConsolaUtils.java
    EntradaValidada.java
```

Los tests estan en `src/test/java/com/tp/jpa/` y cubren entidades, repositorios, servicio, consola, persistencia inicial e integracion JPA.

## Modelo

Las entidades conservan las relaciones indicadas en el UML:

1. `Usuario` hereda de `Base` y se relaciona con muchos `Pedido`.
2. `Categoria` hereda de `Base` y agrupa muchos `Producto`.
3. `Producto` hereda de `Base` y referencia una `Categoria`.
4. `Pedido` hereda de `Base`, implementa `Calculable`, pertenece a un `Usuario` y compone muchos `DetallePedido`.
5. `DetallePedido` hereda de `Base` y referencia un `Producto`.
6. `UsuarioDTO` depende de `Usuario`.

No se modifico `docs/diagrama.puml`.

## IDs Autogenerados

`Base` centraliza el identificador y delega la generacion a JPA:

```java
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

Las altas crean entidades con `id == null`. El ID se obtiene despues de persistir usando la instancia retornada por `guardar(...)` o la entidad administrada por JPA.

La semilla tampoco fija IDs manuales; arma relaciones con referencias a objetos.

## Repositorios

`BaseRepository<T>` implementa:

```java
public T guardar(T entity);
public Optional<T> buscarPorId(Long id);
public List<T> listarActivos();
public boolean eliminarLogico(Long id);
```

Cada metodo abre y cierra su propio `EntityManager`. Las escrituras usan transaccion, `merge()` y rollback ante error.

`ProductoRepository.buscarPorCategoria(Long categoriaId)` usa JPQL tipado y parametro nombrado:

```jpql
select p from Producto p
where p.categoria.id = :categoriaId
  and p.eliminado = false
```

## Capa de Servicio

`CatalogoService` separa la logica de negocio de la consola. Se encarga de:

1. Crear categorias y productos.
2. Modificar categorias y productos.
3. Ejecutar bajas logicas.
4. Validar existencia y estado activo.
5. Validar entrada antes de construir o mutar entidades.
6. Obtener listados activos para consola y reportes.

Validaciones defensivas en servicio:

1. IDs obligatorios y mayores a 0.
2. Nombre y descripcion obligatorios en altas.
3. Precio de producto mayor a 0.
4. Stock mayor o igual a 0.
5. En modificaciones, campos vacios conservan el valor previo; campos presentes se validan antes de tocar la entidad.

## Menu de Consola

Menu principal:

```text
Sistema JPA - Categorias y Productos
1. Categorias
2. Productos
3. Reportes
0. Salir
```

Categorias:

```text
1. Alta de categoria
2. Modificar categoria
3. Baja logica de categoria
4. Listar categorias activas
0. Volver
```

Productos:

```text
1. Alta de producto
2. Modificar producto
3. Baja logica de producto
4. Listar productos activos
0. Volver
```

Reportes:

```text
1. Productos por categoria
0. Volver
```

## Configuracion JPA

La unidad de persistencia es `miUnidad`, definida en:

```text
src/main/resources/META-INF/persistence.xml
```

Entidades registradas:

```text
com.tp.jpa.model.Usuario
com.tp.jpa.model.Categoria
com.tp.jpa.model.Producto
com.tp.jpa.model.Pedido
com.tp.jpa.model.DetallePedido
```

La base local usa H2 en archivo:

```text
jdbc:h2:file:./data/jpa_db;AUTO_SERVER=TRUE
```

Si existe una base vieja creada antes de los IDs autogenerados, borrar `data/jpa_db.mv.db` antes de probar altas nuevas.
La aplicacion usa `GenerationType.AUTO` para que Hibernate genere los IDs sin depender de valores manuales y para evitar errores de tablas H2 antiguas cuyo `ID` no fue creado como identity.

## Ejecucion

Compilar:

```bash
./gradlew build
```

Ejecutar la aplicacion:

```bash
./gradlew run
```

Clase principal:

```text
com.tp.jpa.Main
```

## Verificacion

Suite completa:

```bash
./gradlew test
```

Lint basico de formato:

```bash
./gradlew lintJavaFormatting
```

Tests relevantes:

```bash
./gradlew test --tests com.tp.jpa.MainTest
./gradlew test --tests com.tp.jpa.service.CatalogoServiceTest
./gradlew test --tests com.tp.jpa.repository.CategoriaRepositoryTest
./gradlew test --tests com.tp.jpa.repository.ProductoRepositoryTest
./gradlew test --tests com.tp.jpa.integration.JpaIntegrationTest
```

## Correspondencia con Historias

| Historia | Estado | Implementacion |
|----------|--------|----------------|
| HU-01 BaseRepository | Completa | `BaseRepository<T>` con `guardar`, `buscarPorId`, `listarActivos`, `eliminarLogico`, transacciones y cierre de `EntityManager`. |
| HU-02 Repositorios especificos | Completa | `CategoriaRepository` y `ProductoRepository`, incluyendo `buscarPorCategoria`. |
| HU-03 Alta categoria | Completa | Menu de categorias, validacion de nombre/descripcion, ID visible. |
| HU-04 Modificar categoria | Completa | Lista activas, valida ID, muestra valores actuales, campos vacios conservan valor. |
| HU-05 Baja categoria | Completa | Baja logica, valida inexistente o ya eliminada, no aparece en listados activos. |
| HU-06 Alta producto | Completa | Lista categorias activas, valida precio/stock, relacion `ManyToOne`, ID visible. |
| HU-07 Modificar producto | Completa | Lista activos, valida ID, muestra valores actuales, valida precio/stock. |
| HU-08 Baja producto | Completa | Baja logica, confirma con nombre, no aparece en listados activos. |
| HU-09 Consulta JPQL | Completa | Reporte por categoria con JPQL tipado y parametro `:categoriaId`. |

## Guia para Video de Demostracion

Duracion sugerida: 10 a 15 minutos. Mantener camara encendida y audio claro.

### 1. Presentacion

Duracion sugerida: 1 minuto.

Contenido:

1. Nombre y materia.
2. Indicar que se trata del Parcial 2 de JPA.
3. Mostrar brevemente el objetivo: ABM de categorias/productos, repositorios y consulta JPQL.

### 2. Recorrido por la Estructura

Duracion sugerida: 2 minutos.

Mostrar:

1. `com.tp.jpa.Main` como unica entrada.
2. `model` y `model.enums`.
3. `repository` con `BaseRepository`, `CategoriaRepository`, `ProductoRepository`.
4. `service/CatalogoService`.
5. `persistence.xml`.
6. `docs/diagrama.puml`, aclarando que las relaciones del UML se respetan.

### 3. Repositorios y JPQL

Duracion sugerida: 2 minutos.

Explicar:

1. `BaseRepository.guardar` usa `merge()` y transacciones.
2. `buscarPorId` retorna `Optional`.
3. `listarActivos` filtra `eliminado = false`.
4. `eliminarLogico` marca `eliminado = true`.
5. `ProductoRepository.buscarPorCategoria` usa JPQL con `TypedQuery<Producto>`.

### 4. Demo de Categorias

Duracion sugerida: 2 minutos.

En la consola:

1. Entrar a `Categorias`.
2. Crear una categoria.
3. Mostrar el ID generado.
4. Listar categorias activas.
5. Modificar nombre o descripcion.
6. Dar de baja una categoria.
7. Confirmar que no aparece en el listado activo.

### 5. Demo de Productos

Duracion sugerida: 3 minutos.

En la consola:

1. Entrar a `Productos`.
2. Crear un producto seleccionando una categoria activa.
3. Probar validacion de precio `0` o stock negativo.
4. Confirmar alta con ID generado y categoria asignada.
5. Listar productos activos.
6. Modificar nombre, precio o stock.
7. Dar de baja un producto.
8. Confirmar que no aparece en productos activos.

### 6. Demo de Reporte por Categoria

Duracion sugerida: 2 minutos.

En la consola:

1. Entrar a `Reportes`.
2. Elegir `Productos por categoria`.
3. Seleccionar una categoria activa.
4. Mostrar productos con ID, nombre, precio y stock.
5. Luego de bajar un producto, repetir el reporte y confirmar que ya no aparece.

### 7. Tests y Cierre

Duracion sugerida: 2 minutos.

Mostrar:

```bash
./gradlew test
./gradlew lintJavaFormatting
```

Cerrar explicando:

1. Funcionalidades implementadas.
2. Separacion en repositorio, servicio y consola.
3. Validaciones en consola, servicio y entidades.
4. IDs autogenerados.
5. Dificultades resueltas: unificacion de paquetes, preservacion del UML, listados activos y manejo de base H2 local.
