# HU-16 - Dar de alta un pedido con detalles

## Resumen

**Épica:** Pedidos  
**Prioridad:** Alta  
**Story Points:** 20  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** registrar un pedido completo con sus productos y cantidades  
**Para** registrar una compra y actualizar el inventario automáticamente

## Alcance funcional

- Submenú pedidos opción alta.
- Selección de usuario activo.
- Forma de pago.
- Ciclo de productos.
- DetallePedido vía `Pedido.addDetallePedido`.
- Transacción única atómica.
- Reducción de stock.
- Cálculo de total.

## Criterios de aceptación

- Se lista y selecciona un usuario activo para asociar al pedido.
- Se selecciona la forma de pago: `TARJETA`, `TRANSFERENCIA` o `EFECTIVO`.
- Se muestran los productos activos y disponibles para seleccionar.
- Por cada producto se valida que exista, que `disponible = true` y que tenga stock suficiente.
- Si alguna validación falla después de iniciar la transacción, se hace rollback completo.
- El subtotal de cada `DetallePedido` se calcula como `precio * cantidad`.
- El total del `Pedido` se calcula llamando a `calcularTotal()` como suma de subtotales.
- El stock de cada producto se reduce al confirmar el pedido.
- El estado inicial es `PENDIENTE` y la fecha es la fecha actual.
- Toda la operación ocurre dentro de una única transacción atómica.
- Al guardar se muestra ID generado, total, usuario y resumen de productos con cantidades y subtotales.

## Guía de implementación sugerida

- Antes de abrir transacción, construir lista temporal de `idProducto` y `cantidad`.
- No guardar entidades JPA gestionadas por otro EntityManager en esa lista.
- Dentro de la transacción recuperar usuario/productos con `em.find()`.
- Crear Pedido y detalles dentro del mismo contexto.
- Reducir stock sobre productos gestionados.
- Usar `em.persist(pedido)` y cascade para detalles.
- Rollback ante excepción o validación crítica.
- No confirmar pedido sin al menos un detalle.

## Validación esperada

- Pedido con 2 productos guarda detalles y total correcto.
- Stock se reduce correctamente.
- Stock insuficiente cancela sin cambios parciales.
- Producto no disponible no se puede agregar.
- Pedido inicia PENDIENTE con fecha actual.
- DetallePedido queda asociado a Pedido y Producto.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-16 - Dar de alta un pedido con detalles.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero registrar un pedido completo con sus productos y cantidades, para registrar una compra y actualizar el inventario automáticamente.

Criterios de aceptación obligatorios:
1. Se lista y selecciona un usuario activo para asociar al pedido.
2. Se selecciona la forma de pago: `TARJETA`, `TRANSFERENCIA` o `EFECTIVO`.
3. Se muestran los productos activos y disponibles para seleccionar.
4. Por cada producto se valida que exista, que `disponible = true` y que tenga stock suficiente.
5. Si alguna validación falla después de iniciar la transacción, se hace rollback completo.
6. El subtotal de cada `DetallePedido` se calcula como `precio * cantidad`.
7. El total del `Pedido` se calcula llamando a `calcularTotal()` como suma de subtotales.
8. El stock de cada producto se reduce al confirmar el pedido.
9. El estado inicial es `PENDIENTE` y la fecha es la fecha actual.
10. Toda la operación ocurre dentro de una única transacción atómica.
11. Al guardar se muestra ID generado, total, usuario y resumen de productos con cantidades y subtotales.

Restricciones:
- Mantener el alcance acotado a esta historia.
- No cambiar estructura general salvo que sea imprescindible y justificado.
- No introducir frameworks no pedidos.
- Devolver resumen del diff, archivos tocados, validaciones ejecutadas y riesgos pendientes.
- No hacer commit automáticamente; dejar el cambio listo para revisión humana.
```

## Checklist de revisión humana

- [ ] Leí el diff completo.
- [ ] Ejecuté la aplicación o build correspondiente.
- [ ] Probé el caso feliz.
- [ ] Probé al menos un caso de error/validación.
- [ ] Confirmé que no se rompió una historia anterior.
- [ ] Dejé nota de deuda técnica si aplica.
