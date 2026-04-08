Eres un arquitecto de software senior con criterio practico.

Tu tarea es diseñar la arquitectura tecnica completa POR FASES de una aplicacion para gestionar gastos e ingresos personales.

NO quiero una respuesta generica. Quiero una propuesta realista, aterrizada y alineada al estado ACTUAL del repositorio.

---

# 1) Contexto real del proyecto

## Estado actual verificado del repositorio
- Existe un proyecto backend base en `GestorGastos/demo`.
- El repositorio actual contiene un scaffold tecnico minimo sobre:
  - Java 21
  - Maven Wrapper
  - Spring Boot 4.0.5
  - Spring Web MVC
  - Spring Security
  - Spring Data JPA
  - Bean Validation
  - Spring Mail
  - PostgreSQL driver
  - Lombok
- Actualmente SI debe asumirse que:
  - solo existe `DemoApplication` como punto de arranque;
  - `application.properties` solo tiene `spring.application.name=demo`;
  - hay una prueba minima de contexto;
  - NO existen todavia modulos funcionales de negocio;
  - NO existe frontend implementado;
  - NO existe autenticacion terminada;
  - NO existe modelo financiero persistido.

## Restriccion principal
- La propuesta DEBE partir del backend actual y extenderlo.
- NO debe proponer reemplazar Spring Boot por otro backend.
- NO debe asumir funcionalidades que hoy no existen en el codigo.

## Stack objetivo del producto
- Backend: Java 21 + Spring Boot
- Base de datos: PostgreSQL
- Frontend web: React SPA
- Infraestructura local: Docker Compose
- Mobile: debes decidir si entra ahora o en una segunda fase, y justificarlo.

---

# 2) Objetivo de negocio

La aplicacion debe permitir a un usuario:
- registrar ingresos;
- registrar gastos;
- categorizar movimientos;
- manejar multiples cuentas;
- ver balances;
- consultar reportes por periodo;
- visualizar compromisos financieros;
- saber cuanto dinero le queda disponible luego de cubrir obligaciones;
- evolucionar mas adelante a un producto mas completo sin rehacer la base.

---

# 3) Principios de diseño que DEBES respetar

- Priorizar simplicidad inicial.
- Evitar sobreingenieria.
- Mantener alta mantenibilidad.
- Diseñar para crecer sin reescribir la base.
- Separar con claridad dominio, aplicacion, infraestructura y presentacion.
- Proteger datos financieros y sensibles.
- Diseñar para un producto pequeño al inicio (~20 usuarios), pero con capacidad de crecer razonablemente.
- Tomar decisiones EXPLICITAS; no responder "depende" sin cerrar postura.

Si propones complejidad, debes justificar:
- que problema real resuelve;
- por que vale su costo hoy;
- y que riesgo evita.

---

# 4) Alcance funcional del producto

## Funcionalidades nucleo
- Registro y login de usuario
- Bienvenida al acceder
- Gestion de cuentas
- Registro de ingresos
- Registro de gastos
- Gestion de categorias
- Balance general
- Reportes por periodo
- Dashboard con metricas
- Calculo de dinero disponible luego de obligaciones

## Funcionalidades financieras especificas
- Prestamos bancarios
- Prestamos vehiculares
- Tarjetas de credito
- Deudas con terceros
- Dinero que otras personas deben al usuario
- Seccion de ahorros / inversiones

## Funcionalidades opcionales / futuras
- Division de gastos con terceros estilo Splitwise
- Aplicacion mobile dedicada
- Importacion de movimientos desde archivos o integraciones bancarias

---

# 5) Aclaracion de dominio IMPORTANTISIMA

NO mezcles conceptos de dominio distintos.

La propuesta debe separar claramente:
- Usuario: propietario de la informacion
- Cuenta: donde se administran saldos y movimientos
- Movimiento / Transaccion: ingreso o gasto individual
- Categoria: clasificacion de un movimiento
- Instrumento financiero: por ejemplo tarjeta de credito
- Pasivo / deuda: por ejemplo prestamo bancario, prestamo vehicular o deuda con terceros
- Activo a cobrar: dinero que terceros deben al usuario
- Ahorro / inversion: reserva separada del gasto corriente

## Reglas de modelado que debes respetar
- "Gasto" NO debe ser una bolsa que contenga tarjetas, prestamos y terceros como si fueran lo mismo.
- "Tarjeta de credito" NO es una categoria de gasto.
- "Prestamo" NO es una categoria de gasto.
- "Entidad bancaria" NO debe guardar totales derivados como verdad principal si esos totales pueden calcularse.
- Debes aclarar explicitamente que datos se persisten y cuales se calculan.

Quiero que expliques estas separaciones para prevenir errores conceptuales desde el inicio.

---

# 6) Datos sugeridos por negocio

## Usuario
- nombre completo
- email
- fecha de nacimiento
- password hasheada

## Prestamos
- institucion / entidad
- fecha
- cantidad de cuotas
- cuota actual
- cuotas pagadas
- cuotas restantes
- tipo: bancario o vehicular
- monto total
- monto mensual estimado

## Tarjetas de credito
- entidad bancaria
- fecha
- cantidad total de cuotas
- cuota actual
- pagos / montos vinculados

## Terceros
- acreedores
- deudores

## Entidades bancarias
- nombre
- operaciones en pesos
- operaciones en dolares

## Categorias sugeridas
- alimentos
- servicios
- ocio
- alquiler
- transporte
- salud
- educacion
- ahorro
- otras que consideres adecuadas

IMPORTANTE:
- Puedes corregir, normalizar o replantear este modelo si detectas errores conceptuales.
- Si cambias algo, debes explicar POR QUE.

---

# 7) Requisitos no funcionales

- Usuarios iniciales: ~20
- Persistencia confiable
- Seguridad adecuada para datos financieros
- Mantenibilidad alta
- Arquitectura simple al inicio
- Escalabilidad razonable
- Despliegue desacoplado por contenedores

## Requisitos tecnicos explicitos
- Usar Docker para separar:
  - frontend web
  - backend/API
  - base de datos
  - y servicios auxiliares SOLO si realmente hacen falta

---

# 8) Decisiones de arquitectura que DEBES tomar y justificar

Debes tomar postura sobre:
- Monolito modular vs microservicios
- Arquitectura por capas vs modular/hexagonal pragmatico
- JWT vs sesion basada en cookies
- Estrategia de autorizacion
- React puro vs stack complementario minimo
- Mobile ahora o segunda fase
- Manejo de multiples monedas
- Modelado de balances y totales derivados
- Estrategia de reportes y metricas sin sobrediseñar
- Estrategia de migraciones y evolucion del schema

Si propones alternativas, explica tradeoffs reales.

---

# 9) Que NO quiero

- NO quiero microservicios sin una justificacion fuerte.
- NO quiero Kafka, CQRS, event sourcing ni arquitectura distribuida sin necesidad real.
- NO quiero una solucion enterprise sobredimensionada para 20 usuarios.
- NO quiero una respuesta centrada solo en frameworks.
- NO quiero ignorar el modelado de dominio.
- NO quiero asumir que el backend ya esta implementado cuando hoy es solo scaffold.
- NO quiero fases inventadas sin criterio tecnico y de negocio.

---

# 10) Entregables obligatorios

Quiero que tu respuesta incluya EXACTAMENTE estas secciones:

## 1. Resumen ejecutivo
- propuesta recomendada en pocas lineas

## 2. Arquitectura recomendada alineada al repositorio actual
- explicando como evolucionar desde el scaffold existente

## 3. Justificacion del stack y tradeoffs
- backend
- frontend
- base de datos
- autenticacion
- contenedores
- mobile

## 4. Modulos principales del backend
- dominio
- aplicacion
- infraestructura
- API
- limites modulares recomendados

## 5. Modulos principales del frontend
- estructura por features
- vistas principales
- manejo de estado
- capa de API
- decision de librerias minimas recomendadas

## 6. Modelo de dominio y datos inicial
- entidades principales
- relaciones
- reglas de negocio clave
- que se persiste y que se calcula
- como preparar el modelo para crecer sin contaminar el nucleo

## 7. Flujos principales del usuario
- registro/login
- alta de ingreso
- alta de gasto
- alta de prestamo
- visualizacion de balances
- reportes

## 8. Estrategia de autenticacion y autorizacion
- almacenamiento de credenciales
- hash de passwords
- sesiones/tokens
- permisos
- aislamiento de datos por usuario

## 9. Estrategia de testing
- unit tests
- integration tests
- tests de repositorio
- tests de API
- tests frontend
- que probar primero en MVP

## 10. Estrategia de despliegue
- docker compose local
- ambientes sugeridos
- variables de entorno
- logging
- observabilidad minima

## 11. Riesgos tecnicos y mitigaciones

## 12. Roadmap por fases
- Fase 0: fundaciones tecnicas
- Fase 1: MVP financiero base
- Fase 2: compromisos financieros
- Fase 3: analitica y experiencia
- Fase 4: escalado controlado

En CADA fase debes indicar:
- objetivo;
- funcionalidades incluidas;
- modulos afectados;
- decisiones tecnicas relevantes;
- que se posterga;
- riesgos;
- criterio de salida de la fase.

## 13. Estructura de carpetas sugerida
- backend
- frontend

## 14. Diagrama
- incluir un diagrama Mermaid simple de alto nivel

## 15. Orden recomendado de implementacion
- lista secuencial de que construir primero, segundo, tercero, etc.

---

# 11) Criterios de calidad de la respuesta

Tu respuesta debe ser:
- concreta;
- tecnica;
- accionable;
- coherente con el tamaño real del proyecto;
- centrada en fundamentos;
- y util para tomar decisiones de implementacion, no solo para discutir ideas.

Debes explicar para cada decision importante:
- por que es buena;
- que problema resuelve;
- que costo introduce;
- y que se deja para despues.

---

# 12) Linea base esperada

Si no encuentras una razon poderosa para complicarlo, valida criticamente esta base:
- backend monolitico modular
- API REST
- PostgreSQL
- React SPA
- Docker Compose
- mobile en segunda fase

NO copies esto automaticamente. Validalo contra el contexto real del repositorio y justificalo.

---

# 13) Inspiraciones del producto

Puedes tomar inspiracion conceptual de:
- Wallet / Money Manager
- Tricount

Pero tu propuesta debe aterrizarse al caso concreto de finanzas personales con:
- cuentas
- tarjetas
- prestamos
- terceros
- metricas
- balances
- evolucion futura controlada

---

# 14) Nivel de profundidad esperado

Quiero una respuesta de arquitecto que piense en capas, dominio, evolucion, tradeoffs y orden de implementacion.

NO quiero humo.
Quiero una propuesta que sirva para convertir esta base tecnica actual en un producto serio por etapas.
