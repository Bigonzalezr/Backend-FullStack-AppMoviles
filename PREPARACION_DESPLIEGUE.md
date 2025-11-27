# 🚀 Guía de Preparación para Despliegue AWS

## ✅ Archivos Creados para Despliegue

### 1. Dependencias MySQL
✅ Agregada dependencia `mysql-connector-j` a todos los microservicios para conectar con RDS MySQL en producción.

### 2. Configuración de Producción
✅ Archivos `application-prod.properties` creados en cada microservicio:
- `msvc-usuarios/src/main/resources/application-prod.properties`
- `msvc-productos/src/main/resources/application-prod.properties`
- `msvc-carrito/src/main/resources/application-prod.properties`
- `msvc-pedidos/src/main/resources/application-prod.properties`
- `msvc-pagos/src/main/resources/application-prod.properties`
- `msvc-logs/src/main/resources/application-prod.properties`
- `msvc-admin/src/main/resources/application-prod.properties`
- `msvc-resenas/src/main/resources/application-prod.properties`

**Variables de entorno configuradas:**
- `RDS_HOSTNAME`: Hostname de la base de datos RDS
- `RDS_PORT`: Puerto de MySQL (3306)
- `RDS_DB_NAME`: Nombre de la base de datos
- `RDS_USERNAME`: Usuario de la base de datos
- `RDS_PASSWORD`: Contraseña de la base de datos
- `JWT_SECRET`: Secreto para tokens JWT (solo msvc-usuarios)
- `CORS_ORIGINS`: Orígenes permitidos para CORS
- `*_SERVICE_URL`: URLs de otros microservicios

### 3. Configuración Elastic Beanstalk
✅ Archivos `.ebextensions/01-environment.config` creados en cada microservicio con:
- Puerto del servidor: 5000
- Perfil de Spring activo: prod
- Health check configurado en `/actuator/health`
- Proxy Nginx configurado

### 4. Dockerfiles
✅ Dockerfile multi-stage optimizado creado para cada microservicio:
- **Stage 1 (Build)**: Compila la aplicación con Maven
- **Stage 2 (Runtime)**: Imagen ligera solo con JRE
- Usuario no-root para seguridad
- Health checks configurados
- Memoria optimizada: 256MB-512MB

**Puertos expuestos:**
- msvc-usuarios: 8008
- msvc-productos: 8002
- msvc-carrito: 8003
- msvc-pedidos: 8006
- msvc-pagos: 8004
- msvc-logs: 8007
- msvc-admin: 8005
- msvc-resenas: 8001

### 5. Archivo .dockerignore
✅ Creado para optimizar builds de Docker

---

## 📦 Empaquetar Microservicios (JARs)

Para generar los archivos JAR ejecutables:

```powershell
# Empaquetar msvc-usuarios
cd msvc-usuarios
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-productos
cd ..\msvc-productos
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-carrito
cd ..\msvc-carrito
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-pedidos
cd ..\msvc-pedidos
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-pagos
cd ..\msvc-pagos
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-logs
cd ..\msvc-logs
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-admin
cd ..\msvc-admin
.\mvnw.cmd clean package -DskipTests

# Empaquetar msvc-resenas
cd ..\msvc-resenas
.\mvnw.cmd clean package -DskipTests
```

Los archivos JAR se generarán en `target/` de cada microservicio.

---

## 🐳 Construir Imágenes Docker

```powershell
# Construir imagen de msvc-usuarios
cd msvc-usuarios
docker build -t appmoviles-usuarios:latest .

# Construir imagen de msvc-productos
cd ..\msvc-productos
docker build -t appmoviles-productos:latest .

# Construir imagen de msvc-carrito
cd ..\msvc-carrito
docker build -t appmoviles-carrito:latest .

# Y así sucesivamente para cada microservicio...
```

---

## ☁️ Próximos Pasos

Consulta el archivo `DESPLIEGUE_AWS.md` para:

1. **Crear Base de Datos RDS MySQL**
2. **Desplegar en Elastic Beanstalk** (Opción más fácil)
3. **Desplegar en ECS con Fargate** (Opción más escalable)
4. **Configurar Application Load Balancer**
5. **Desplegar Frontend en S3 + CloudFront**
6. **Configurar Route 53 para DNS**
7. **Configurar CI/CD con GitHub Actions**

---

## 🔐 Variables de Entorno Requeridas en AWS

Al crear los environments en Elastic Beanstalk, configurar:

```bash
# Base de datos
RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com
RDS_PORT=3306
RDS_DB_NAME=appmoviles
RDS_USERNAME=admin
RDS_PASSWORD=[tu-password-seguro]

# JWT (solo para msvc-usuarios)
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000

# CORS
CORS_ORIGINS=https://tu-dominio.cloudfront.net,https://tu-dominio.com

# URLs de servicios (configurar según ALB)
PRODUCTOS_SERVICE_URL=http://appmoviles-productos-env.elasticbeanstalk.com
USUARIOS_SERVICE_URL=http://appmoviles-usuarios-env.elasticbeanstalk.com
CARRITO_SERVICE_URL=http://appmoviles-carrito-env.elasticbeanstalk.com
PEDIDOS_SERVICE_URL=http://appmoviles-pedidos-env.elasticbeanstalk.com
PAGOS_SERVICE_URL=http://appmoviles-pagos-env.elasticbeanstalk.com
```

---

## 📊 Estado de Compilación

| Microservicio | Estado | JAR |
|--------------|--------|-----|
| msvc-usuarios | ✅ Compilando | Pendiente |
| msvc-productos | ✅ Compilando | Pendiente |
| msvc-carrito | ✅ Compilando | Pendiente |
| msvc-pedidos | ⏳ Pendiente | Pendiente |
| msvc-pagos | ⏳ Pendiente | Pendiente |
| msvc-logs | ✅ Compilando | Pendiente |
| msvc-admin | ✅ Compilando | Pendiente |
| msvc-resenas | ⚠️ Requiere corrección | Pendiente |

---

## 💡 Notas Importantes

1. **H2 vs MySQL**: Los microservicios usarán H2 en desarrollo y MySQL en producción según el perfil activo.
2. **Perfil prod**: Se activa automáticamente mediante `SPRING_PROFILES_ACTIVE=prod`.
3. **Health checks**: Todos los microservicios exponen `/actuator/health` para monitoreo.
4. **Seguridad**: Los Dockerfiles usan usuarios no-root y límites de memoria.
5. **Optimización**: Builds multi-stage reducen el tamaño de las imágenes finales.
