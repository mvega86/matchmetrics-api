# CLAUDE.md — MatchMetrics Backend

Guía obligatoria para cualquier agente de IA que trabaje sobre este módulo.
Complementa `AGENTS.md` (raíz del workspace) — leer ambos antes de cualquier cambio.

---

## 1. Stack

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 17 | Lenguaje |
| Spring Boot | 3.x | Framework |
| Spring Security | 6.x | Auth, CORS, filtros |
| JPA / Hibernate | 6.x | ORM |
| PostgreSQL | 15+ | Base de datos |
| Flyway | — | Migraciones de BD |
| Lombok | — | `@RequiredArgsConstructor`, `@Data`, etc. |
| Maven | Wrapper `mvnw` | Build |

**Despliegue:** Render.com — instancia única con PostgreSQL gestionado.

---

## 2. Estructura de paquetes

```
com.matchmetrics/
├── controller/            ← @RestController — solo recibe/devuelve DTOs
│   └── auth/              ← AuthController (login, registro, refresh, me)
├── service/               ← interfaces de servicio
│   ├── implementation/    ← lógica de negocio real
│   ├── stats/             ← cálculo de estadísticas (Sistema A + Sistema B)
│   ├── invitation/        ← invitaciones de usuario
│   └── notification/      ← email (Resend) + SMS (Twilio, desactivado)
├── persistence/
│   ├── entity/            ← entidades JPA — una por tabla
│   ├── repository/        ← Spring Data JPA repositories
│   └── audit/             ← AuditModel — extender siempre
├── domain/
│   └── enums/             ← SportType, MatchStatus, UserRole, etc.
├── mapper/
│   └── dto/               ← DTOs + mappers manuales (no MapStruct)
├── security/              ← JwtService, JwtFilter, RateLimitInterceptor, ApiRateLimiter
├── config/                ← WebConfig (CORS + interceptores), SecurityConfig
├── exception/             ← GlobalExceptionHandler, excepciones propias
└── utils/                 ← helpers menores
```

---

## 3. Flujo de datos obligatorio

```
HTTP Request
    ↓
JwtFilter (extrae usuario de cookie access_token)
    ↓
@RestController  →  recibe RequestBody como DTO
    ↓
@Service (interface)  →  lógica de negocio
    ↓
@Repository (Spring Data)  →  consultas JPA
    ↓
Entidad JPA
    ↓
Mapper  →  DTO de respuesta
    ↓
ApiResponse<T>  →  respuesta HTTP estandarizada
```

**Nunca:**
- Lógica de negocio en controllers
- Exponer entidades JPA directamente en respuestas HTTP
- Consultas `@Query` en los controllers
- Instanciar repositorios manualmente

---

## 4. Respuesta estándar — ApiResponse\<T\>

Todos los endpoints devuelven `ApiResponse<T>`. Nunca `Map<String, Object>` ni `ResponseEntity<MiEntidad>`.

```java
// Éxito con dato
return ResponseEntity.ok(ApiResponse.success(miDto));

// Éxito sin dato (ej: delete)
return ResponseEntity.ok(ApiResponse.success(null));

// Error (el GlobalExceptionHandler lo gestiona automáticamente)
throw new ResourceNotFoundException("Match not found: " + id);
```

`ApiResponse<T>` tiene: `success`, `data`, `message`, `timestamp`.

---

## 5. Entidades JPA — reglas

### AuditModel — OBLIGATORIO extender

```java
@Entity
@Table(name = "mi_tabla")
public class MiEntidad extends AuditModel {
    // NO declarar createdAt, updatedAt ni modifiedBy aquí
    // AuditModel ya los provee vía @MappedSuperclass
}
```

`AuditModel` provee: `createdAt` (`@CreationTimestamp`), `updatedAt` (`@UpdateTimestamp`), `modifiedBy` (siempre `"SYSTEM"`).

### Prohibido en entidades

- `@JsonIgnore` como solución a ciclos de serialización — usar DTOs
- Campos `createdAt`/`updatedAt` propios si ya extiende `AuditModel`
- `FetchType.EAGER` salvo casos documentados (relaciones `@ManyToMany` de jugadores)
- Lógica de negocio dentro de la entidad

### Actualizar — patrón obligatorio

```java
// CORRECTO: cargar desde BD y modificar con setters
MiEntidad entity = repository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("..."));
entity.setNombre(dto.getNombre());
repository.save(entity);

// INCORRECTO: crear entidad nueva para un update
MiEntidad entity = mapper.toEntity(dto);  // pierde ID, auditoría, relaciones
repository.save(entity);
```

---

## 6. Flyway — reglas de migración

```
src/main/resources/db/migration/
    V1__...sql    ← nunca modificar migraciones existentes
    V2__...sql
    ...
    V55__create_refresh_tokens.sql   ← última existente
    V56__nueva_migracion.sql         ← siguiente a crear
```

**Reglas:**
- Número siguiente al último existente (verificar antes)
- Nombre: `V{N}__{descripcion_snake_case}.sql`
- Nunca modificar una migración ya aplicada — crear una nueva que corrija
- Para añadir columnas a tablas existentes: `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`
- Columnas nullable por defecto en migraciones nuevas para no romper filas existentes
- `out-of-order=true` activado (varias ramas pueden crear migraciones simultáneas)

---

## 7. Seguridad — JWT con HttpOnly cookies

El sistema usa **HttpOnly cookies** (no headers `Authorization` para el flujo principal).

| Cookie | Propósito | Path |
|--------|-----------|------|
| `access_token` | JWT de acceso (15 min) | `/` |
| `refresh_token` | Refresh (7 días) | `/api/v1/auth` |

```java
// Extraer usuario actual en cualquier servicio/controller
String email = SecurityContextHolder.getContext()
    .getAuthentication().getName();
```

**Roles:** `ADMIN` > `MANAGER` > `USER`

- `@PreAuthorize("hasRole('ADMIN')")` para rutas de admin
- `@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")` para managers
- Endpoints de overlays y live scoring: públicos (sin auth)

**Fallback Bearer:** los overlays externos pueden usar `Authorization: Bearer <token>` como alternativa a la cookie.

---

## 8. CORS

Configurado en `WebConfig.corsConfigurationSource()`.

- Origen permitido en dev: `http://localhost:[*]` (acepta cualquier puerto local)
- Producción: variable de entorno `CORS_ALLOWED_ORIGINS`
- Usar siempre `setAllowedOriginPatterns()`, nunca `setAllowedOrigins()` (incompatible con `allowCredentials=true` y wildcards)

---

## 9. Rate limiting

`RateLimitInterceptor` aplica límites por tipo de endpoint:

| Tipo | Límite | Clave |
|------|--------|-------|
| Live (game-state, play-events) | 60 req/min | IP |
| Stats (player-stats, team-stats) | 120 req/min | Usuario autenticado, fallback IP |

Para añadir nuevos endpoints al rate limiting: registrar en `WebConfig.addInterceptors()`.

---

## 10. Nombres y convenciones

| Elemento | Convención | Ejemplo |
|---------|-----------|---------|
| Entidades | Singular, PascalCase | `PlayerMatch`, `PlayEvent` |
| Repositorios | `XxxRepository` | `PlayerMatchRepository` |
| Servicios (interface) | `XxxService` | `MatchService` |
| Servicios (impl) | `XxxServiceImpl` | `MatchServiceImpl` |
| DTOs | `XxxDTO` | `PlayerDTO`, `MatchDTO` |
| Mappers | `XxxMapper` | `PlayerMapper` |
| Controllers | `XxxController` | `MatchController` |
| Excepciones | `XxxException` | `ResourceNotFoundException` |

---

## 11. Aislamiento por deporte (SportType)

```java
// Filtrado por deporte en queries — patrón estándar
if (search != null && search.startsWith("sport:")) {
    String sportStr = search.split(":", 2)[1].trim().toUpperCase();
    SportType sportType = SportType.valueOf(sportStr);
    return repository.findBySportTypeOrderByNameAsc(sportType);
}
```

Nunca mezclar datos de SportType.SOFTBALL con SportType.BASEBALL en la misma respuesta.

---

## 12. Estadísticas — dos sistemas

| Sistema | Qué calcula | Cuándo |
|---------|------------|--------|
| Sistema A | Stats de PlayEvent (conteo de eventos) | En tiempo real al registrar cada evento |
| Sistema B | Stats derivadas (AVG, ERA, OBP, SLG) | Bajo demanda desde `/api/v1/player-statistics/**` |

**ERA softball = (Earned Runs × 7) / Innings Pitched** — el multiplicador es **7**, nunca 9.

Los endpoints de stats del Sistema B son públicos (sin auth) para que overlays y espectadores los consuman.

---

## 13. Qué NO tocar sin necesidad explícita

- `SecurityConfig` — afecta toda la autenticación
- `JwtFilter` — afecta todas las peticiones
- Migraciones Flyway ya aplicadas en producción
- Variables de entorno (`JWT_SECRET`, `DATABASE_URL`, `RESEND_API_KEY`)
- Configuración de HikariCP en `application.properties` (ajustada para Render 512 MB)
- `application-prod.properties` — perfil de producción

---

## 14. Variables de entorno requeridas en producción

| Variable | Descripción | Dev default |
|----------|-------------|-------------|
| `JWT_SECRET` | Secreto JWT (mín. 32 chars) | Valor largo hardcodeado (solo dev) |
| `DATABASE_URL` | URL PostgreSQL | `jdbc:postgresql://localhost:5432/api_party` |
| `DATABASE_USERNAME` | Usuario BD | `postgres` |
| `DATABASE_PASSWORD` | Contraseña BD | `postgres` |
| `CORS_ALLOWED_ORIGINS` | Origen frontend | `http://localhost:[*]` |
| `RESEND_API_KEY` | API key de email | (vacío — stub activo en dev) |
| `COOKIE_SECURE` | Cookies HTTPS only | `false` |
| `EMAIL_STUB_ALLOWED` | Stub email en lugar de Resend | `true` |

---

## 15. Comandos

```bash
# Desde backend/matchmetrics-api/
./mvnw spring-boot:run          # arrancar (puerto 8080, perfil dev)
./mvnw test                     # ejecutar tests
./mvnw package -DskipTests      # empaquetar sin tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod   # perfil producción
```

---

## 16. Antes de entregar cualquier cambio

1. `./mvnw test` — sin errores
2. Verificar que el backend arranca (`./mvnw spring-boot:run`) sin excepciones en el log de inicio
3. Si se añadió migración Flyway: verificar que Flyway la aplica sin error (`Flyway migrated 1 migration`)
4. Si la tarea es del roadmap: actualizar `ROADMAP_FINAL.md` + JSON en `frontend/matchmetrics-front/public/` — ver `AGENTS.md §29`

---

## 17. Roadmap — actualización obligatoria

Al completar una tarea:
1. Añadir `✅ COMPLETADO YYYY-MM-DD` en `ROADMAP_FINAL.md`
2. Cambiar `"status": "PENDING"` → `"status": "COMPLETED"` en `frontend/matchmetrics-front/public/roadmap-functional.json`

Ver reglas completas en `AGENTS.md §29`.
