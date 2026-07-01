# HU-07 - Dar de baja una categoría

## Resumen

**Épica:** Categorías  
**Prioridad:** Alta  
**Story Points:** 7  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** dar de baja una categoría que ya no se utiliza  
**Para** ocultarla del sistema sin perder el historial de datos

## Alcance funcional

- Submenú categorías opción baja.
- Baja lógica.
- Confirmación por nombre.

## Criterios de aceptación

- La baja es lógica: se establece `eliminado = true`, el registro permanece en la BD.
- Si el ID no existe o ya está dado de baja, el sistema informa el error.
- La categoría dada de baja no aparece en ningún listado activo.
- Se confirma la operación mostrando el nombre de la categoría afectada.

## Guía de implementación sugerida

- Buscar categoría activa antes de eliminar para obtener nombre.
- Usar `categoriaRepo.eliminarLogico(id)`.
- No borrar productos relacionados.
- No hacer delete físico.
- Volver al submenú.

## Validación esperada

- Baja válida confirma nombre.
- Listar activos ya no muestra categoría.
- Segundo intento informa error.
- Registro sigue en BD con eliminado=true.
- Productos históricos no se pierden.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-07 - Dar de baja una categoría.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero dar de baja una categoría que ya no se utiliza, para ocultarla del sistema sin perder el historial de datos.

Criterios de aceptación obligatorios:
1. La baja es lógica: se establece `eliminado = true`, el registro permanece en la BD.
2. Si el ID no existe o ya está dado de baja, el sistema informa el error.
3. La categoría dada de baja no aparece en ningún listado activo.
4. Se confirma la operación mostrando el nombre de la categoría afectada.

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
