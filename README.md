# TP ToString - Colecciones

Trabajo practico de Programacion III sobre modelado de objetos, colecciones `Set` y sobrescritura de metodos en Java.

## Objetivo

Desarrollar y procesar colecciones de objetos en Java, implementando `toString()` para mostrar los datos de forma clara por consola.

## Consigna

A partir del UML incluido en [docs/PROGRAMACION III ToString-Colecciones.pdf](docs/PROGRAMACI%C3%93N%20III%20ToString-Colecciones.pdf), se deben implementar las clases y relaciones del modelo usando colecciones de tipo `Set`.

En cada clase corresponde sobrescribir:

- `toString()`
- `equals()`
- `hashCode()`

## Modelo del proyecto

El proyecto contiene las siguientes entidades base:

- `Usuario`
- `Pedido`
- `DetallePedido`
- `Producto`
- `Categoria`
- `Base`

Tambien incluye enums para representar datos del dominio:

- `Estado`
- `FormaPago`
- `Rol`

## Datos a instanciar

En la clase `Main` se deben crear:

- 2 usuarios
- 3 pedidos, con al menos 2 detalles por pedido
- 3 categorias
- 10 productos

Luego se debe mostrar por consola:

- Un producto usando `toString()`
- El listado de productos cargados
- Los pedidos del usuario que mas pedidos tenga
- La comparacion de un producto nuevo contra la coleccion, usando los campos definidos en `equals()`

## Como ejecutar

Requisitos:

- Java instalado
- Gradle Wrapper incluido en el proyecto

Ejecutar desde la raiz del repositorio:

```bash
./gradlew run
```

Si el plugin `application` no esta configurado, se puede compilar con:

```bash
./gradlew build
```

## Documento original

La consigna completa se encuentra en:

[docs/PROGRAMACION III ToString-Colecciones.pdf](docs/PROGRAMACI%C3%93N%20III%20ToString-Colecciones.pdf)
