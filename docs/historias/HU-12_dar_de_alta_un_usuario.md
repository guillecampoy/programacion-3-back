# HU-12 - Dar de alta un usuario

## Resumen

**Épica:** Usuarios  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** registrar un nuevo usuario con sus datos y rol  
**Para** que pueda ser asociado a pedidos en el sistema

## Alcance funcional

- Submenú usuarios opción alta.
- Validación de mail único.
- Selección de rol.
- Persistencia de usuario.

## Criterios de aceptación

- Se solicitan: nombre, apellido, mail, celular, contraseña y rol (`ADMIN` / `USUARIO`).
- Si el mail ya está en uso (`buscarPorMail` retorna un Optional no vacío), se informa el error y no se persiste.
- Al guardar exitosamente se muestra el ID generado.
- El usuario queda con `eliminado = false`.

## Guía de implementación sugerida

- Validar mail no vacío y único entre activos.
- Mostrar opciones de rol.
- No mostrar contraseña luego de cargarla.
- Usar entidad retornada para ID.
- Volver al submenú.

## Validación esperada

- Alta válida muestra ID.
- Mail duplicado activo no guarda.
- Rol inválido se rechaza.
- Listado muestra usuario sin password si aplica.
- Usuario puede seleccionarse para pedido.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-12 - Dar de alta un usuario.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero registrar un nuevo usuario con sus datos y rol, para que pueda ser asociado a pedidos en el sistema.

Criterios de aceptación obligatorios:
1. Se solicitan: nombre, apellido, mail, celular, contraseña y rol (`ADMIN` / `USUARIO`).
2. Si el mail ya está en uso (`buscarPorMail` retorna un Optional no vacío), se informa el error y no se persiste.
3. Al guardar exitosamente se muestra el ID generado.
4. El usuario queda con `eliminado = false`.

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
