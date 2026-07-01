# HU-15 - Buscar usuario por mail

## Resumen

**Épica:** Usuarios  
**Prioridad:** Alta  
**Story Points:** 6  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** buscar un usuario ingresando su dirección de mail  
**Para** consultar rápidamente sus datos sin recorrer el listado

## Alcance funcional

- Submenú usuarios o reportes.
- `usuarioRepo.buscarPorMail`.
- Salida sin contraseña.

## Criterios de aceptación

- Se solicita el mail por consola.
- Se llama a `usuarioRepo.buscarPorMail(mail)`.
- Si el Optional no está vacío, se muestran todos los datos del usuario sin mostrar la contraseña.
- Si el Optional está vacío, se informa que no existe usuario activo con ese mail.

## Guía de implementación sugerida

- Normalizar input con trim.
- No listar la contraseña.
- No buscar usuarios eliminados.
- No duplicar JPQL fuera del repo.
- Mensaje claro para no encontrado.

## Validación esperada

- Mail existente muestra datos.
- Mail inexistente muestra mensaje.
- Usuario eliminado no aparece.
- No se imprime contraseña.
- No falla por mayúsculas/espacios si se decide normalizar.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-15 - Buscar usuario por mail.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero buscar un usuario ingresando su dirección de mail, para consultar rápidamente sus datos sin recorrer el listado.

Criterios de aceptación obligatorios:
1. Se solicita el mail por consola.
2. Se llama a `usuarioRepo.buscarPorMail(mail)`.
3. Si el Optional no está vacío, se muestran todos los datos del usuario sin mostrar la contraseña.
4. Si el Optional está vacío, se informa que no existe usuario activo con ese mail.

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
