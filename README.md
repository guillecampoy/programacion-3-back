# Parcial 2 - Programación III - JPA

## Propósito del documento

Este `README.md` es el documento de arranque para implementar la segunda entrega del trabajo integrador/parcial de Programación III sobre Java Persistence API (JPA), repositorios y ABM de Categorías y Productos.

El objetivo es extender el proyecto Gradle existente del TP de la Unidad 8. El código base se considera existente y funcional. El desarrollo debe agregar solamente las piezas pedidas por la consigna, respetando los paquetes, entidades, `JPAUtil` y configuración de persistencia ya provista.

## Resultado esperado

Al finalizar, el proyecto debe permitir ejecutar una aplicacion de consola que exponga:

1. ABM de Categorías.
2. ABM de Productos.
3. Reporte de productos activos filtrados por categoria usando JPQL.
4. Repositorios JPA reutilizables para `Categoria` y `Producto`.
5. Baja lógica mediante el campo `eliminado`.
6. Listados que muestren solo registros activos (`eliminado = false`).
7. Generación automática de IDs por la base de datos/JPA.

## Generación automática de IDs

La clase base del modelo (`ar.edu.tup.programacion3.entities.Base`) centraliza el identificador de las entidades y lo delega a JPA/Hibernate:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Con esta configuración:

1. Las entidades nuevas deben crearse con `id == null`.
2. No se debe llamar a `setId(...)` al crear categorías, productos, usuarios, pedidos o detalles nuevos.
3. La base de datos asigna el ID al persistir la entidad.
4. Para mostrar el ID generado en consola, se debe usar la instancia retornada por `guardar(...)` o la entidad administrada por JPA.
5. La semilla inicial no fija IDs manuales; las relaciones se arman con referencias a objetos, no con valores numéricos hardcodeados.

Los IDs visibles en la consola o en los tests pueden variar según el estado de la base. Por eso, las validaciones deben buscar registros por datos de negocio cuando corresponda, por ejemplo nombre o mail, y usar el ID generado solo después de persistir.

## Restricciones obligatorias

Estas restricciones son de maxima prioridad para el agente de desarrollo:

1. Mantener el modelo JPA actual y la generación automática de IDs definida en `Base`.
   - No asignar IDs manualmente en altas ni en datos semilla.
   - No depender de IDs fijos como `1L`, `48L` o `50L` para encontrar datos iniciales.
   - No modificar enums existentes salvo que una nueva consigna lo pida explícitamente.
   - No modificar `persistence.xml` salvo necesidad justificada.
2. Mantener una sola clase de entrada de consola: `src/main/java/com/tp/jpa/Main.java`.
3. Mantener el proyecto Gradle compilable y ejecutable.
4. Implementar persistencia con JPA, `EntityManager`, transacciones y JPQL.
5. No reemplazar JPA por colecciones en memoria, JDBC directo, SQL nativo ni frameworks no pedidos.
6. No introducir dependencias nuevas salvo que el proyecto base ya las contemple o sea estrictamente necesario y justificable.
7. No cambiar el modelo de dominio para facilitar la solución.
8. No cambiar nombres de paquetes, clases o métodos requeridos por la consigna.
9. El método `ProductoRepository.buscarPorCategoria(Long categoriaId)` debe tener un comentario que explique que hace la consulta JPQL.
10. Cada método de repositorio debe abrir y cerrar su propio `EntityManager`.
11. Todo `EntityManager` abierto debe cerrarse en un bloque `finally`.
12. Toda transacción de escritura debe hacer `rollback` si ocurre un error antes del `commit`.
13. Las operaciones de menu deben manejar errores de usuario sin romper la aplicación por una excepción no controlada.
14. La lógica de negocio debe vivir en `CatalogoService`; `Main` debe limitarse a entrada/salida de consola y navegación de menús.

## Estructura de paquetes requerida

La estructura esperada dentro de `src/main/java/` es:

```text
ar.edu.tup.programacion3/
  entities/          # entidades JPA del dominio
  enums/             # enums del dominio
  seed/              # carga y operaciones iniciales de persistencia
  utils/             # utilidades compartidas de consola/entrada
com.tp.jpa/
  util/
    JPAUtil.java
  repository/
    BaseRepository.java
    CategoriaRepository.java
    ProductoRepository.java
  service/
    CatalogoService.java
  Main.java          # unica clase Main de consola
```

En términos de paquetes Java:

```text
com.tp.jpa
com.tp.jpa.util
com.tp.jpa.repository
com.tp.jpa.service
```

La clase `src/main/java/ar/edu/tup/programacion3/Main.java` de una entrega anterior fue removida. La ejecución del programa queda centralizada en `com.tp.jpa.Main`.

## Archivos principales

| Archivo                    | Paquete                 | Responsabilidad                                                                                                               |
|----------------------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `BaseRepository.java`      | `com.tp.jpa.repository` | Repositorio generico abstracto `<T>` con operaciones comunes: `guardar`, `buscarPorId`, `listarActivos`, `eliminarLogico`.    |
| `CategoriaRepository.java` | `com.tp.jpa.repository` | Repositorio especifico de `Categoria`. Extiende `BaseRepository<Categoria>`. No agrega metodos.                               |
| `ProductoRepository.java`  | `com.tp.jpa.repository` | Repositorio especifico de `Producto`. Extiende `BaseRepository<Producto>` y agrega `buscarPorCategoria(Long categoriaId)`.    |
| `CatalogoService.java`     | `com.tp.jpa.service`    | Capa de servicio para reglas de negocio, validación de activos, creación, modificación, baja lógica y reportes por categoria. |
| `Main.java`                | `com.tp.jpa`            | Aplicacion de consola con menu principal y submenus. No accede directamente a repositorios para reglas de negocio.            |

## Protocolo recomendado para agente de desarrollo

Antes de implementar cualquier historia, el agente debe inspeccionar el proyecto base:

```bash
find src/main/java -type f | sort
find src/main/resources -type f | sort
cat build.gradle || cat build.gradle.kts
```

Luego revisar específicamente:

```bash
find src/main/java -type f | grep -E 'Categoria|Producto|JPAUtil|persistence'
```

Confirmar:

1. Paquete real de `Categoria` y `Producto`.
2. Nombre exacto de los campos.
3. Tipo de `id` y estrategia de generación (`@GeneratedValue(strategy = GenerationType.IDENTITY)`).
4. Tipo de `precio`.
5. Tipo de `stock`.
6. Nombre del getter/setter de `eliminado` (`getEliminado`, `isEliminado`, `setEliminado`, etc.).
7. Si las entidades usan constructores con parámetros, constructor vacío, setters o Lombok.
8. Si `createdAt` se inicializa automáticamente en la entidad o requiere seteo manual.
9. Método disponible en `JPAUtil` para obtener `EntityManagerFactory`.
10. Forma ya prevista por el proyecto para ejecutar una clase `main`.
11. Que las altas no asignen IDs antes de persistir.
12. Que `Main` use `CatalogoService` para operaciones de negocio.

Regla de trabajo: implementar una historia por vez, compilar, corregir y recién después avanzar.

## Orden recomendado de implementación

1. `HU-01` - BaseRepository genérico.
2. `HU-02` - Repositorios específicos de Categoria y Producto.
3. `HU-03` - Alta de categoria.
4. `HU-04` - Modificación de categoria.
5. `HU-05` - Baja lógica de categoria.
6. `HU-06` - Alta de producto.
7. `HU-07` - Modificación de producto.
8. `HU-08` - Baja lógica de producto.
9. `HU-09` - Consulta JPQL de productos por categoria.
10. Validación final integral y preparación de entrega.

## Backlog incluido

Las historias están en la carpeta `historias/`:

| Historia | Archivo                                           | Puntos | Prioridad |
|----------|---------------------------------------------------|-------:|-----------|
| HU-01    | `historias/HU-01-base-repository.md`              |     18 | Alta      |
| HU-02    | `historias/HU-02-repositorios-especificos.md`     |     12 | Alta      |
| HU-03    | `historias/HU-03-alta-categoria.md`               |      8 | Alta      |
| HU-04    | `historias/HU-04-modificar-categoria.md`          |     10 | Alta      |
| HU-05    | `historias/HU-05-baja-categoria.md`               |      7 | Media     |
| HU-06    | `historias/HU-06-alta-producto.md`                |     12 | Alta      |
| HU-07    | `historias/HU-07-modificar-producto.md`           |     10 | Alta      |
| HU-08    | `historias/HU-08-baja-producto.md`                |      8 | Media     |
| HU-09    | `historias/HU-09-productos-por-categoria-jpql.md` |     15 | Alta      |

Total: 100 puntos. Mínimo para aprobar: 60 puntos.

## Contrato técnico de BaseRepository

`BaseRepository<T>` debe ser una clase abstracta ubicada en `com.tp.jpa.repository`.

Responsabilidades:

1. Recibir `Class<T>` por constructor.
2. Obtener él `EntityManagerFactory` desde `JPAUtil`.
3. Implementar los métodos comunes.
4. Abrir su propio `EntityManager` en cada método.
5. Cerrar él `EntityManager` en `finally`.
6. Manejar transacciones en operaciones de escritura.

Firma esperada de métodos:

```java
public T guardar(T entity);
public Optional<T> buscarPorId(Long id);
public List<T> listarActivos();
public boolean eliminarLogico(Long id);
```

### Detalles importantes

- `guardar(T entity)` debe usar `merge()`, no `persist()`.
- Las entidades nuevas deben llegar a `guardar(T entity)` sin ID manual.
- `merge()` retorna la instancia administrada. Para mostrar IDs generados en consola, usar el objeto retornado por `guardar`, no asumir que el objeto original ya tiene ID actualizado.
- `buscarPorId(Long id)` debe usar `EntityManager.find(...)` y retornar `Optional.empty()` si no existe.
- `listarActivos()` debe usar JPQL con filtro `eliminado = false`.
- `eliminarLogico(Long id)` debe buscar por ID, marcar `eliminado = true`, persistir el cambio y retornar `true` si encontró el registro. Debe retornar `false` si no encontró el registro.
- Como `T` es genérico y no se pueden modificar entidades para que implementen una interfaz común, es aceptable usar reflexion para invocar `setEliminado(true)` dentro del repositorio base, siempre que sea claro, controlado y con manejo de error.
- Si las entidades tienen un ancestro común ya existente con `setEliminado`, usarlo solo si ya existe en el código base. No crearlo si implica modificar entidades.

## Contrato técnico de CategoriaRepository

`CategoriaRepository` debe:

1. Estar en `com.tp.jpa.repository`.
2. Extender `BaseRepository<Categoria>`.
3. Llamar a `super(Categoria.class)` en su constructor.
4. No agregar métodos adicionales.

## Contrato técnico de ProductoRepository

`ProductoRepository` debe:

1. Estar en `com.tp.jpa.repository`.
2. Extender `BaseRepository<Producto>`.
3. Llamar a `super(Producto.class)` en su constructor.
4. Implementar `public List<Producto> buscarPorCategoria(Long categoriaId)`.
5. Usar JPQL, no SQL nativo.
6. Usar parámetro nombrado `:categoriaId`.
7. Filtrar productos activos con `eliminado = false`.
8. Usar `TypedQuery<Producto>`.
9. No hacer casteos manuales.
10. Incluir un comentario sobre que hace la consulta JPQL.

JPQL esperada conceptualmente:

```
SELECT p
FROM Producto p
WHERE p.categoria.id = :categoriaId
  AND p.eliminado = false
```

Si la entidad usa un nombre JPA personalizado con `@Entity(name = "...")`, ajustar el nombre de entidad en JPQL respetando el modelo existente.

## Contrato técnico de CatalogoService

`CatalogoService` debe estar en `com.tp.jpa.service` y concentrar la lógica de negocio que antes quedaba mezclada en `Main`.

Responsabilidades:

1. Recibir `CategoriaRepository` y `ProductoRepository` por constructor.
2. Crear categorías y productos con `id == null` para respetar IDs autogenerados.
3. Setear valores operativos por defecto (`eliminado = false`, `createdAt`, `disponible`, imagen por defecto en productos).
4. Validar existencia y estado activo antes de modificar, dar de baja o listar productos por categoria.
5. Ejecutar bajas lógicas a través de los repositorios.
6. Exponer listados activos para que la consola pueda mostrarlos y validar opciones.
7. Lanzar excepciones con mensajes de negocio claros cuando el ID no existe o el registro ya está dado de baja.

`Main` debe llamar a este servicio y reservarse la lectura de datos, impresión de menús, parseo de entradas y mensajes de consola.

## Contrato de Main

`Main.java` debe estar en `com.tp.jpa` y exponer el menu principal de consola. Es la unica clase `Main` de la aplicación.

Menu principal sugerido:

```text
=== Sistema JPA - Categorias y Productos ===
1. Categorias
2. Productos
3. Reportes
0. Salir
Seleccione una opcion:
```

Submenu de Categorías:

```text
=== Categorias ===
1. Alta de categoria
2. Modificar categoria
3. Baja logica de categoria
0. Volver
```

Submenu de Productos:

```text
=== Productos ===
1. Alta de producto
2. Modificar producto
3. Baja logica de producto
0. Volver
```

Submenu de Reportes:

```text
=== Reportes ===
1. Productos por categoria
0. Volver
```

Las opciones de modificación, alta de producto y reportes muestran listados activos cuando son necesarios para seleccionar IDs.

## Validaciones globales de consola

1. No aceptar nombre vacío para categoria.
2. No aceptar precio menor o igual a 0.
3. No aceptar stock menor a 0.
4. En modificación, un campo de texto vacío conserva el valor anterior.
5. En modificación, un campo numérico vacío conserva el valor anterior.
6. Si el usuario ingresa un número inválido, informar el error y no persistir cambios parciales.
7. Si el usuario selecciona un ID inexistente, informar el error.
8. Si el usuario selecciona un registro dado de baja, informar el error cuando la operación requiera un activo.
9. Si no hay categorías activas, no permitir alta de producto ni reporte por categoria.
10. Si una categoria no tiene productos activos, informar explícitamente.

Sugerencia técnica: leer entradas con `Scanner.nextLine()` y parsear manualmente. Esto evita problemas comunes al mezclar `nextInt()`/`nextDouble()` con `nextLine()`.

## Salidas mínimas esperadas

### Categoria

Listado de categorías activas:

```text
ID: 1 | Nombre: Bebidas | Descripcion: Bebidas frias y calientes
```

Alta exitosa:

```text
Categoria creada correctamente. ID generado: 1
```

Baja exitosa:

```text
Categoria dada de baja correctamente: Bebidas
```

### Producto

Listado de productos activos:

```text
ID: 10 | Nombre: Cafe | Precio: 1500.00 | Stock: 20 | Categoria: Bebidas
```

Alta exitosa:

```text
Producto creado correctamente. ID generado: 10 | Categoria: Bebidas
```

Los valores `1` y `10` son ejemplos. Con IDs autogenerados, el número real depende de la secuencia de la base.

Baja exitosa:

```text
Producto dado de baja correctamente: Cafe
```

### Reporte por categoria

```text
Productos activos de la categoria Bebidas:
ID: 10 | Nombre: Cafe | Precio: 1500.00 | Stock: 20
```

Si no hay resultados:

```text
No hay productos activos para la categoria seleccionada.
```

## Matriz de evaluación

| HU    | Item evaluado                | Descripcion                                                                     | Puntaje |
|-------|------------------------------|---------------------------------------------------------------------------------|--------:|
| HU-01 | BaseRepository<T>            | CRUD generico correcto, transacciones, Optional, cierre de EntityManager.       |      18 |
| HU-02 | CategoriaRepo / ProductoRepo | Extension correcta, `super()` con `Class<T>`, `buscarPorCategoria` con JPQL.    |      12 |
| HU-03 | Alta de categoria            | Validacion de nombre, persistencia, ID visible.                                 |       8 |
| HU-04 | Modificacion de categoria    | Listado previo, error en ID invalido, campo vacio conserva valor.               |      10 |
| HU-05 | Baja de categoria            | Baja logica, error en ID invalido, no aparece en listados.                      |       7 |
| HU-06 | Alta de producto             | Listado de categorias, validacion precio/stock, relacion `@ManyToOne` resuelta. |      12 |
| HU-07 | Modificacion de producto     | Valores actuales visibles, validaciones de precio y stock.                      |      10 |
| HU-08 | Baja de producto             | Baja logica, mensaje con nombre, error en ID invalido.                          |       8 |
| HU-09 | Consulta JPQL                | JPQL correcto, `TypedQuery<Producto>`, parametro nombrado, sin casteos.         |      15 |

Adicionalmente, la entrega actual separa responsabilidades con `CatalogoService`:

| Item extra          | Descripcion                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| Servicio de negocio | `CatalogoService` encapsula reglas de alta, modificación, baja y reportes. |
| Presentación        | `com.tp.jpa.Main` queda como capa de consola y unica clase `Main`.          |
| Tests               | `CatalogoServiceTest` cubre reglas de servicio y `MainTest` cubre flujos UI. |

## Pruebas manuales integrales

Ejecutar este flujo completo antes de entregar:

1. Compilar el proyecto.
2. Ejecutar `Main`.
3. Crear una categoria con nombre y descripción.
4. Confirmar que se muestra el ID generado.
5. Listar categorías y confirmar que aparece activa.
6. Modificar nombre o descripción de la categoria.
7. Dejar un campo vacío y confirmar que conserva el valor anterior.
8. Crear un producto seleccionando la categoria activa.
9. Validar que precio `0`, precio negativo y texto no numérico no persisten.
10. Validar que stock negativo y texto no numérico no persisten.
11. Confirmar que se muestra el ID generado del producto y la categoria asignada.
12. Ejecutar reporte `Productos por categoria` y confirmar que aparece ID, nombre, precio y stock.
13. Entrar a modificación de producto y confirmar que el producto aparece en el listado de selección.
14. Dar de baja lógicamente el producto.
15. Entrar nuevamente a modificación de producto o al reporte y confirmar que ya no aparece.
16. Ejecutar nuevamente reporte `Productos por categoria` y confirmar que ya no aparece el producto dado de baja.
17. Dar de baja lógicamente una categoria.
18. Listar categorías activas y confirmar que ya no aparece.
19. Intentar modificar o dar de baja ID inexistentes y confirmar mensajes de error.
20. salir del sistema sin excepciones.

Durante estas pruebas no se debe ingresar ni calcular manualmente el ID de una entidad nueva. El sistema debe recibir los IDs únicamente después de que JPA persista el registro.

## Comandos de verificación

Usar los comandos que correspondan al proyecto base. Como referencia:

```bash
./gradlew clean build
```

También se puede ejecutar la suite de pruebas sin limpiar artefactos previos:

```bash
./gradlew test
./gradlew lintJavaFormatting
```

Tests relevantes para esta separación:

```bash
./gradlew test --tests com.tp.jpa.MainTest --tests com.tp.jpa.service.CatalogoServiceTest
```

Para ejecutar, usar la configuración existente del proyecto. Posibles alternativas:

```bash
./gradlew run
```

O ejecutar desde el IDE la clase:

```text
com.tp.jpa.Main
```

No debe existir una segunda clase `Main` en `ar.edu.tup.programacion3`; esa entrada anterior fue removida de la entrega.

El bloque `test` de `build.gradle` configura los tests de repositorios con H2 en memoria y esquema recreado. Esto evita que una base local vieja, creada antes de usar IDs identity, afecte las pruebas automatizadas.

Si existe una base local en `data/jpa_db.mv.db` creada con el esquema anterior, borrarla o recrearla antes de probar altas nuevas con IDs autogenerados.

## Guion orientativo video entregable

1. Presentacion breve del alumno.
2. Funcionamiento de la aplicación.
3. Generación de una categoria.
4. Creación de productos asociados a categoria.
5. Consulta de productos por ID/categoria.
6. Eliminación lógica de un producto de la categoria.
7. Nueva consulta de productos por categoria para comprobar que el producto eliminado ya no se muestra.
8. Explicación de funcionalidades implementadas.
9. Explicación del abordaje técnico.
10. Dificultades encontradas y como se resolvieron.
