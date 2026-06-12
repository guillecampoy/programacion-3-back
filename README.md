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
jdbc:h2:file:./data/jpa_db
```

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
- 11 productos.
- 3 pedidos.
- Cada pedido tiene al menos 2 detalles.

Para cumplir estrictamente con la consigna, la persistencia inicial debe guardar al menos 10 productos. El modelo actual ya supera esa cantidad.

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

Compilar y ejecutar tests:

```bash
./gradlew build
```
