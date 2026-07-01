# HU-09 - Modificar un producto

## Resumen

**Épica:** Productos  
**Prioridad:** Alta  
**Story Points:** 10  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** actualizar el nombre, precio y stock de un producto existente  
**Para** mantener el catálogo actualizado sin recrear el registro

## Alcance funcional

- Submenú productos opción modificar.
- Listado de productos activos.
- Campos en blanco conservan valor.
- Validación de precio/stock.

## Criterios de aceptación

- El sistema lista los productos activos antes de pedir el ID.
- Si el ID no existe o el producto está dado de baja, se muestra error.
- Se muestran los valores actuales antes de pedir los nuevos.
- Dejar un campo en blanco conserva el valor anterior.
- Precio no puede actualizarse a un valor menor o igual a 0.
- Stock no puede actualizarse a un valor negativo.

## Guía de implementación sugerida

- Validar id contra listado activo.
- Permitir modificar nombre, descripción, precio, stock, imagen, disponible y categoría si el proyecto ya lo contempla; mínimo nombre/precio/stock.
- No pisar con null/cadena vacía.
- Validar números solo si se ingresa valor.
- Persistir con `productoRepo.guardar(producto)`.

## Validación esperada

- Campo vacío conserva valor.
- Precio 0 falla y no persiste.
- Stock negativo falla y no persiste.
- Producto eliminado no se modifica.
- Cambio persiste tras reinicio.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-09 - Modificar un producto.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero actualizar el nombre, precio y stock de un producto existente, para mantener el catálogo actualizado sin recrear el registro.

Criterios de aceptación obligatorios:
1. El sistema lista los productos activos antes de pedir el ID.
2. Si el ID no existe o el producto está dado de baja, se muestra error.
3. Se muestran los valores actuales antes de pedir los nuevos.
4. Dejar un campo en blanco conserva el valor anterior.
5. Precio no puede actualizarse a un valor menor o igual a 0.
6. Stock no puede actualizarse a un valor negativo.

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
