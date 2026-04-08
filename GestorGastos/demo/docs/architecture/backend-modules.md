# Backend module conventions

## Active modules in phase 0
- `shared`: cross-cutting configuration, error handling, web concerns and technical infrastructure.
- `identity`: authentication foundations, user persistence and refresh token lifecycle.

## Layer rules
- `api`: HTTP adapters only; never depend directly on `infrastructure`.
- `application`: orchestration and contracts; may depend on `domain`.
- `domain`: pure business types and rules; never depend on Spring.
- `infrastructure`: persistence, security, framework adapters and configuration.

## Dependency rules enforced by tests
- No cycles between top-level modules.
- `..domain..` classes must not depend on `org.springframework..`.
- `..api..` classes must not depend on `..infrastructure..`.

## Conventions
- Use UUIDs as identifiers.
- Persist timestamps in UTC.
- Persist refresh tokens hashed, never in plaintext.
- Prefer ownership-aware repository operations such as `findByIdAndUserId(...)`.
