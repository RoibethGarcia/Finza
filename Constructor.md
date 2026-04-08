Actua como un desarrollador senior especializado en Java y Spring Boot.

Necesito que implementes lo siguiente:

## Funcionalidad
Implementa el primer corte funcional del backend para una aplicacion de gestion de gastos e ingresos personales sobre el scaffold actual del repositorio.

El alcance de esta primera entrega debe incluir:
- registro y login de usuario con email y password;
- endpoint del usuario autenticado con mensaje de bienvenida;
- CRUD de cuentas del usuario;
- CRUD de categorias separadas por tipo (`INCOME` / `EXPENSE`);
- alta, edicion, baja y listado de movimientos financieros;
- resumen de balance por periodo con ingresos, gastos y saldo neto;
- estructura preparada para evolucionar despues a prestamos, tarjetas de credito, deudas con terceros, dinero a cobrar y ahorros, SIN intentar resolver todo eso en este primer corte.

NO asumas frontend existente. Expon una API REST limpia y consistente, lista para ser consumida por una React SPA mas adelante.

## Contexto tecnico
- Repositorio actual: `GestorGastos/demo`
- Stack verificado: Java 21, Maven Wrapper, Spring Boot 4.0.5, Spring Web MVC, Spring Security, Spring Data JPA, Bean Validation, Mail, Lombok y PostgreSQL.
- Estado actual verificado del backend:
  - existe solo un scaffold minimo con `DemoApplication`;
  - `application.properties` solo contiene `spring.application.name=demo`;
  - no existen controladores funcionales, servicios de negocio, entidades del dominio financiero ni seguridad terminada;
  - hay una prueba minima de carga de contexto.
- Base de datos: PostgreSQL. Hoy NO hay schema funcional del dominio ni migraciones del negocio implementadas.
- Convenciones del proyecto:
  - mantener una arquitectura de monolito modular;
  - separar claramente `api` / `application` / `domain` / `infrastructure`;
  - NO exponer entidades JPA directamente en la API; usar DTOs de request/response;
  - modelar bien el dominio: una tarjeta no es una categoria, un prestamo no es un gasto y una deuda no es lo mismo que un dinero a cobrar;
  - priorizar simplicidad inicial, mantenibilidad y crecimiento posterior sin sobreingenieria;
  - incluir tests en la logica critica siguiendo TDD estricto;
  - NO ejecutar build ni levantar la aplicacion como parte de la entrega;
  - si no vas a hacer un refactor completo de naming, manten temporalmente el package base actual de forma consistente.

## Formato de entrega
Entrega el codigo en bloques separados por archivo. Indica la ruta de cada archivo como encabezado.

Al final, incluye una seccion breve `Como probarlo` con:
- pasos exactos;
- endpoints a invocar;
- payloads de ejemplo;
- datos minimos necesarios en PostgreSQL;
- y cualquier variable de entorno requerida.

## Requisitos del codigo
1. Codigo listo para produccion, no ejemplos simplificados.
2. Incluir validacion completa de inputs y manejo de errores consistente.
3. Aplicar responsabilidad unica y separacion clara de capas.
4. Incluir DTOs, enums y tipos del dominio donde aporten claridad.
5. Asegurar aislamiento de datos por usuario autenticado.
6. Incluir tests unitarios y/o de integracion para autenticacion, validaciones, reglas de balance y control de acceso a recursos del usuario.
7. Si necesitas agregar dependencias, indicalo al inicio explicando por que y mostrando el cambio necesario en `pom.xml`.
8. Incluir comentarios SOLO donde la logica no sea obvia.
