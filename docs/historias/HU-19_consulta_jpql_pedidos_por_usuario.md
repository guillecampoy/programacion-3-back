# HU-19 - Consulta JPQL: Pedidos por usuario

## Resumen

**Épica:** Reportes  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** ver todos los pedidos activos de un usuario específico  
**Para** consultar el historial de compras de un cliente

## Alcance funcional

- Reporte pedidos por usuario.
- `pedidoRepo.buscarPorUsuario(idUsuario)`.
- Salida por consola.

## Criterios de aceptación

- Se lista y selecciona un usuario activo.
- Se llama a `pedidoRepo.buscarPorUsuario(idUsuario)`.
- Se muestra ID, fecha, estado, forma de pago y total de cada pedido.
- Si el usuario no tiene pedidos activos, se informa explícitamente.
- La consulta usa `List<Pedido>` con parámetro nombrado y filtra `eliminado = false`.

## Guía de implementación sugerida

- Validar usuario activo seleccionado.
- No consultar por usuarios eliminados.
- No duplicar JPQL en Main.
- Mostrar tabla legible.
- Tratar lista vacía.

## Validación esperada

- Usuario con pedidos muestra listado.
- Usuario sin pedidos muestra mensaje.
- Pedido eliminado no aparece.
- Salida incluye forma de pago.
- Filtro por usuario no mezcla pedidos.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-19 - Consulta JPQL: Pedidos por usuario.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero ver todos los pedidos activos de un usuario específico, para consultar el historial de compras de un cliente.

Criterios de aceptación obligatorios:
1. Se lista y selecciona un usuario activo.
2. Se llama a `pedidoRepo.buscarPorUsuario(idUsuario)`.
3. Se muestra ID, fecha, estado, forma de pago y total de cada pedido.
4. Si el usuario no tiene pedidos activos, se informa explícitamente.
5. La consulta usa `List<Pedido>` con parámetro nombrado y filtra `eliminado = false`.

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
