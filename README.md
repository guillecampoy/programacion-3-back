# Programacion III - Backend JPA

Backend de consola para el TPI de Programacion III con Java, Gradle, JPA/Hibernate y H2 en archivo.

La referencia de entrega para este proyecto es `docs/TPI.pdf`, contrastada con `docs/diagrama.puml` para preservar las relaciones del dominio. En este repo se implementa el backend evaluable y, aparte, se conservan algunas utilidades de prueba y recuperacion que no cambian la consigna principal.

## Fuente de verdad y criterio de alcance

1. `docs/TPI.pdf` define las historias, las firmas esperadas y la rubricacion del backend.
2. `docs/diagrama.puml` se toma como contrato fijo de relaciones entre entidades.
3. Cuando el PDF y una utilidad local no coinciden, la entrega prioriza el PDF.
4. Las extensiones locales se mantienen separadas para no contaminar lo evaluable.

## Requerido por la rubrica

### Dominio

1. `Base` centraliza `id`, `eliminado` y `createdAt`.
2. `Categoria`, `Producto`, `Usuario`, `Pedido` y `DetallePedido` conforman el modelo persistente.
3. `Pedido` implementa `Calculable` y compone sus detalles.
4. `Producto` referencia una `Categoria`.
5. `Usuario` referencia sus `Pedido`.
6. `DetallePedido` referencia `Producto`.

### Repositorios

1. `BaseRepository<T>` implementa `guardar`, `buscarPorId`, `listarActivos` y `eliminarLogico`.
2. `CategoriaRepository` solo extiende la base.
3. `ProductoRepository.buscarPorCategoria(Long)` usa JPQL tipado con `:catId`.
4. `UsuarioRepository.buscarPorMail(String)` usa JPQL tipado con `:mail` y retorna `Optional<Usuario>`.
5. `PedidoRepository.buscarPorUsuario(Long)` y `buscarPorEstado(Estado)` usan JPQL tipado.

### Consola

1. El menu principal queda ordenado como `Categorias`, `Productos`, `Usuarios`, `Pedidos`, `Reportes`.
2. El submenu de categorias cubre alta, modificacion, baja logica y listado.
3. El submenu de productos cubre alta, modificacion, baja logica y listado.
4. El submenu de usuarios cubre alta, modificacion, baja logica y busqueda por mail.
5. El submenu de pedidos cubre alta con detalles, cambio de estado y baja logica.
6. El submenu de reportes cubre productos por categoria, pedidos por usuario, pedidos por estado y total facturado.

### Validacion

1. La operacion de alta de pedido corre en una transaccion unica.
2. Las bajas son logicas.
3. Los campos en blanco en modificaciones conservan el valor anterior.
4. Al salir de la aplicacion se cierra `JPAUtil`.

## Consideraciones adicionales

Estas opciones no forman parte del recorrido evaluable principal, pero se mantienen porque ayudan a probar el sistema sin tocar el contrato del modelo:

1. `Regenerar datos`.
2. `Revertir baja logica de categoria`.
3. `Revertir baja logica de producto`.

El menu las muestra separadas del bloque requerido, con una subtitulo propio, para que quede claro que son auxiliares de prueba y no cambian el orden de la entrega.

### Criterio aplicado

1. La busqueda de usuario por mail se dejo exacta porque eso es lo que pide la rubrica; la coincidencia parcial quedo solo como posible ampliacion futura.
2. El menu principal se reordeno para que la ruta evaluable quede primero y las utilidades de prueba queden al final.
3. El listado de productos agrega `disponible` porque el PDF lo pide; el reporte por categoria no arrastra columnas extra.
4. Las opciones de restauracion y regeneracion se conservaron porque facilitan las pruebas manuales, pero no reemplazan el recorrido principal de la entrega.

## Estado actual

1. El repositorio ya cubre las historias de backend del PDF.
2. Los tests automaticos cubren repositorios, servicios, modelo, utilidades y flujo de consola.
3. El orden del menu principal y los reportes se alinean con la rubrica.
4. Las utilidades adicionales quedan documentadas aparte.

## Estructura actual

```text
src/main/java/com/tp/jpa/
  Main.java
  H2LocalConsole.java
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
    UsuarioRepository.java
    PedidoRepository.java
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
    ManejoErroresConsola.java

src/test/java/com/tp/jpa/
  MainTest.java
  integration/JpaIntegrationTest.java
  model/RelacionesTest.java
  repository/CategoriaRepositoryTest.java
  repository/ProductoRepositoryTest.java
  repository/PedidoRepositoryTest.java
  repository/UsuarioRepositoryTest.java
  seed/PersistenciaInicialTest.java
  service/CatalogoServiceTest.java
  service/CatalogoServicePedidosTest.java
  util/EntradaValidadaTest.java
  util/ManejoErroresConsolaTest.java
```

## Validacion

Los comandos que se usan como cierre son:

```bash
./gradlew test
./gradlew check
```

`test` valida el comportamiento funcional. `check` agrega Spotless y confirma que el cierre no deja desalineaciones de formato.
