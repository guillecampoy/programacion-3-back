# HU-17 - Cambiar estado de un pedido

## Resumen

**Épica:** Pedidos  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** actualizar el estado de un pedido existente  
**Para** reflejar el progreso de la orden en el sistema

## Alcance funcional

- Submenú pedidos opción cambiar estado.
- Selección de estado enum.
- Persistencia de cambio.

## Criterios de aceptación

- Se solicita el ID del pedido.
- Si no existe o está dado de baja, se informa el error.
- Se muestra el estado actual del pedido.
- Se permite seleccionar el nuevo estado: `PENDIENTE`, `CONFIRMADO`, `TERMINADO` o `CANCELADO`.
- El cambio se persiste correctamente.
- Se confirma mostrando el ID del pedido y su nuevo estado.

## Guía de implementación sugerida

- Buscar pedido con `buscarPorId` y validar eliminado=false.
- Mostrar opciones del enum.
- No modificar total/detalles/stock.
- Persistir con `pedidoRepo.guardar(pedido)`.
- Manejar opción inválida.

## Validación esperada

- Cambiar PENDIENTE -> CONFIRMADO.
- Cambiar CONFIRMADO -> TERMINADO.
- ID inexistente falla.
- Pedido eliminado falla.
- Reporte por estado refleja cambio.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-17 - Cambiar estado de un pedido.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero actualizar el estado de un pedido existente, para reflejar el progreso de la orden en el sistema.

Criterios de aceptación obligatorios:
1. Se solicita el ID del pedido.
2. Si no existe o está dado de baja, se informa el error.
3. Se muestra el estado actual del pedido.
4. Se permite seleccionar el nuevo estado: `PENDIENTE`, `CONFIRMADO`, `TERMINADO` o `CANCELADO`.
5. El cambio se persiste correctamente.
6. Se confirma mostrando el ID del pedido y su nuevo estado.

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
