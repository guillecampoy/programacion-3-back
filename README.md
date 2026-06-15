# TP JPA

Trabajo practico de Programacion III orientado a persistir el modelo de dominio con JPA e Hibernate.

La consigna se encuentra en [docs/TP - JPA.pdf](docs/TP%20-%20JPA.pdf).

## Objetivo

Disenar e implementar un modelo de dominio persistente utilizando JPA sobre las clases generadas en la practica de Lombok y DTO.

El trabajo debe permitir:

- Persistir objetos en una base de datos relacional.
- Comprender el ciclo de vida de una entidad.
- Realizar operaciones CRUD sobre el modelo.

## Consigna

Segun el documento del TP, el proyecto debe incorporar:

- Libreria de Hibernate.
- Archivo `persistence.xml`.
- Anotaciones JPA para entidades, ids y relaciones.
- Persistencia inicial de datos:
  - 2 usuarios.
  - 3 pedidos, con al menos 2 detalles por pedido.
  - 3 categorias.
  - 10 productos.
- Actualizacion de al menos 2 productos.
- Busqueda de usuario por id.
- Busqueda de usuario por mail.
- Borrado de 1 producto.

## Estructura esperada para JPA

El archivo de configuracion de persistencia debe ubicarse en:

```text
src/main/resources/META-INF/persistence.xml
```

La unidad de persistencia indicada por la consigna es:

```xml
<persistence-unit name="miUnidad" transaction-type="RESOURCE_LOCAL">
```

La base de datos sugerida es H2 en archivo:

```text
jdbc:h2:file:./data/jpa_db;AUTO_SERVER=TRUE
```

La carpeta `data/` no se versiona. La aplicacion genera nuevamente la base local cuando no detecta `data/jpa_db.mv.db`.

Las clases del dominio deben anotarse como entidades JPA y registrar sus relaciones correspondientes.

## Modelo del proyecto

El diagrama del modelo se encuentra en [docs/diagrama.puml](docs/diagrama.puml).

Entidades del dominio:

- `Base`
- `Usuario`
- `Categoria`
- `Producto`
- `Pedido`
- `DetallePedido`

Relaciones principales:

- `Usuario` tiene una coleccion de `Pedido`.
- `Pedido` pertenece a un `Usuario`.
- `Pedido` tiene una coleccion de `DetallePedido`.
- `DetallePedido` referencia un `Producto`.
- `Producto` pertenece a una `Categoria`.
- `Categoria` tiene una coleccion de `Producto`.

Enums:

- `Estado`
- `FormaPago`
- `Rol`

DTO y datos semilla:

- `UsuarioDTO`
- `DatosSemilla`
- `DatosSemillaFactory`

Interfaz:

- `Calculable`

## Datos de prueba actuales

La clase `DatosSemillaFactory` instancia datos en memoria que sirven como base para el TP JPA:

- 2 usuarios.
- 3 categorias.
- 10 productos.
- 3 pedidos.
- Cada pedido tiene al menos 2 detalles.

La persistencia inicial guarda exactamente 10 productos, segun la consigna.

## Operaciones JPA requeridas

La implementacion del TP debe cubrir el siguiente flujo:

1. Crear un `EntityManagerFactory` usando la unidad `miUnidad`.
2. Crear un `EntityManager`.
3. Abrir una transaccion.
4. Persistir usuarios, categorias, productos, pedidos y detalles.
5. Confirmar la transaccion.
6. Actualizar al menos 2 productos.
7. Buscar un usuario por `id`.
8. Buscar un usuario por `mail`.
9. Borrar 1 producto.
10. Cerrar `EntityManager` y `EntityManagerFactory`.

## Test de integracion JPA

El test `JpaIntegrationTest` valida el flujo end to end solicitado por la consigna:

- Crea un `EntityManagerFactory` con la unidad de persistencia `miUnidad`.
- Sobrescribe la URL JDBC con `jdbc:h2:mem:jpa_integration_test;DB_CLOSE_DELAY=-1`.
- Crea una base H2 en memoria para el test.
- No usa ni modifica la base de desarrollo `data/jpa_db.mv.db`.
- Valida explicitamente que la URL de test empiece con `jdbc:h2:mem:` y que no contenga `./data/jpa_db`.
- Limpia las tablas del dominio antes de persistir la semilla.
- Persiste los datos de `DatosSemillaFactory`.
- Valida que existan 2 usuarios, 3 categorias, 10 productos y 3 pedidos.
- Valida que cada pedido tenga usuario y al menos 2 detalles.
- Valida que cada detalle referencie un producto y tenga subtotal positivo.
- Actualiza 2 productos.
- Busca un usuario por `id`.
- Busca un usuario por `mail`.
- Borra 1 producto.
- Cierra los recursos de JPA al finalizar.

El test `PersistenciaInicialTest` valida especificamente que, si la base H2 local no existe, el inicializador crea una base nueva y persiste la semilla. Para no tocar la base de desarrollo, usa un directorio temporal de JUnit y una URL `jdbc:h2:file:` apuntando a ese temporal.

## Conclusiones esperadas

El test de integracion deja cubiertos los puntos finales de la consigna:

- Persistir objetos en base de datos.
- Comprender el ciclo de vida de una entidad mediante persistencia, busqueda, actualizacion y borrado.
- Comprender operaciones CRUD sobre entidades JPA.

## Dependencias esperadas

Ademas de Lombok y JUnit, el proyecto debe incluir dependencias para:

- Hibernate ORM.
- Jakarta Persistence API.
- H2 Database.

## Ejecucion

Requisitos:

- Java instalado.
- Gradle Wrapper incluido en el proyecto.
- Dependencias configuradas en `build.gradle`.

Ejecutar desde la raiz del repositorio:

```bash
./gradlew run
```

Mientras el programa esta corriendo, deja disponible la consola web local de H2:

```text
URL: http://localhost:8082
JDBC URL: jdbc:h2:file:./data/jpa_db;AUTO_SERVER=TRUE
Usuario: sa
Password: dejar vacio
```

El programa debe quedar abierto para conectarse desde la consola. Si se cierra con la opcion `0`, tambien se cierra la consola web.

Opciones del menu:

```text
1 - Mostrar estado
2 - Borrar base local y reinstanciar semilla
3 - Actualizar 2 productos
4 - Buscar usuario por id
5 - Buscar usuario por mail parcial
0 - Salir
```

La opcion `2` cierra JPA y la consola H2, borra los archivos locales `data/jpa_db.mv.db`, `data/jpa_db.trace.db` y `data/jpa_db.lock.db` si existen, vuelve a crear la base y persiste otra vez la semilla inicial: 2 usuarios, 3 pedidos con al menos 2 detalles por pedido, 3 categorias y 10 productos.

La opcion `3` obliga a actualizar 2 productos distintos. Para cada producto:

- Muestra la lista de productos disponibles.
- Pide un id existente y no repetido.
- Construye dinamicamente el menu de atributos editables.
- Permite editar `nombre`, `precio`, `descripcion`, `stock`, `imagen`, `disponible` o `categoria`.
- No tiene opcion por defecto: el usuario debe elegir explicitamente que atributo actualizar.
- Muestra el valor actual o el detalle correspondiente antes de pedir el nuevo valor.
- Valida estrictamente las entradas: textos no vacios, stock entero mayor o igual a 0, precio decimal mayor o igual a 0.01, disponible como `si/no`, `s/n` o `true/false` y categoria existente.

La validacion de entradas esta centralizada en `EntradaValidada` para reutilizarla en las proximas opciones del menu.

La opcion `4` busca un usuario por id. Valida que el id sea numerico y mayor a 0, y muestra el detalle del usuario si existe.

La opcion `5` busca usuarios por coincidencia parcial de mail usando `like`. Permite ingresar fragmentos como `gmail`, `bruno` o `@gmail.com`; valida que el texto no este vacio ni tenga espacios y muestra todos los usuarios encontrados.

Compilar y ejecutar tests:

```bash
./gradlew build
```

## Salida esperada

Al ejecutar `./gradlew run`, el programa muestra:
- Si la base H2 local existia al iniciar.
- Si se persistieron los datos iniciales.
- El total actual de usuarios, categorias, productos y pedidos.
- Los datos de conexion para la consola web H2 local.
- El menu con opcion explicita de salida.
