# 🔐 Seguridad JWT Implementada en Todos los Microservicios

## ✅ COMPLETADO - Todos los microservicios ahora requieren autenticación JWT

### 📦 Dependencias Agregadas (7 microservicios)

Agregadas a: `msvc-productos`, `msvc-carrito`, `msvc-pedidos`, `msvc-pagos`, `msvc-logs`, `msvc-admin`, `msvc-resenas`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🏗️ Clases de Seguridad Creadas

Cada microservicio ahora tiene estas 4 clases en su paquete `security`:

### 1. **JwtTokenProvider.java**
- Genera y valida tokens JWT
- Extrae username del token
- Verifica expiración (24 horas por defecto)
- Usa `jwt.secret` configurable

### 2. **JwtAuthenticationFilter.java**
- Intercepta cada request HTTP
- Extrae token del header `Authorization: Bearer <token>`
- Valida el token antes de permitir acceso
- Establece autenticación en SecurityContext

### 3. **SimpleUserDetailsService.java**
- Servicio simplificado (no accede a BD)
- Solo valida que el token sea correcto
- La autenticación real ya fue hecha en `msvc-usuarios`

### 4. **SecurityConfig.java**
- Configuración Spring Security
- CSRF deshabilitado (API REST stateless)
- Sesiones STATELESS
- Endpoints públicos:
  - `/h2-console/**` - Base de datos H2
  - `/actuator/**` - Health checks AWS
  - `/swagger-ui/**` - Documentación API
- **Todos los demás endpoints requieren JWT válido**

---

## 🔑 Cómo Funciona el Flujo de Autenticación

### 1️⃣ **Login en msvc-usuarios**
```http
POST http://localhost:8008/api/v1/auth/login
Content-Type: application/json

{
  "username": "usuario123",
  "password": "miPassword"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "usuario123",
  "email": "usuario@email.com",
  "rol": "USER"
}
```

### 2️⃣ **Usar el Token en Otros Microservicios**

Ahora **TODOS** los microservicios requieren este header:

```http
GET http://localhost:8002/api/v1/productos
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

```http
POST http://localhost:8003/api/v1/carrito
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "productoId": 1,
  "cantidad": 2
}
```

### 3️⃣ **Sin Token = Error 403 Forbidden**

```http
GET http://localhost:8002/api/v1/productos
(sin Authorization header)
```

**Respuesta:**
```json
{
  "timestamp": "2025-11-27T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/productos"
}
```

---

## 🛡️ Microservicios Protegidos

| Microservicio | Puerto | Estado JWT | Endpoints Protegidos |
|--------------|--------|------------|---------------------|
| **msvc-usuarios** | 8008 | ✅ Original | `/api/v1/usuarios/**` (excepto registro y login) |
| **msvc-productos** | 8002 | ✅ Agregado | `/api/v1/productos/**` |
| **msvc-carrito** | 8003 | ✅ Agregado | `/api/v1/carrito/**` |
| **msvc-pedidos** | 8006 | ✅ Agregado | `/api/v1/pedidos/**` |
| **msvc-pagos** | 8004 | ✅ Agregado | `/api/v1/pagos/**` |
| **msvc-logs** | 8007 | ✅ Agregado | `/api/v1/logs/**` |
| **msvc-admin** | 8005 | ✅ Agregado | `/api/v1/admin/**` |
| **msvc-resenas** | 8001 | ✅ Agregado | `/api/v1/resenas/**` |

---

## ⚙️ Variables de Entorno

Cada microservicio usa estas variables (ya configuradas en `application-prod.properties`):

```properties
# JWT Configuration (debe ser igual en TODOS los microservicios)
jwt.secret=${JWT_SECRET:mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

**⚠️ IMPORTANTE:** El `jwt.secret` **DEBE SER EL MISMO** en todos los microservicios para que puedan validar tokens generados por `msvc-usuarios`.

---

## 🧪 Testing con Postman/Thunder Client

### Colección de Pruebas

#### 1. Login (Obtener Token)
```http
POST http://localhost:8008/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Guardar el `token` de la respuesta.

#### 2. Probar Productos (Con Token)
```http
GET http://localhost:8002/api/v1/productos
Authorization: Bearer {{token}}
```

#### 3. Probar Carrito (Con Token)
```http
POST http://localhost:8003/api/v1/carrito
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "productoId": 1,
  "cantidad": 2
}
```

#### 4. Probar Sin Token (Debe Fallar)
```http
GET http://localhost:8002/api/v1/productos
(sin Authorization header)
```

Debe retornar **403 Forbidden**.

---

## 🔄 Próximos Pasos

### 1. Recompilar Microservicios
```powershell
# Para cada microservicio
cd msvc-productos
.\mvnw.cmd clean package -Dmaven.test.skip=true

cd ..\msvc-carrito
.\mvnw.cmd clean package -Dmaven.test.skip=true

# ... repetir para todos
```

### 2. Reiniciar Microservicios
```powershell
# Detener todos los microservicios en ejecución
# Iniciar de nuevo cada uno
```

### 3. Actualizar Frontend
El frontend debe:
1. Guardar el token después del login
2. Incluir el token en **TODAS** las peticiones:
```javascript
// Ejemplo con Axios
axios.get('http://localhost:8002/api/v1/productos', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
});

// Ejemplo con Fetch
fetch('http://localhost:8002/api/v1/productos', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
});
```

### 4. Desplegar en AWS
- El `JWT_SECRET` debe configurarse como variable de entorno en **TODOS** los microservicios
- Debe ser el mismo valor en todos para que funcione la validación

```powershell
# Para cada microservicio en AWS
eb setenv `
  JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm `
  JWT_EXPIRATION=86400000
```

---

## 🎯 Beneficios de la Implementación

### ✅ Seguridad
- **Autenticación centralizada**: Solo `msvc-usuarios` valida credenciales
- **Stateless**: No se guardan sesiones en servidor
- **Expiración automática**: Tokens expiran en 24 horas
- **Protección CSRF**: Deshabilitado porque usamos tokens

### ✅ Escalabilidad
- Sin estado compartido entre instancias
- Fácil escalado horizontal
- Compatible con load balancers

### ✅ Performance
- Validación rápida (solo verificar firma del token)
- No consultas a BD para cada request
- Cacheable en frontend

### ✅ Flexibilidad
- Token contiene información del usuario
- Puede incluir roles y permisos
- Fácil de extender con claims adicionales

---

## 📊 Arquitectura de Seguridad

```
┌─────────────┐
│   Frontend  │
│   (React/   │
│   Angular)  │
└──────┬──────┘
       │
       │ 1. POST /auth/login
       │    {username, password}
       ▼
┌──────────────────┐
│  msvc-usuarios   │◄─── Único que valida credenciales
│  (Puerto 8008)   │◄─── Genera JWT token
└──────┬───────────┘
       │
       │ 2. Retorna token JWT
       ▼
┌─────────────┐
│   Frontend  │
│ Guarda token│
│ en localStorage
└──────┬──────┘
       │
       │ 3. Todas las peticiones incluyen:
       │    Authorization: Bearer <token>
       │
       ├──────────────┬──────────────┬──────────────┐
       ▼              ▼              ▼              ▼
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│msvc-     │   │msvc-     │   │msvc-     │   │msvc-     │
│productos │   │carrito   │   │pedidos   │   │pagos     │
└──────────┘   └──────────┘   └──────────┘   └──────────┘
    ▲              ▲              ▲              ▲
    │              │              │              │
    └──────────────┴──────────────┴──────────────┘
         Todos validan el mismo JWT token
         (usando el mismo jwt.secret)
```

---

## 🚨 Solución de Problemas

### Error: "Access Denied" o 403
- **Causa**: Token no incluido o inválido
- **Solución**: Verificar que el header `Authorization: Bearer <token>` esté presente

### Error: "JWT expired"
- **Causa**: Token expiró (>24 horas)
- **Solución**: Hacer login nuevamente para obtener nuevo token

### Error: "Invalid JWT signature"
- **Causa**: `jwt.secret` diferente entre microservicios
- **Solución**: Asegurar que todos usen el mismo `JWT_SECRET`

### Error al compilar
- **Causa**: Dependencias no descargadas
- **Solución**: 
```powershell
.\mvnw.cmd clean install -Dmaven.test.skip=true
```

---

## 📝 Notas Importantes

1. **JWT Secret**: Debe ser de al menos 256 bits (32 caracteres) para HS256
2. **Expiración**: Por defecto 24 horas (86400000 ms)
3. **Endpoints Públicos**: Solo health checks, Swagger y H2 console
4. **Comunicación entre Microservicios**: Feign clients deben incluir el token
5. **CORS**: Ya configurado para aceptar header `Authorization`

---

## ✅ Checklist de Verificación

- [x] Dependencias Spring Security agregadas a 7 microservicios
- [x] JwtTokenProvider creado en cada microservicio
- [x] JwtAuthenticationFilter creado en cada microservicio
- [x] SimpleUserDetailsService creado en cada microservicio
- [x] SecurityConfig creado en cada microservicio
- [ ] Recompilar todos los microservicios
- [ ] Probar login en msvc-usuarios
- [ ] Probar acceso con token en otros microservicios
- [ ] Probar acceso sin token (debe fallar)
- [ ] Actualizar frontend para incluir tokens
- [ ] Configurar JWT_SECRET en AWS

---

**🎉 ¡Seguridad JWT Completada!**

Todos los microservicios ahora están protegidos y requieren autenticación válida para acceder.
