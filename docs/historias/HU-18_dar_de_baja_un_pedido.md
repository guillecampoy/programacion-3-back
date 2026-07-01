# HU-18 - Dar de baja un pedido

## Resumen

**Épica:** Pedidos  
**Prioridad:** Alta  
**Story Points:** 6  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** dar de baja un pedido incorrecto o cancelado  
**Para** ocultarlo del sistema sin perder el historial

## Alcance funcional

- Submenú pedidos opción baja.
- Baja lógica.
- No restaurar stock.
- Detalles permanecen.

## Criterios de aceptación

- La baja es lógica: `eliminado = true`, el registro permanece en la BD.
- Si el ID no existe o ya está dado de baja, se informa el error.
- El stock de los productos NO se restaura.
- Los `DetallePedido` permanecen en la BD.
- Se confirma mostrando el ID y el total del pedido dado de baja.

## Guía de implementación sugerida

- Obtener pedido activo antes de baja para mostrar total.
- Usar `pedidoRepo.eliminarLogico(id)` o lógica equivalente.
- No recorrer detalles para restaurar stock.
- No borrar detalles.
- Excluir pedido de listados activos/reportes.

## Validación esperada

- Baja válida confirma ID y total.
- Stock no cambia luego de baja.
- Pedido no aparece activo.
- Detalles permanecen consultables por BD.
- Segundo intento informa error.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-18 - Dar de baja un pedido.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero dar de baja un pedido incorrecto o cancelado, para ocultarlo del sistema sin perder el historial.

Criterios de aceptación obligatorios:
1. La baja es lógica: `eliminado = true`, el registro permanece en la BD.
2. Si el ID no existe o ya está dado de baja, se informa el error.
3. El stock de los productos NO se restaura.
4. Los `DetallePedido` permanecen en la BD.
5. Se confirma mostrando el ID y el total del pedido dado de baja.

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
