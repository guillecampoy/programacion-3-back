# HU-21 - Reporte: Total facturado

## Resumen

**Épica:** Reportes  
**Prioridad:** Alta  
**Story Points:** 5  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** conocer el total acumulado de los pedidos en estado TERMINADO  
**Para** tener una vista rápida de la facturación del sistema

## Alcance funcional

- Reporte total facturado.
- Pedidos `TERMINADO` y activos.
- Formato de moneda.

## Criterios de aceptación

- Al seleccionar la opción, el sistema obtiene los pedidos con estado `TERMINADO` y `eliminado = false`.
- Suma los campos `total` de todos los pedidos del resultado.
- Muestra el resultado formateado, por ejemplo `Total facturado: $12500.00`.
- Si no hay pedidos terminados, muestra `$0.00`.

## Guía de implementación sugerida

- Puede reutilizar `pedidoRepo.buscarPorEstado(Estado.TERMINADO)`.
- Tratar `null` como 0 para evitar errores.
- Formatear con `String.format(Locale.US, "$%.2f", total)`.
- No incluir pedidos cancelados, pendientes, confirmados ni eliminados.
- Agregar opción en menú de reportes.

## Validación esperada

- Sin terminados muestra $0.00.
- Con un terminado muestra su total.
- Con varios terminados suma total.
- Pedido terminado eliminado no suma.
- Formato usa dos decimales.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-21 - Reporte: Total facturado.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero conocer el total acumulado de los pedidos en estado TERMINADO, para tener una vista rápida de la facturación del sistema.

Criterios de aceptación obligatorios:
1. Al seleccionar la opción, el sistema obtiene los pedidos con estado `TERMINADO` y `eliminado = false`.
2. Suma los campos `total` de todos los pedidos del resultado.
3. Muestra el resultado formateado, por ejemplo `Total facturado: $12500.00`.
4. Si no hay pedidos terminados, muestra `$0.00`.

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
