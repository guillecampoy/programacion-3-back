# HU-06 - Modificar una categoría existente

## Resumen

**Épica:** Categorías  
**Prioridad:** Alta  
**Story Points:** 10  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** editar el nombre o la descripción de una categoría ya creada  
**Para** corregir errores sin tener que borrar y recrear el registro

## Alcance funcional

- Submenú categorías opción modificar.
- Listado previo de activas.
- Campos en blanco conservan valor.
- Persistencia de cambios.

## Criterios de aceptación

- El sistema lista las categorías activas antes de pedir el ID.
- Si el ID no corresponde a ninguna categoría activa, se muestra un mensaje de error.
- Se muestran los valores actuales antes de pedir los nuevos.
- Dejar un campo en blanco mantiene el valor anterior.
- El cambio se persiste correctamente en la base de datos.

## Guía de implementación sugerida

- Usar `listarActivos()` para seleccionar.
- Validar que el id elegido pertenezca a esa lista activa.
- No permitir nombre final vacío.
- Llamar `categoriaRepo.guardar(categoria)` para actualizar.
- Mostrar confirmación.

## Validación esperada

- Modificar nombre cambia listado.
- Campo vacío conserva valor.
- ID inexistente muestra error.
- Categoría eliminada no puede modificarse.
- Reiniciar app conserva cambio.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-06 - Modificar una categoría existente.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero editar el nombre o la descripción de una categoría ya creada, para corregir errores sin tener que borrar y recrear el registro.

Criterios de aceptación obligatorios:
1. El sistema lista las categorías activas antes de pedir el ID.
2. Si el ID no corresponde a ninguna categoría activa, se muestra un mensaje de error.
3. Se muestran los valores actuales antes de pedir los nuevos.
4. Dejar un campo en blanco mantiene el valor anterior.
5. El cambio se persiste correctamente en la base de datos.

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
