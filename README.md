# ReciCUT

Sistema de gestión de reciclaje para el campus del **CUTonalá, Universidad de Guadalajara**,
construido como arquitectura de microservicios.

Proyecto de la materia **Programación de Servicios Web**.

Un miembro de la comunidad universitaria solicita que pasen a recoger material
reciclable, un recolector lo recoge y pesa, y el sistema acumula el impacto
ambiental de todo el campus en reportes agregados.

---

## Arquitectura

```
                        ┌──────────────────┐
                        │  Angular (4200)  │
                        └────────┬─────────┘
                                 │ HTTP + JWT
                        ┌────────▼─────────┐
                        │  api-gateway     │  :8080
                        │  valida el JWT   │
                        │  → X-User-Id     │
                        └────────┬─────────┘
                                 │
      ┌──────────┬──────────┬────┴─────┬──────────┬──────────────┐
      │          │          │          │          │              │
 ┌────▼────┐┌────▼────┐┌────▼─────┐┌───▼─────┐┌───▼────────┐┌────▼─────────┐
 │  auth   ││  user   ││collection││ report  ││recolectores││   centro     │
 │  :8070  ││  :8060  ││  :8050   ││  :8040  ││   :8030    ││ reciclajes   │
 │         ││         ││          ││         ││            ││    :8081     │
 └────┬────┘└────┬────┘└────┬─────┘└───▲─────┘└─────┬──────┘└────┬─────────┘
      │          │          │          │            │            │
   ┌──▼──┐    ┌──▼──┐    ┌──▼──┐    ┌──▼──┐      ┌──▼──┐      ┌──▼──┐
   │MySQL│    │MySQL│    │MySQL│    │MySQL│      │MySQL│      │MySQL│
   └─────┘    └─────┘    └─────┘    └─────┘      └─────┘      └─────┘
      │          ▲          │          ▲
      └──────────┴──────────┴──────────┘
              Kafka  :19092 (interno) / :9092 (host)

        config-server :8888        discovery-server (Eureka) :8761
        reparte la configuración    los servicios se encuentran por nombre
```

**Cada microservicio es dueño absoluto de su base de datos.** Ninguno lee las
tablas de otro: cuando `collection-service` guarda un `collectorId`, guarda un
número, no una llave foránea. Quien traduce ese número a un nombre es el
frontend, llamando a `recolectores`. Así cada equipo puede cambiar su esquema
sin romper a los demás.

### Comunicación entre servicios

| Tipo | Dónde se usa | Para qué |
|---|---|---|
| **Síncrona** (OpenFeign) | `recolectores` → `user-service` | Validar que el usuario exista antes de crear un recolector |
| **Asíncrona** (Kafka) | `auth-service` → `user-service` | Al registrarse un usuario, crearle su perfil |
| **Asíncrona** (Kafka) | `collection-service` → `report-service` | Al completarse una recolección, alimentar los reportes |

La diferencia importa: si `report-service` está caído, las recolecciones **se
siguen completando** — el evento espera en Kafka y se procesa cuando vuelva.
Con una llamada síncrona, la recolección habría fallado.

El consumidor de `report-service` es **idempotente**: la columna `collection_id`
es única, y antes de insertar verifica si ya procesó ese evento. Kafka garantiza
entrega *al menos una vez*, no *exactamente una vez*, así que un evento repetido
es algo que va a pasar, y el servicio lo ignora en lugar de duplicar el reporte.

---

## Microservicios

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `config-server` | 8888 | — | Reparte la configuración de todos los servicios |
| `discovery-server` | 8761 | — | Eureka: registro y descubrimiento |
| `api-gateway` | 8080 | — | Única puerta de entrada; valida el JWT |
| `auth-service` | 8070 | `auth_service` | Registro, login, login con Google, emisión de JWT |
| `user-service` | 8060 | `user_service` | Perfiles de usuario |
| `collection-service` | 8050 | `collection_service` | Solicitudes de recolección y su ciclo de vida |
| `report-service` | 8040 | `report_service` | Reportes agregados e impacto ambiental |
| `recolectores` | 8030 | `recolectores_service` | Catálogo de recolectores y sus zonas |
| `centro-reciclajes` | 8081 | `centros_service` | Catálogo de centros de reciclaje |

Solo el gateway (8080), Eureka (8761), Kafka (9092) y Kafka UI (8085) están
expuestos a la máquina. Los microservicios **no**: se les habla a través del
gateway, como haría un cliente real. Las bases de datos se exponen únicamente
en `127.0.0.1` para poder inspeccionarlas durante el desarrollo.

---

## Cómo levantarlo

### Requisitos

- Docker Desktop
- JDK 17 (solo si vas a compilar fuera de Docker)

### Pasos

```bash
git clone https://github.com/AntonioBernal23/ReciCUT.git
cd ReciCUT
cp .env.example.docker .env
```

Abre `.env` y rellena los campos vacíos: las contraseñas de MySQL, el
`JWT_SECRET` y el `GOOGLE_CLIENT_ID`. El archivo `.env` está en `.gitignore`
y **nunca debe subirse** — contiene credenciales.

Para el `JWT_SECRET` necesitas al menos 32 caracteres:

```bash
openssl rand -base64 48
```

Luego:

```bash
docker compose up -d --build
```

La primera vez tarda varios minutos porque compila los 9 módulos. Los
servicios arrancan en orden: primero `config-server`, luego Eureka, después
las bases de datos y Kafka, y al final los microservicios. El
`docker-compose.yml` usa `depends_on` con `condition: service_healthy`, así
que nadie arranca antes de que su dependencia responda.

### Verificar que funcionó

```bash
docker compose ps
```

Los 17 contenedores deben estar `Up`. Después:

| Qué | Dónde |
|---|---|
| Servicios registrados en Eureka | http://localhost:8761 |
| Tópicos y mensajes de Kafka | http://localhost:8085 |
| API (a través del gateway) | http://localhost:8080 |

En Eureka deben aparecer los 7 servicios de negocio e infraestructura. Si
alguno falta, revisa sus registros:

```bash
docker compose logs -f collection-service
```

### Apagarlo

```bash
docker compose down        # conserva los datos
docker compose down -v     # borra también las bases de datos
```

---

## Compilar sin Docker

```bash
./mvnw -DskipTests compile      # Linux / Mac / Git Bash
.\mvnw.cmd -DskipTests compile  # PowerShell
```

Para correr un servicio suelto contra la infraestructura de Docker, usa
`.env.example.local` como plantilla (apunta a `localhost` en lugar de a los
nombres de contenedor) y el script `run.ps1`.

---

## Endpoints

Todos pasan por el gateway: `http://localhost:8080`.

Las rutas protegidas necesitan la cabecera `Authorization: Bearer <token>`.
El gateway valida el token y reenvía el id del usuario a los microservicios
como `X-User-Id`, así que **ningún servicio tiene que volver a validar el JWT**.

### `auth-service` — autenticación

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/auth/createUser` | Registra un usuario nuevo |
| POST | `/api/auth/login` | Devuelve un JWT a partir de correo y contraseña |
| POST | `/api/auth/google` | Inicia sesión con una cuenta de Google |
| GET | `/api/auth/validate` | Comprueba si un token sigue siendo válido |

### `user-service` — perfiles

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/users/{id}` | Consulta el perfil de un usuario |

### `collection-service` — solicitudes de recolección

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/collections` | Crea una solicitud con sus materiales |
| PUT | `/api/collections/{id}` | Edita una solicitud (solo si está `PENDING`) |
| GET | `/api/collections/{id}` | Consulta una solicitud con todo su detalle |
| GET | `/api/collections/mine` | Lista las solicitudes del usuario autenticado |
| GET | `/api/collections/pending` | Lista las solicitudes sin asignar |
| GET | `/api/collections` | Lista todas, con filtros y paginación |
| GET | `/api/collections/{id}/history` | Bitácora de cambios de estado |
| PATCH | `/api/collections/{id}/assign` | Asigna un recolector |
| PATCH | `/api/collections/{id}/start` | Marca que la recolección empezó |
| PATCH | `/api/collections/{id}/complete` | Captura los pesos reales y cierra |
| PATCH | `/api/collections/{id}/cancel` | Cancela la solicitud |
| PATCH | `/api/collections/{id}/reject` | Rechaza la solicitud |

No hay `DELETE`: una solicitud nunca se borra, se cancela. El historial es
parte del valor del sistema.

Tampoco hay un `PATCH` genérico de estado. Cada transición tiene su propio
endpoint, porque cada una requiere datos distintos (asignar necesita un
recolector; completar necesita los pesos) y reglas distintas.

### `report-service` — reportes

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/reports/summary` | Totales generales del campus |
| GET | `/api/reports/materials` | Desglose por tipo de material |
| GET | `/api/reports/timeline` | Evolución por día |
| GET | `/api/reports/impact` | CO₂ evitado, agua y energía ahorradas |
| GET | `/api/reports/leaderboard` | Usuarios que más han reciclado |
| GET | `/api/reports/users/me` | Reporte del usuario autenticado |
| GET | `/api/reports/users/{userId}` | Reporte de un usuario |
| GET | `/api/reports/centers` | Comparativa entre centros |
| GET | `/api/reports/centers/{centerId}` | Reporte de un centro |

Todos son de lectura. `report-service` **nunca escribe** por HTTP: su única
fuente de datos son los eventos de Kafka. Es la separación entre escritura y
lectura (CQRS): `collection-service` manda en las transacciones,
`report-service` mantiene un modelo optimizado para consultar.

### `recolectores`

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/recolectores` | Registra un recolector |
| GET | `/api/recolectores` | Lista los recolectores |
| DELETE | `/api/recolectores/{id}` | Da de baja un recolector |

### `centro-reciclajes`

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/centros` | Registra un centro de reciclaje |
| GET | `/api/centros` | Lista los centros |
| DELETE | `/api/centros/{id}` | Da de baja un centro |

---

## Ciclo de vida de una recolección

```
         ┌─────────┐
         │ PENDING │  recién creada
         └────┬────┘
              │ assign
         ┌────▼─────┐
         │ ASSIGNED │  ya tiene recolector
         └────┬─────┘
              │ start
       ┌──────▼──────┐
       │ IN_PROGRESS │  el recolector va en camino
       └──────┬──────┘
              │ complete  → publica CollectionCompletedEvent
        ┌─────▼─────┐
        │ COMPLETED │  (final)
        └───────────┘

   En cualquier punto antes de COMPLETED: → CANCELLED
   Desde PENDING, un administrador puede:  → REJECTED
```

Las transiciones válidas no están repartidas en `if`s por el código: viven
dentro del `enum CollectionStatus`, en un solo lugar. Intentar una transición
inválida devuelve **409 Conflict**, nunca un 500.

La respuesta de cada solicitud incluye `nextPossibleStatuses`, calculado por
ese mismo enum. El frontend dibuja los botones a partir de esa lista, así que
la interfaz **no puede** ofrecer una acción que el backend vaya a rechazar.

---

## Estructura de un microservicio

Todos siguen el mismo esqueleto:

```
collection-service/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/com/collection_service/
    │   ├── config/         configuración de Kafka, beans
    │   ├── controller/     endpoints HTTP — no tiene lógica de negocio
    │   ├── dto/            qué entra y qué sale de la API
    │   ├── entity/         las tablas, con JPA
    │   ├── enums/          estados y catálogos cerrados
    │   ├── events/         los mensajes que publica en Kafka
    │   ├── exception/      errores propios + el manejador global
    │   ├── repository/     consultas a la base de datos
    │   └── service/        la lógica y las reglas
    └── resources/
        └── application.yaml
```

El `controller` recibe y responde. El `service` decide. El `repository`
consulta. Un controlador con reglas de negocio adentro es un controlador que
no se puede probar sin levantar un servidor HTTP.

---

## Frontend

Vive en un repositorio aparte: **https://github.com/AntonioBernal23/ReciCUT_Front**

Angular 21 con componentes *standalone*, en modo **zoneless** (sin `zone.js`).
Esto importa al programar: el estado que se muestre en pantalla tiene que ser
un `signal()`, porque sin Zone.js Angular no detecta cambios en campos
normales y la vista se queda congelada aunque los datos ya hayan llegado.

```bash
npm install
npm start          # http://localhost:4200
```

Necesita `src/environments/environment.ts` (está en `.gitignore`):

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080/api',
  googleClientId: '<el client id del proyecto>',
};
```

---

## Equipo

| Integrante | Microservicios |
|---|---|
| Antonio Bernal | `auth-service`, `user-service` |
| Julián Santoyo | `collection-service`, `report-service` |
| Omar | `recolectores`, `centro-reciclajes` |

## Ramas

- `main` — versión estable
- `dev` — integración; de aquí salen y aquí regresan las ramas de trabajo
- una rama por persona o por funcionalidad, que entra a `dev` por *pull request*

## Pendientes conocidos

- Los factores de impacto ambiental de `report-service` tienen valores de
  referencia pero **todavía no citan una fuente verificada**
  (`ImpactFactorSeeder`). Hay que documentar de dónde sale cada número.
- `user-service` solo expone la consulta por id; le falta el resto del CRUD
  de perfiles.
- Las pruebas automatizadas son únicamente las que genera Spring
  (`contextLoads`). Faltan pruebas de la máquina de estados y de la
  idempotencia del consumidor de Kafka.
