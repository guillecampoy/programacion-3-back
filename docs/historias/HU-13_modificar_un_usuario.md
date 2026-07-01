# HU-13 - Modificar un usuario

## Resumen

**Épica:** Usuarios  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** editar los datos de un usuario existente  
**Para** mantener la información actualizada

## Alcance funcional

- Submenú usuarios opción modificar.
- Listado de usuarios activos.
- Campos en blanco conservan valor.
- Validación de mail único si cambia.

## Criterios de aceptación

- El sistema lista los usuarios activos antes de pedir el ID.
- Si el ID no corresponde a un usuario activo, se muestra error.
- Se muestran los valores actuales antes de pedir los nuevos.
- Dejar un campo en blanco conserva el valor anterior.
- Si se modifica el mail, verificar que no esté en uso por otro usuario activo.
- El cambio se persiste correctamente.

## Guía de implementación sugerida

- Validar id contra usuarios activos.
- No mostrar contraseña actual en claro; permitir cambiarla si se ingresa una nueva.
- Comparar mail nuevo contra otro usuario activo.
- No pisar con blanco.
- Persistir con `usuarioRepo.guardar(usuario)`.

## Validación esperada

- Campo vacío conserva valor.
- Mail duplicado de otro usuario falla.
- ID inexistente falla.
- Usuario eliminado no se modifica.
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
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-13 - Modificar un usuario.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero editar los datos de un usuario existente, para mantener la información actualizada.

Criterios de aceptación obligatorios:
1. El sistema lista los usuarios activos antes de pedir el ID.
2. Si el ID no corresponde a un usuario activo, se muestra error.
3. Se muestran los valores actuales antes de pedir los nuevos.
4. Dejar un campo en blanco conserva el valor anterior.
5. Si se modifica el mail, verificar que no esté en uso por otro usuario activo.
6. El cambio se persiste correctamente.

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
