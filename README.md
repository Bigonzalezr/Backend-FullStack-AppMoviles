# Backend Full-Stack App Móviles - LevelUp Gamer

Sistema backend de microservicios para aplicación de e-commerce de videojuegos.

## 🏗️ Arquitectura

El proyecto consta de **8 microservicios** desarrollados con Spring Boot 3.4.5 y Java 21:

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **msvc-productos** | 8002 | Gestión de catálogo de productos (videojuegos) |
| **msvc-carrito** | 8003 | Carrito de compras |
| **msvc-resenas** | 8004 | Reseñas y calificaciones de productos |
| **msvc-pagos** | 8005 | Procesamiento de pagos |
| **msvc-pedidos** | 8006 | Gestión de pedidos/órdenes |
| **msvc-logs** | 8007 | Registro de actividades del sistema |
| **msvc-usuarios** | 8008 | Autenticación y gestión de usuarios |
| **msvc-admin** | 8009 | Panel de administración |

## 🔐 Seguridad

### Autenticación JWT

El microservicio de usuarios implementa autenticación stateless basada en tokens JWT:

- **Algoritmo**: HS256 (HMAC con SHA-256)
- **Expiración**: 24 horas (configurable)
- **Encriptación de contraseñas**: BCrypt
- **Spring Security**: Configurado con filtros personalizados

### Endpoints de Autenticación

```
POST /api/v1/auth/login
Body: { "username": "admin", "password": "admin123" }
Response: { "token": "eyJhbGc...", "type": "Bearer", "usuarioId": 1, "username": "admin", "email": "...", "rol": "ADMIN" }

POST /api/v1/auth/register
Body: { "username": "...", "email": "...", "password": "...", "nombre": "...", "apellido": "..." }

GET /api/v1/auth/me
Header: Authorization: Bearer {token}
Response: UsuarioDTO del usuario autenticado
```

### Roles Implementados

- **USER**: Usuario estándar con acceso básico
- **ADMIN**: Acceso completo a todas las operaciones
- **MODERATOR**: Permisos intermedios para gestión de contenido

### Usuarios de Prueba

| Username | Password | Rol | Email |
|----------|----------|-----|-------|
| admin | admin123 | ADMIN | admin@levelupgamer.com |
| juan_perez | password123 | USER | juan.perez@email.com |
| maria_garcia | password123 | USER | maria.garcia@email.com |
| mod_carlos | mod123 | MODERATOR | carlos.mod@levelupgamer.com |

## 🔧 Tecnologías

- **Spring Boot**: 3.4.5
- **Spring Cloud**: 2024.0.1
- **Java**: 21
- **Base de Datos**: H2 (persistencia en archivo)
- **Seguridad**: Spring Security + JWT (jjwt 0.12.3)
- **Comunicación entre servicios**: OpenFeign
- **Documentación API**: SpringDoc OpenAPI 2.8.9
- **HATEOAS**: Enlaces relacionales en APIs REST
- **Validación**: Jakarta Bean Validation
- **Utilidades**: Lombok

## 🚀 Instalación y Ejecución

### Requisitos Previos

- JDK 21
- Maven 3.8+

### Iniciar Servicios

Cada microservicio se ejecuta de forma independiente:

```powershell
# Desde la raíz de cada módulo (ej: msvc-usuarios)
./mvnw spring-boot:run

# O compilar y ejecutar
./mvnw clean package
java -jar target/msvc-usuarios-0.0.1-SNAPSHOT.jar
```

### Compilación del Proyecto Completo

```powershell
# Desde la raíz del proyecto
mvn clean install
```

## 📡 APIs REST

### msvc-usuarios (Puerto 8008)

#### Endpoints Públicos
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/register` - Registrar nuevo usuario
- `POST /api/v1/usuarios` - Crear usuario (alias de register)

#### Endpoints Autenticados
- `GET /api/v1/auth/me` - Obtener usuario actual
- `GET /api/v1/usuarios` - Listar todos los usuarios
- `GET /api/v1/usuarios/{id}` - Obtener usuario por ID
- `GET /api/v1/usuarios/username/{username}` - Buscar por username
- `GET /api/v1/usuarios/email/{email}` - Buscar por email
- `GET /api/v1/usuarios/activos` - Listar usuarios activos
- `GET /api/v1/usuarios/rol/{rol}` - Filtrar por rol
- `GET /api/v1/usuarios/buscar?q={searchTerm}` - Búsqueda por nombre/apellido
- `PUT /api/v1/usuarios/{id}` - Actualizar usuario
- `PATCH /api/v1/usuarios/{id}/activar` - Activar usuario
- `PATCH /api/v1/usuarios/{id}/desactivar` - Desactivar usuario
- `DELETE /api/v1/usuarios/{id}` - Eliminar usuario (solo ADMIN)

### Otros Microservicios

Cada servicio expone sus propias APIs REST siguiendo el patrón `/api/v1/{resource}`:

- **msvc-productos**: CRUD de productos, búsqueda, categorías
- **msvc-carrito**: Gestión de ítems del carrito
- **msvc-resenas**: Crear/listar reseñas y calificaciones
- **msvc-pagos**: Procesar pagos, verificar estados
- **msvc-pedidos**: Crear/gestionar pedidos, cambiar estados
- **msvc-logs**: Registrar y consultar actividades
- **msvc-admin**: Paneles de administración y reportes

## 🌐 CORS

Todos los microservicios están configurados para aceptar peticiones desde:

- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)
- `http://localhost:8080` (Vue)
- `http://localhost:5173` (Vite)

Métodos permitidos: GET, POST, PUT, PATCH, DELETE, OPTIONS

## 🗄️ Base de Datos

Cada microservicio usa H2 con persistencia en archivo:

```properties
spring.datasource.url=jdbc:h2:file:./data/msvc_{nombre}
```

### Consolas H2

Accesibles en `http://localhost:{puerto}/h2-console`:

- Usuario: `sa`
- Contraseña: `sa`

## 📝 Configuración

### Archivo application.properties (msvc-usuarios)

```properties
spring.application.name=msvc-usuarios
server.port=8008

# Base de datos
spring.datasource.url=jdbc:h2:file:./data/msvc_usuarios
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=sa

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000

# Logging
logging.level.org.hibernate.SQL=debug
spring.jpa.show-sql=true
```

## 🔄 Comunicación entre Servicios

Los microservicios se comunican mediante **OpenFeign**:

```java
@FeignClient(name = "msvc-usuarios", url = "http://localhost:8008")
public interface UsuarioClientRest {
    @GetMapping("/api/v1/usuarios/{id}")
    ResponseEntity<UsuarioDTO> findById(@PathVariable Long id);
}
```

## 🛡️ Seguridad Implementada

### Componentes Principales

1. **JwtTokenProvider**: Generación y validación de tokens JWT
2. **JwtAuthenticationFilter**: Intercepta peticiones para validar tokens
3. **CustomUserDetailsService**: Carga usuarios para autenticación Spring Security
4. **SecurityConfig**: Configuración de seguridad global

### Flujo de Autenticación

1. Usuario envía credenciales a `/api/v1/auth/login`
2. Backend valida credenciales con BCrypt
3. Se genera token JWT firmado
4. Cliente incluye token en header `Authorization: Bearer {token}`
5. JwtAuthenticationFilter valida token en cada petición
6. Spring Security autoriza según roles

## 📊 Estructura del Proyecto

```
msvc-{nombre}/
├── src/
│   ├── main/
│   │   ├── java/com/appmovil/msvc/{nombre}/
│   │   │   ├── config/          # Configuraciones (CORS, Security, Inicialización)
│   │   │   ├── controllers/     # Controladores REST
│   │   │   ├── dtos/            # Objetos de transferencia de datos
│   │   │   ├── exceptions/      # Excepciones personalizadas
│   │   │   ├── models/          # Entidades JPA
│   │   │   │   └── entities/
│   │   │   ├── repositories/    # Repositorios Spring Data
│   │   │   ├── services/        # Capa de negocio
│   │   │   ├── security/        # Componentes de seguridad (solo usuarios)
│   │   │   └── MsvcApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## 🚧 Estado del Proyecto

### ✅ Completado

- Estructura de 8 microservicios
- Modelo de datos con validaciones Jakarta
- APIs REST con HATEOAS
- Sistema de autenticación JWT
- Encriptación de contraseñas con BCrypt
- Configuración CORS en todos los servicios
- Manejo de excepciones centralizado
- Documentación OpenAPI
- Inicialización de datos de prueba
- Comunicación inter-servicios con Feign
- **Validaciones de negocio**:
  - Verificación de stock antes de agregar al carrito
  - Validación de existencia de usuario en operaciones de pedidos
  - Verificación de disponibilidad de productos al confirmar pedido
  - Actualización de stock al crear/cancelar pedidos
  - Prevención de reseñas duplicadas por usuario/producto
  - Límite máximo de 10 unidades por producto en carrito
- **Configuración por ambiente**:
  - application-dev.properties para todos los servicios
  - application-test.properties para todos los servicios
  - Base de datos con ddl-auto=update (preserva datos)
- **Resilience4j Circuit Breaker**:
  - Dependencias agregadas a 7 microservicios
  - Configuración de circuit breaker para todos los Feign clients
  - Clases fallback implementadas en servicios críticos (carrito, pedidos)
  - Feign configurado con circuitbreaker habilitado
  - Health indicators para monitoreo de circuit breakers

### 🔄 Pendiente

#### MEDIUM Priority
- **Testing**:
  - Unit tests para servicios
  - Integration tests para APIs

#### LOW Priority
- **Docker**:
  - Dockerfile para cada servicio
  - docker-compose.yml
  
- **Documentación**:
  - Swagger descriptions mejoradas
  - Colección Postman
  
- **Monitoreo**:
  - Spring Boot Actuator endpoints
  - Health checks personalizados

## 📚 Documentación API

Cada servicio expone documentación OpenAPI en:

```
http://localhost:{puerto}/swagger-ui.html
http://localhost:{puerto}/v3/api-docs
```

## 🤝 Contribución

Este es un proyecto académico para desarrollo de aplicaciones móviles.

## 📄 Licencia

Proyecto educativo - Universidad

---

**Última actualización**: Enero 2025
