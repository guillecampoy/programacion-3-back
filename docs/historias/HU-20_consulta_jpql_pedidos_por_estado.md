# HU-20 - Consulta JPQL: Pedidos por estado

## Resumen

**Épica:** Reportes  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** filtrar pedidos activos por su estado  
**Para** gestionar los pedidos pendientes o terminados rápidamente

## Alcance funcional

- Reporte pedidos por estado.
- `pedidoRepo.buscarPorEstado(estado)`.
- Salida por consola.

## Criterios de aceptación

- Se muestran las opciones disponibles: `PENDIENTE`, `CONFIRMADO`, `TERMINADO`, `CANCELADO`.
- Se llama a `pedidoRepo.buscarPorEstado(estado)`.
- Se muestra ID, fecha, nombre de usuario y total de cada pedido.
- Si no hay pedidos con ese estado, se informa explícitamente.
- La consulta filtra `eliminado = false`.

## Guía de implementación sugerida

- Usar enum `Estado`.
- Validar opción ingresada.
- No duplicar JPQL en Main.
- Mostrar usuario asociado sin NPE.
- Tratar lista vacía.

## Validación esperada

- Estado con pedidos muestra filas.
- Estado sin pedidos muestra mensaje.
- Pedidos eliminados no aparecen.
- Salida incluye nombre de usuario.
- Cambio de estado se refleja en consulta.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-20 - Consulta JPQL: Pedidos por estado.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero filtrar pedidos activos por su estado, para gestionar los pedidos pendientes o terminados rápidamente.

Criterios de aceptación obligatorios:
1. Se muestran las opciones disponibles: `PENDIENTE`, `CONFIRMADO`, `TERMINADO`, `CANCELADO`.
2. Se llama a `pedidoRepo.buscarPorEstado(estado)`.
3. Se muestra ID, fecha, nombre de usuario y total de cada pedido.
4. Si no hay pedidos con ese estado, se informa explícitamente.
5. La consulta filtra `eliminado = false`.

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
