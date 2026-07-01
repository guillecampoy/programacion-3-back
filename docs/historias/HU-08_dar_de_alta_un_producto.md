# HU-08 - Dar de alta un producto

## Resumen

**Épica:** Productos  
**Prioridad:** Alta  
**Story Points:** 12  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** operador del sistema  
**Quiero** registrar un nuevo producto asociándolo a una categoría existente  
**Para** incorporar artículos al catálogo con toda su información básica

## Alcance funcional

- Submenú productos opción alta.
- Selección de categoría activa.
- Validación de precio/stock.
- Persistencia de producto.

## Criterios de aceptación

- El sistema lista las categorías activas para que el operador seleccione una.
- Si no hay categorías activas, se informa y se cancela la operación.
- Se solicitan: nombre obligatorio, descripción, precio mayor a 0, stock mayor o igual a 0, imagen y disponible.
- Si precio o stock tienen valores inválidos, se informa el error y no se persiste.
- Al guardar se muestra el ID generado y la categoría asignada.

## Guía de implementación sugerida

- Listar categorías antes de pedir datos de producto.
- Validar parseo numérico.
- Setear `eliminado=false` y `disponible` según input.
- Asociar objeto `Categoria` activo al producto.
- Mostrar ID desde entidad retornada por `guardar()`.

## Validación esperada

- Sin categorías activas cancela.
- Precio <=0 falla.
- Stock <0 falla.
- Producto válido aparece en listado.
- Buscar productos por categoría lo incluye.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-08 - Dar de alta un producto.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como operador del sistema, quiero registrar un nuevo producto asociándolo a una categoría existente, para incorporar artículos al catálogo con toda su información básica.

Criterios de aceptación obligatorios:
1. El sistema lista las categorías activas para que el operador seleccione una.
2. Si no hay categorías activas, se informa y se cancela la operación.
3. Se solicitan: nombre obligatorio, descripción, precio mayor a 0, stock mayor o igual a 0, imagen y disponible.
4. Si precio o stock tienen valores inválidos, se informa el error y no se persiste.
5. Al guardar se muestra el ID generado y la categoría asignada.

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
