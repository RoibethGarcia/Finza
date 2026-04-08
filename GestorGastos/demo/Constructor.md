Eres un arquitecto de software senior con criterio práctico.

Tu tarea es diseñar la arquitectura técnica inicial de una aplicación para gestionar gastos e ingresos personales.

NO quiero una respuesta genérica. Quiero una propuesta realista, aterrizada y alineada al estado ACTUAL del repositorio.

---

# 1) Contexto real del proyecto

## Estado actual verificado del repositorio
- Existe un proyecto backend base en **Java 21 + Spring Boot**.
- El repositorio hoy tiene un **scaffold técnico inicial**, no una arquitectura de negocio ya implementada.
- Están presentes dependencias base para:
  - Spring Web MVC
  - Spring Security
  - Spring Data JPA
  - Validation
  - Mail
  - PostgreSQL
- Actualmente NO debe asumirse que ya existen:
  - módulos de dominio,
  - controladores funcionales,
  - servicios de negocio,
  - entidades del modelo financiero,
  - autenticación terminada,
  - frontend implementado.

## Restricción principal
- La propuesta **debe partir de la base actual y extenderla**.
- **NO** debe proponer reemplazar el backend actual por otro stack.

## Stack objetivo
- Backend: **Java 21 + Spring Boot**
- Base de datos: **PostgreSQL**
- Frontend web: **React SPA**
- Mobile: debes indicar si conviene:
  - iniciar en esta fase, o
  - postergarlo a una segunda fase

---

# 2) Objetivo de negocio

La aplicación debe permitir a un usuario:
- registrar ingresos,
- registrar gastos,
- categorizar movimientos,
- ver balances,
- consultar reportes por período,
- manejar múltiples cuentas,
- visualizar compromisos financieros,
- conocer cuánto dinero le queda disponible luego de cubrir sus obligaciones,
- preparar el producto para evolucionar en el futuro sin sobreingeniería inicial.

---

# 3) Principios de diseño que DEBES respetar

- Priorizar **simplicidad inicial**.
- Evitar **sobreingeniería**.
- Diseñar con **alta mantenibilidad**.
- Permitir crecimiento posterior sin rehacer todo.
- Mantener una arquitectura coherente con una app pequeña que inicia con pocos usuarios.
- Proteger información financiera y datos sensibles.
- Favorecer separación clara entre:
  - dominio,
  - aplicación,
  - infraestructura,
  - presentación.

Si propones algo complejo, debes justificar claramente por qué vale la pena.

---

# 4) Alcance funcional esperado

## Funcionalidades núcleo
- Registro de ingresos
- Registro de gastos
- Gestión de categorías
- Gestión de múltiples cuentas
- Balance general
- Reportes por período
- Cálculo de dinero disponible luego de gastos/obligaciones
- Dashboard con métricas y gráficas

## Funcionalidades financieras específicas
- Gestión de préstamos bancarios
- Gestión de préstamos vehiculares
- Gestión de tarjetas de crédito
- Gestión de deudas con terceros
- Gestión de dinero que otras personas me deben
- Sección de ahorros / inversiones

## Reglas funcionales deseadas
- Los préstamos deben mostrar:
  - institución,
  - fecha,
  - cantidad total de cuotas,
  - cuota actual,
  - cuotas pagadas,
  - cuotas restantes,
  - total de deuda,
  - monto mensual estimado
- Las tarjetas de crédito deben mostrar:
  - institución,
  - fecha,
  - cantidad total de cuotas,
  - cuota actual,
  - pagos/montos vinculados
- El sistema debe permitir distinguir visualmente productos financieros diferentes.
- Debe existir login.
- Debe existir mensaje de bienvenida luego del acceso.
- El frontend debe ser SPA.
- La información debe persistir y ser consistente entre futuras interfaces web y mobile.

## Funcionalidades opcionales / fase posterior
- División de gastos con terceros, estilo Splitwise
- Funcionalidad móvil dedicada

---

# 5) Aclaración de dominio IMPORTANTÍSIMA

NO mezcles conceptos que pertenecen a niveles distintos del dominio.

La propuesta debe separar claramente:

## Conceptos de dominio esperados
- **Usuario**: propietario de la información
- **Cuenta**: lugar donde el usuario administra saldo o movimientos
- **Movimiento / Transacción**: ingreso o gasto individual
- **Categoría**: clasificación del movimiento (alimentos, ocio, servicios, etc.)
- **Medio o instrumento financiero**: por ejemplo tarjeta de crédito
- **Pasivo / deuda**: por ejemplo préstamo bancario, préstamo vehicular o deuda con terceros
- **Activo a cobrar**: dinero que terceros deben al usuario
- **Ahorro / inversión**: reserva o instrumento separado de gasto corriente

## Reglas de modelado
- “Gasto” NO debe convertirse en una bolsa que contenga tarjetas, préstamos y terceros como si fueran lo mismo.
- “Tarjeta de crédito” NO es una categoría de gasto; es un instrumento o fuente de financiación.
- “Préstamo” NO es una categoría de gasto; es un pasivo con reglas propias.
- “Entidad bancaria” NO debe almacenar totales como verdad principal si esos valores pueden derivarse de movimientos o saldos calculados.
- Los totales agregados deben definirse explícitamente como:
  - calculados en tiempo real,
  - materializados,
  - o persistidos por necesidad justificada.

Quiero que la propuesta explique estas separaciones para evitar errores de modelado desde el inicio.

---

# 6) Datos del dominio sugeridos por negocio

## Usuario
- nombre completo
- email
- fecha de nacimiento
- contraseña hasheada

## Préstamos
- institución / entidad
- fecha
- cantidad de cuotas
- cuota actual
- tipo:
  - bancario
  - vehicular
- monto total
- monto mensual

## Tarjetas de crédito
- entidad bancaria
- fecha
- cantidad total de cuotas
- cuota actual

## Terceros
- deudores
- acreedores

## Entidad bancaria
- nombre
- operaciones en pesos
- operaciones en dólares

## Gastos / categorías sugeridas
- alimentos
- servicios
- ocio
- alquiler
- otras categorías que propongas

IMPORTANTE:
- Puedes mejorar, normalizar o replantear este modelo si detectas errores conceptuales.
- Si cambias algo, debes explicar por qué.

---

# 7) Requisitos no funcionales

- Usuarios iniciales: ~20
- Debe poder crecer posteriormente
- Seguridad adecuada para datos financieros
- Alta mantenibilidad
- Arquitectura simple al inicio
- Escalabilidad razonable
- Persistencia confiable
- Despliegue desacoplado por contenedores

## Requisitos técnicos explícitos
- Usar **Docker** para separar:
  - frontend web,
  - backend/API,
  - base de datos,
  - y servicios auxiliares si fueran necesarios

---

# 8) Decisiones esperadas de arquitectura

Quiero que tomes postura y no respondas con “depende” sin justificar.

Debes decidir y justificar:
- Monolito modular vs microservicios
- Arquitectura por capas vs hexagonal/modular
- JWT vs sesión basada en cookies
- Estrategia de autorización
- React puro vs stack complementario mínimo
- Si mobile va ahora o en segunda fase
- Cómo manejar múltiples monedas
- Cómo modelar balances y totales derivados
- Cómo organizar reportes y métricas sin sobrediseñar

Si propones alternativas, explica tradeoffs reales.

---

# 9) Qué NO quiero

- NO quiero microservicios si no están fuertemente justificados.
- NO quiero Kafka, event sourcing, CQRS o arquitectura distribuida sin una razón clara.
- NO quiero una solución enterprise sobredimensionada para 20 usuarios iniciales.
- NO quiero una respuesta centrada solo en frameworks.
- NO quiero una respuesta que ignore el modelado de dominio.
- NO quiero asumir que el backend ya está implementado cuando hoy solo existe el scaffold.

---

# 10) Entregables obligatorios

Quiero que tu respuesta incluya exactamente estas secciones:

## 1. Resumen ejecutivo
- propuesta recomendada en pocas líneas

## 2. Arquitectura recomendada alineada al repositorio actual
- describiendo cómo evolucionar desde el scaffold existente

## 3. Justificación del stack y tradeoffs
- backend
- frontend
- base de datos
- autenticación
- contenedores
- mobile

## 4. Módulos principales del backend
- módulos de dominio
- aplicación
- infraestructura
- API

## 5. Módulos principales del frontend
- estructura por features
- vistas principales
- manejo de estado
- capa de API

## 6. Modelo de dominio y datos inicial
- entidades principales
- relaciones
- reglas de negocio clave
- aclaración de qué se calcula y qué se persiste

## 7. Flujos principales del usuario
- registro/login
- alta de ingreso
- alta de gasto
- alta de préstamo
- visualización de balances
- reportes

## 8. Estrategia de autenticación y autorización
- almacenamiento de credenciales
- hash de contraseña
- sesiones/tokens
- permisos y aislamiento de datos por usuario

## 9. Estrategia de testing
- unit tests
- integration tests
- tests de repositorio
- tests de API
- tests frontend
- qué probar primero en MVP

## 10. Estrategia de despliegue
- docker compose local
- ambientes sugeridos
- variables de entorno
- logging y observabilidad mínima

## 11. Riesgos técnicos y mitigaciones

## 12. Roadmap por fases
- MVP
- v2
- escalado

## 13. Estructura de carpetas sugerida
- backend
- frontend

## 14. Diagrama
- incluir un diagrama Mermaid simple de alto nivel

---

# 11) Criterios de calidad de la respuesta

Tu respuesta debe ser:
- concreta,
- técnica,
- accionable,
- coherente con el tamaño real del proyecto,
- y con foco en fundamentos, no humo.

Debes explicar:
- por qué una decisión es buena,
- qué problema resuelve,
- qué costo introduce,
- y qué se deja para después.

---

# 12) Recomendación base esperada

Si no encuentras una razón poderosa para complicarlo, parte de esta línea:
- **backend monolítico modular**
- **API REST**
- **PostgreSQL**
- **React SPA**
- **Docker Compose**
- **mobile en segunda fase**

Pero NO copies esto automáticamente: valídalo y justifícalo.

---

# 13) Inspiraciones del producto

El producto puede tomar inspiración conceptual de:
- Wallet / Money Manager
- Tricount

Pero la propuesta debe aterrizarse al caso de uso concreto de finanzas personales con:
- cuentas,
- tarjetas,
- préstamos,
- terceros,
- métricas,
- y evolución futura controlada.
