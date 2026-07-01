# HU-10 - Dar de baja un producto

## Resumen

**Épica:** Productos  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** dar de baja un producto que ya no está disponible  
**Para** retirarlo del catálogo activo sin eliminar su historial

## Alcance funcional

- Submenú productos opción baja.
- Baja lógica.
- Confirmación con nombre.

## Criterios de aceptación

- La baja es lógica: `eliminado = true`, el registro permanece en la BD.
- Si el ID no existe o ya está dado de baja, se informa el error.
- El producto dado de baja no aparece en el listado de productos activos.
- Se muestra confirmación con el nombre del producto afectado.

## Guía de implementación sugerida

- Obtener producto activo antes de baja para confirmar nombre.
- Usar `productoRepo.eliminarLogico(id)`.
- No tocar detalles de pedidos históricos.
- No restaurar ni modificar stock.
- No hacer delete físico.

## Validación esperada

- Baja válida confirma nombre.
- Producto no aparece en activos.
- Buscar por categoría no lo devuelve.
- Segundo intento informa error.
- Pedidos históricos conservan detalle.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-10 - Dar de baja un producto.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero dar de baja un producto que ya no está disponible, para retirarlo del catálogo activo sin eliminar su historial.

Criterios de aceptación obligatorios:
1. La baja es lógica: `eliminado = true`, el registro permanece en la BD.
2. Si el ID no existe o ya está dado de baja, se informa el error.
3. El producto dado de baja no aparece en el listado de productos activos.
4. Se muestra confirmación con el nombre del producto afectado.

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
