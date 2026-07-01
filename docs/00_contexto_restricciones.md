# Backend - Contexto, restricciones y reglas de implementación

## Objetivo

Completar la Parte 2 del TPI Food Store: backend de consola con Java, Gradle, JPA/Hibernate y H2 en archivo. No es una API REST. El objetivo es persistencia, modelo de dominio, repositorios, soft delete, JPQL, transacciones y menú navegable.

## Estructura esperada

```text
backend/
├── build.gradle
├── settings.gradle
├── data/                         # H2 en archivo, puede regenerarse
├── src/main/java/com/tp/jpa/
│   ├── Main.java                  # menú de consola
│   ├── model/
│   │   ├── Base.java
│   │   ├── Categoria.java
│   │   ├── Producto.java
│   │   ├── Usuario.java
│   │   ├── Pedido.java
│   │   ├── DetallePedido.java
│   │   └── Calculable.java
│   ├── model/enums/
│   │   ├── Rol.java
│   │   ├── Estado.java
│   │   └── FormaPago.java
│   ├── repository/
│   │   ├── BaseRepository.java
│   │   ├── CategoriaRepository.java
│   │   ├── ProductoRepository.java
│   │   ├── UsuarioRepository.java
│   │   └── PedidoRepository.java
│   └── util/
│       └── JPAUtil.java
└── src/main/resources/META-INF/
    └── persistence.xml
```

## Persistencia

- Base de datos H2 en archivo: `jdbc:h2:file:./data/jpa_db`.
- Hibernate gestiona esquema con `hbm2ddl.auto = update`.
- `JPAUtil` mantiene una única instancia de `EntityManagerFactory`.
- `JPAUtil.close()` debe ejecutarse al salir por opción `0` del menú principal.

## Modelo de dominio obligatorio

- `Base` abstracta con `@MappedSuperclass`: `id Long` autogenerado con `@GeneratedValue`, `eliminado boolean` default `false`, `createdAt LocalDateTime`.
- `Calculable`: interfaz con `calcularTotal(): void`.
- Entidades JPA: `Categoria`, `Producto`, `Usuario`, `Pedido`, `DetallePedido`.
- `Pedido` implementa `Calculable`; `calcularTotal()` suma subtotales y asigna `this.total`. No retorna valor.

## Campos por entidad

- `Categoria`: `nombre`, `descripcion`.
- `Producto`: `nombre`, `precio`, `descripcion`, `stock`, `imagen`, `disponible`, `categoria`.
- `Usuario`: `nombre`, `apellido`, `mail` único a nivel lógico, `celular`, `contrasena`, `rol`.
- `Pedido`: `fecha`, `estado`, `total`, `formaPago`, `usuario`, `detalles`.
- `DetallePedido`: `cantidad`, `subtotal`, `producto`, `pedido`.

## Relaciones JPA relevantes

- `Producto -> Categoria`: `@ManyToOne @JoinColumn(name="categoria_id")`; relación unidireccional desde producto.
- `Pedido -> DetallePedido`: composición bidireccional. `Pedido` mantiene `List<DetallePedido> detalles = new ArrayList<>()` con `@OneToMany(mappedBy="pedido", cascade=CascadeType.ALL, orphanRemoval=true)`.
- `DetallePedido -> Pedido`: `@ManyToOne`; el campo debe llamarse exactamente `pedido` para coincidir con `mappedBy="pedido"`.
- `DetallePedido -> Producto`: `@ManyToOne @JoinColumn(name="producto_id")`.
- `Usuario -> Pedido`: `@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY) @JoinColumn(name="usuario_id")`; para consultar por usuario usar `PedidoRepository.buscarPorUsuario`.
- `addDetallePedido(int cantidad, Producto producto)` es el único punto de creación de `DetallePedido`: calcula subtotal, setea `detalle.setPedido(this)` y agrega a `detalles`.

## Enumerados

- `Rol`: `ADMIN`, `USUARIO`.
- `Estado`: `PENDIENTE`, `CONFIRMADO`, `TERMINADO`, `CANCELADO`.
- `FormaPago`: `TARJETA`, `TRANSFERENCIA`, `EFECTIVO`.

## Reglas técnicas fuertes

- No usar Spring Boot ni API REST para este entregable.
- Repositorios en paquete `repository`.
- `Main` en paquete `com.tp.jpa`.
- Cada método de repositorio abre su propio `EntityManager` y lo cierra en `finally`, salvo el flujo especial de alta de pedido, que requiere una transacción única controlada desde el flujo.
- Escrituras (`guardar`, `eliminarLogico`) usan `begin()`, `commit()` y `rollback()` ante excepción.
- `guardar()` usa `persist()` si `id == null`; usa `merge()` si ya tiene id. El ID generado debe leerse desde la entidad retornada por `guardar()`.
- `buscarPorId()` retorna `Optional<T>`.
- `listarActivos()` usa JPQL con `WHERE e.eliminado = false`, construido con `entityClass.getSimpleName()`.
- `eliminarLogico()` nunca borra físicamente: setea `eliminado = true`.
- Todas las bajas son lógicas. Los registros permanecen en BD y no deben aparecer en listados activos.
- Los métodos JPQL personalizados deben tener comentario breve que explique qué consulta hacen.
- Las modificaciones por consola deben permitir dejar campos en blanco para conservar el valor anterior.
- No pisar campos con `null` ni cadena vacía cuando el operador deja el valor en blanco.
- No mezclar entidades gestionadas por distintos `EntityManager`.
- En alta de pedido, la lista temporal previa debe guardar `idProducto` y `cantidad`, no entidades JPA.

## Alta de pedido: regla transaccional crítica

El alta de pedido se ejecuta dentro de una única transacción atómica:

1. Validar usuario, forma de pago, productos, disponibilidad y stock.
2. Si la lista temporal queda vacía, cancelar sin persistir.
3. Abrir un único `EntityManager`.
4. Iniciar una única transacción.
5. Recuperar usuario/productos con `em.find()` dentro de ese mismo `EntityManager`.
6. Crear `Pedido` con fecha actual, estado `PENDIENTE`, forma de pago y usuario.
7. Para cada ítem, crear detalle vía `pedido.addDetallePedido(cantidad, producto)`.
8. Reducir stock del producto gestionado.
9. Invocar `pedido.calcularTotal()` y luego leer `pedido.getTotal()`.
10. Persistir `Pedido` con `em.persist(pedido)` y cascade a detalles.
11. Hacer `commit()`.
12. Ante cualquier validación fallida dentro de la transacción, hacer `rollback()` completo.

## Validación general

```bash
./gradlew clean build
./gradlew run
```

Validar manualmente: crear 2 categorías, 3 productos, 2 usuarios, crear pedido con 2 productos, ver subtotales/total, cambiar estado a `CONFIRMADO` y `TERMINADO`, consultar reportes por usuario, por estado y total facturado, y probar baja lógica de producto.
