# HU-07 - Modificar un producto

## Metadata

- ID: HU-07
- Titulo: Modificar un producto
- Prioridad: Alta
- Story Points: 10
- Area: Consola / ABM Productos
- Archivo principal a modificar: `src/main/java/com/tp/jpa/Main.java`

## Historia de usuario

Como operador del sistema, quiero poder actualizar el nombre, precio, stock y categoria de un producto existente para mantener el catalogo actualizado sin recrear el registro.

## Dependencias

Esta historia depende de:

- `HU-01 - Repositorio generico con CRUD`.
- `HU-02 - Repositorios especificos de Categoria y Producto`.
- `HU-06 - Dar de alta un producto`.

## Alcance funcional

Implementar en `Main` una opcion de modificacion de producto dentro del submenu de Productos.

La opcion debe:

1. Listar productos activos antes de pedir ID.
2. Solicitar ID de producto.
3. Validar que exista.
4. Validar que este activo.
5. Mostrar valores actuales.
6. Permitir editar nombre, precio, stock y categoria.
7. Conservar valor anterior si el usuario deja un campo vacio.
8. Validar precio mayor a 0 cuando se ingrese nuevo precio.
9. Validar stock mayor o igual a 0 cuando se ingrese nuevo stock.
10. Permitir reasignar el producto solo a una categoria activa existente.
11. Persistir cambios usando `ProductoRepository.guardar(...)`.

## Menu esperado

Ruta:

```text
Menu principal -> Productos -> Modificar producto
```

Ejemplo:

```text
=== Modificar producto ===
Productos activos:
ID: 10 | Nombre: Cafe | Precio: 1500.00 | Stock: 20 | Categoria: Bebidas
Ingrese ID de producto: 10
Valores actuales:
Nombre actual: Cafe
Precio actual: 1500.00
Stock actual: 20
Categoria actual: Bebidas
Nuevo nombre (enter para conservar): Cafe premium
Nuevo precio (enter para conservar): 1800.00
Nuevo stock (enter para conservar): 
Nueva categoria (enter para conservar): 2
Producto modificado correctamente.
```

## Reglas de negocio

1. Solo se pueden modificar productos activos.
2. Si el ID no existe, mostrar error.
3. Si el producto esta dado de baja, mostrar error.
4. Campo vacio conserva valor anterior.
5. Precio nuevo debe ser mayor a 0.
6. Stock nuevo debe ser mayor o igual a 0.
7. La consigna pide modificar nombre, precio, stock y categoria. No agregar cambio de descripcion en esta historia.

## Reglas tecnicas

1. Usar `ProductoRepository.listarActivos()` para mostrar productos.
2. Usar `ProductoRepository.buscarPorId(id)` para obtener producto.
3. Validar `eliminado` usando el getter real.
4. Usar `ProductoRepository.guardar(producto)` para persistir.
5. No usar `EntityManager` directamente desde `Main`.
6. No modificar `Producto`.
7. Confirmar tipo real de `precio` antes de parsear.
8. Confirmar tipo real de `stock` antes de parsear.
9. Manejar parseo invalido sin persistir cambios parciales.
10. Si una validacion falla, no guardar el producto.

## Criterios de aceptacion

1. El sistema lista los productos activos antes de pedir el ID.
2. Si no hay productos activos, se informa y se cancela la operacion.
3. Si el ID no existe, se muestra error.
4. Si el producto esta dado de baja, se muestra error.
5. Se muestran los valores actuales antes de pedir los nuevos.
6. Dejar nombre en blanco conserva el valor anterior.
7. Dejar precio en blanco conserva el valor anterior.
8. Dejar stock en blanco conserva el valor anterior.
9. Precio no puede actualizarse a un valor menor o igual a 0.
10. Stock no puede actualizarse a un valor negativo.
11. El cambio se persiste correctamente.
12. La opcion es accesible desde el menu principal.
13. El proyecto compila.

## Fuera de alcance

No implementar en esta historia:

1. Cambio de descripcion del producto.
2. Cambio a una categoria inexistente o inactiva.
3. Baja de producto.
4. Reporte por categoria.

## Pruebas manuales

1. Crear categoria.
2. Crear producto.
3. Entrar a modificar producto.
4. Confirmar que se listan productos activos.
5. Cambiar solo nombre y dejar precio/stock/categoria vacios.
6. Confirmar que conserva precio, stock y categoria.
7. Intentar reasignar a una categoria inexistente o inactiva y confirmar error.
8. Intentar precio `0` y confirmar error.
9. Intentar precio negativo y confirmar error.
10. Intentar stock negativo y confirmar error.
11. Ingresar valores validos y confirmar persistencia.
12. Intentar ID inexistente.
13. Confirmar mensaje de error.

## Checklist de terminado

- [ ] Lista productos activos antes de pedir ID.
- [ ] Maneja caso sin productos activos.
- [ ] Valida ID inexistente.
- [ ] Valida producto eliminado.
- [ ] Muestra valores actuales.
- [ ] Campo vacio conserva valor.
- [ ] Valida precio > 0.
- [ ] Valida stock >= 0.
- [ ] Permite reasignar categoria activa.
- [ ] Usa `ProductoRepository.guardar`.
- [ ] Compila.

## Prompt sugerido para agente

```text
Implementa la historia HU-07 en Main.java. Agrega modificacion de producto: listar productos activos, pedir ID, validar existencia y activo, mostrar valores actuales, permitir editar nombre, precio, stock y categoria conservando valores con enter vacio, validar precio > 0 y stock >= 0, permitir reasignar solo a categorias activas existentes y persistir con ProductoRepository.guardar. No agregues cambio de descripcion. Maneja errores de input. Compila al finalizar.
```
