# HU-05 - Dar de alta una categoría

## Resumen

**Épica:** Categorías  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** crear una nueva categoría ingresando nombre y descripción  
**Para** organizar los productos del catálogo en grupos temáticos

## Alcance funcional

- Submenú categorías opción alta.
- Validación de nombre.
- Persistencia con `categoriaRepo.guardar()`.

## Criterios de aceptación

- El sistema solicita nombre y descripción por consola.
- Si el nombre está vacío, el sistema informa el error y no persiste.
- Al guardar exitosamente, se muestra el ID generado por la base de datos.
- La categoría queda con `eliminado = false`.

## Guía de implementación sugerida

- Leer nombre con trim.
- Descripción puede ser opcional.
- Usar entidad retornada por `guardar()` para mostrar ID.
- No crear categorías eliminadas por defecto.
- Volver al submenú luego de operar.

## Validación esperada

- Alta válida muestra ID.
- Nombre vacío no guarda.
- Listado activo muestra nueva categoría.
- BD conserva registro tras reinicio.
- No hay excepción ante descripción vacía.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-05 - Dar de alta una categoría.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero crear una nueva categoría ingresando nombre y descripción, para organizar los productos del catálogo en grupos temáticos.

Criterios de aceptación obligatorios:
1. El sistema solicita nombre y descripción por consola.
2. Si el nombre está vacío, el sistema informa el error y no persiste.
3. Al guardar exitosamente, se muestra el ID generado por la base de datos.
4. La categoría queda con `eliminado = false`.

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
