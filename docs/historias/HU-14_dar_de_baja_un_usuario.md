# HU-14 - Dar de baja un usuario

## Resumen

**Épica:** Usuarios  
**Prioridad:** Alta  
**Story Points:** 6  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** dar de baja un usuario que ya no utiliza el sistema  
**Para** ocultarlo sin perder el historial de pedidos

## Alcance funcional

- Submenú usuarios opción baja.
- Baja lógica.
- Pedidos permanecen.

## Criterios de aceptación

- La baja es lógica: `eliminado = true`, el registro permanece en la BD.
- Si el ID no existe o ya está dado de baja, se informa el error.
- El usuario dado de baja no aparece en ningún listado activo.
- Se confirma mostrando el nombre completo del usuario afectado.
- Sus pedidos permanecen en el sistema sin modificación.

## Guía de implementación sugerida

- Obtener usuario activo para confirmar nombre.
- Usar `usuarioRepo.eliminarLogico(id)`.
- No borrar ni modificar pedidos.
- No permitir seleccionar usuario eliminado para nuevos pedidos.
- No hacer delete físico.

## Validación esperada

- Baja válida confirma nombre completo.
- Usuario no aparece en activos.
- Buscar por mail no lo devuelve.
- Pedidos históricos siguen consultables según reportes.
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
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-14 - Dar de baja un usuario.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero dar de baja un usuario que ya no utiliza el sistema, para ocultarlo sin perder el historial de pedidos.

Criterios de aceptación obligatorios:
1. La baja es lógica: `eliminado = true`, el registro permanece en la BD.
2. Si el ID no existe o ya está dado de baja, se informa el error.
3. El usuario dado de baja no aparece en ningún listado activo.
4. Se confirma mostrando el nombre completo del usuario afectado.
5. Sus pedidos permanecen en el sistema sin modificación.

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
