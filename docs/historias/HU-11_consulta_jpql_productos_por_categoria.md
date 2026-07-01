# HU-11 - Consulta JPQL: Productos por categoría

## Resumen

**Épica:** Productos  
**Prioridad:** Alta  
**Story Points:** 10  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** ver todos los productos activos que pertenecen a una categoría específica  
**Para** consultar el catálogo filtrado sin revisar todos los productos

## Alcance funcional

- Reporte/consulta de productos por categoría.
- `ProductoRepository.buscarPorCategoria`.
- Salida por consola.

## Criterios de aceptación

- El sistema lista las categorías activas para que el operador seleccione una.
- La consulta está implementada en `ProductoRepository` con JPQL y parámetro nombrado `:catId`.
- El método retorna `List<Producto>` sin casteos manuales.
- Solo se incluyen productos con `eliminado = false`.
- El resultado muestra ID, nombre, precio y stock de cada producto.
- Si la categoría no tiene productos activos, se informa explícitamente.

## Guía de implementación sugerida

- No duplicar consulta en Main.
- Validar categoría activa antes de consultar.
- Mostrar tabla simple en consola.
- Tratar lista vacía con mensaje claro.
- Mantener comentario JPQL en repository.

## Validación esperada

- Categoría con productos muestra filas.
- Categoría sin productos muestra mensaje.
- Producto eliminado no aparece.
- ID de categoría inválido falla.
- Salida incluye ID/nombre/precio/stock.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-11 - Consulta JPQL: Productos por categoría.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero ver todos los productos activos que pertenecen a una categoría específica, para consultar el catálogo filtrado sin revisar todos los productos.

Criterios de aceptación obligatorios:
1. El sistema lista las categorías activas para que el operador seleccione una.
2. La consulta está implementada en `ProductoRepository` con JPQL y parámetro nombrado `:catId`.
3. El método retorna `List<Producto>` sin casteos manuales.
4. Solo se incluyen productos con `eliminado = false`.
5. El resultado muestra ID, nombre, precio y stock de cada producto.
6. Si la categoría no tiene productos activos, se informa explícitamente.

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
