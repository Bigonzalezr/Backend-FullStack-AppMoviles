# 🚀 Guía Completa: Despliegue en AWS desde Cero

## 📋 Requisitos Previos

### 1. Cuenta de AWS
- [ ] Crear cuenta en https://aws.amazon.com
- [ ] Tener tarjeta de crédito/débito válida
- [ ] Verificar identidad (puede tomar 24 horas)

### 2. Software Necesario en tu PC
```powershell
# Instalar AWS CLI
winget install Amazon.AWSCLI

# Instalar EB CLI (Elastic Beanstalk CLI)
pip install awsebcli

# Verificar instalación
aws --version
eb --version
```

---

## 🔐 PASO 1: Configurar Credenciales AWS

### 1.1. Crear Usuario IAM con permisos
1. Ir a AWS Console → IAM → Users
2. Click "Create user"
   - Username: `appmoviles-deployer`
3. Attach policies directly:
   - ✅ `AWSElasticBeanstalkFullAccess`
   - ✅ `AmazonRDSFullAccess`
   - ✅ `AmazonS3FullAccess`
   - ✅ `CloudFrontFullAccess`
   - ✅ `IAMFullAccess` (o IAMUserChangePassword)
4. Click "Create user"

### 1.2. Crear Access Keys
1. Seleccionar el usuario creado
2. Security credentials → Create access key
3. Use case: "Command Line Interface (CLI)"
4. Guardar:
   - Access Key ID: `AKIA...`
   - Secret Access Key: `wJalr...` (¡solo se muestra una vez!)

### 1.3. Configurar AWS CLI
```powershell
# Configurar credenciales
aws configure

# Ingresar:
AWS Access Key ID [None]: AKIA...
AWS Secret Access Key [None]: wJalr...
Default region name [None]: us-east-1
Default output format [None]: json

# Verificar
aws sts get-caller-identity
```

**Salida esperada:**
```json
{
    "UserId": "AIDA...",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/appmoviles-deployer"
}
```

---

## 🗄️ PASO 2: Crear Base de Datos RDS MySQL

### 2.1. Crear DB desde AWS Console
1. AWS Console → RDS → Create database

### 2.2. Configuración Básica
```yaml
Engine: MySQL 8.0
Templates: Free tier (para empezar) o Production

# Settings
DB instance identifier: appmoviles-db
Master username: admin
Master password: [TuPasswordSeguro123!]  # Guardar esto!

# Instance configuration
DB instance class: db.t3.micro (Free tier) o db.t3.small

# Storage
Storage type: General Purpose SSD (gp3)
Allocated storage: 20 GB
Enable storage autoscaling: Yes (max 100 GB)

# Connectivity
Public access: Yes (para desarrollo) o No (para producción con VPC)
VPC security group: Create new → "appmoviles-db-sg"

# Database authentication
Password authentication

# Additional configuration
Initial database name: appmoviles
Backup retention: 7 days
Enable deletion protection: No (para desarrollo) o Yes (producción)
```

### 2.3. Configurar Security Group de RDS
1. EC2 → Security Groups → Buscar "appmoviles-db-sg"
2. Edit inbound rules:
   ```
   Type: MySQL/Aurora
   Protocol: TCP
   Port: 3306
   Source: 0.0.0.0/0 (desarrollo) o Security Group de EB (producción)
   ```

### 2.4. Guardar Datos de Conexión
```bash
# Endpoint (aparece después de crear la BD)
RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com
RDS_PORT=3306
RDS_DB_NAME=appmoviles
RDS_USERNAME=admin
RDS_PASSWORD=TuPasswordSeguro123!
```

**⏰ Tiempo estimado: 5-10 minutos**

---

## ☁️ PASO 3: Desplegar Microservicios en Elastic Beanstalk

### 3.1. Desplegar msvc-usuarios (Primer microservicio)

#### 3.1.1. Inicializar Elastic Beanstalk
```powershell
cd "c:\Users\bena0\OneDrive\Escritorio\Backend-FullStack-AppMoviles\msvc-usuarios"

# Inicializar aplicación
eb init

# Responder:
# Select a default region: 2) us-east-1
# Enter Application Name: appmoviles
# Select a platform: 1) Corretto
# Select a platform branch: Corretto 21
# Do you wish to continue with CodeCommit? n
# Do you want to set up SSH? y
```

#### 3.1.2. Crear Environment
```powershell
# Crear environment de producción
eb create appmoviles-usuarios-prod

# Responder:
# Enter Environment Name: appmoviles-usuarios-prod
# Enter DNS CNAME prefix: appmoviles-usuarios
# Select a load balancer type: 2) application
```

**⏰ Tiempo: 5-10 minutos por microservicio**

#### 3.1.3. Configurar Variables de Entorno
```powershell
# Configurar todas las variables
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm `
  JWT_EXPIRATION=86400000 `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000

# Desplegar JAR
eb deploy
```

#### 3.1.4. Verificar Despliegue
```powershell
# Ver URL del servicio
eb status

# Ver logs
eb logs

# Abrir en navegador
eb open

# Probar health check
curl http://appmoviles-usuarios.elasticbeanstalk.com/actuator/health
```

**Salida esperada:**
```json
{"status":"UP"}
```

### 3.2. Desplegar Resto de Microservicios

Repetir el proceso para cada microservicio:

#### msvc-productos
```powershell
cd ..\msvc-productos
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-productos-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

#### msvc-carrito
```powershell
cd ..\msvc-carrito
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-carrito-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  PRODUCTOS_SERVICE_URL=http://appmoviles-productos-prod.elasticbeanstalk.com `
  USUARIOS_SERVICE_URL=http://appmoviles-usuarios-prod.elasticbeanstalk.com `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

#### msvc-pedidos
```powershell
cd ..\msvc-pedidos
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-pedidos-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  PRODUCTOS_SERVICE_URL=http://appmoviles-productos-prod.elasticbeanstalk.com `
  USUARIOS_SERVICE_URL=http://appmoviles-usuarios-prod.elasticbeanstalk.com `
  PAGOS_SERVICE_URL=http://appmoviles-pagos-prod.elasticbeanstalk.com `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

#### msvc-pagos
```powershell
cd ..\msvc-pagos
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-pagos-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  PEDIDOS_SERVICE_URL=http://appmoviles-pedidos-prod.elasticbeanstalk.com `
  USUARIOS_SERVICE_URL=http://appmoviles-usuarios-prod.elasticbeanstalk.com `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

#### msvc-logs
```powershell
cd ..\msvc-logs
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-logs-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  USUARIOS_SERVICE_URL=http://appmoviles-usuarios-prod.elasticbeanstalk.com `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

#### msvc-admin
```powershell
cd ..\msvc-admin
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-admin-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  PRODUCTOS_SERVICE_URL=http://appmoviles-productos-prod.elasticbeanstalk.com `
  PEDIDOS_SERVICE_URL=http://appmoviles-pedidos-prod.elasticbeanstalk.com `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

#### msvc-resenas
```powershell
cd ..\msvc-resenas
eb init -p corretto-21 appmoviles --region us-east-1
eb create appmoviles-resenas-prod
eb setenv `
  SPRING_PROFILES_ACTIVE=prod `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=TuPasswordSeguro123! `
  PRODUCTOS_SERVICE_URL=http://appmoviles-productos-prod.elasticbeanstalk.com `
  USUARIOS_SERVICE_URL=http://appmoviles-usuarios-prod.elasticbeanstalk.com `
  CORS_ORIGINS=http://localhost:5173,http://localhost:3000
eb deploy
```

### 3.3. Verificar Todos los Servicios

```powershell
# Script para verificar todos
$servicios = @(
    'appmoviles-usuarios-prod',
    'appmoviles-productos-prod',
    'appmoviles-carrito-prod',
    'appmoviles-pedidos-prod',
    'appmoviles-pagos-prod',
    'appmoviles-logs-prod',
    'appmoviles-admin-prod',
    'appmoviles-resenas-prod'
)

foreach($s in $servicios) {
    Write-Host "`nVerificando $s..." -ForegroundColor Yellow
    $url = "http://$s.us-east-1.elasticbeanstalk.com/actuator/health"
    try {
        $response = Invoke-RestMethod -Uri $url
        Write-Host "✅ $s : $($response.status)" -ForegroundColor Green
    } catch {
        Write-Host "❌ $s : ERROR" -ForegroundColor Red
    }
}
```

**⏰ Tiempo total: 40-60 minutos para 8 microservicios**

---

## 🔀 PASO 4: Configurar Application Load Balancer (Opcional pero Recomendado)

### 4.1. ¿Por qué un ALB?
- **URLs unificadas**: Un solo dominio para todo
- **HTTPS fácil**: Certificado SSL en un solo lugar
- **Ruteo inteligente**: /api/usuarios → msvc-usuarios, etc.

### 4.2. Crear ALB desde AWS Console

1. **EC2 → Load Balancers → Create Load Balancer**
   - Type: Application Load Balancer
   - Name: `appmoviles-alb`
   - Scheme: Internet-facing
   - IP address type: IPv4

2. **Network mapping**
   - VPC: Default VPC
   - Availability Zones: Seleccionar todas (mínimo 2)

3. **Security groups**
   - Create new: `appmoviles-alb-sg`
   - Inbound rules:
     ```
     HTTP (80): 0.0.0.0/0
     HTTPS (443): 0.0.0.0/0
     ```

4. **Listeners and routing**
   - HTTP (80): Forward to → Create target group

### 4.3. Crear Target Groups

Para cada microservicio, crear un target group:

```bash
# Ejemplo: Target group para usuarios
Name: appmoviles-usuarios-tg
Target type: Instance
Protocol: HTTP
Port: 80
Health check path: /actuator/health
```

Repetir para todos los microservicios.

### 4.4. Configurar Listener Rules

1. ALB → Listeners → View/edit rules
2. Agregar reglas de ruteo:

```yaml
# Regla 1: Usuarios
IF path is /api/usuarios*
THEN forward to appmoviles-usuarios-tg

# Regla 2: Productos
IF path is /api/productos*
THEN forward to appmoviles-productos-tg

# Regla 3: Carrito
IF path is /api/carrito*
THEN forward to appmoviles-carrito-tg

# Regla 4: Pedidos
IF path is /api/pedidos*
THEN forward to appmoviles-pedidos-tg

# Regla 5: Pagos
IF path is /api/pagos*
THEN forward to appmoviles-pagos-tg

# Regla 6: Logs
IF path is /api/logs*
THEN forward to appmoviles-logs-tg

# Regla 7: Admin
IF path is /api/admin*
THEN forward to appmoviles-admin-tg

# Regla 8: Reseñas
IF path is /api/resenas*
THEN forward to appmoviles-resenas-tg
```

### 4.5. Obtener URL del ALB

```powershell
aws elbv2 describe-load-balancers --names appmoviles-alb --query 'LoadBalancers[0].DNSName' --output text
```

**Salida:** `appmoviles-alb-123456789.us-east-1.elb.amazonaws.com`

---

## 🌐 PASO 5: Desplegar Frontend en S3 + CloudFront

### 5.1. Actualizar URLs del Frontend

Editar archivo de configuración del frontend (React/Vue/Angular):

```javascript
// src/config/api.js o similar
export const API_BASE_URL = 'http://appmoviles-alb-123456789.us-east-1.elb.amazonaws.com';

export const API_ENDPOINTS = {
  usuarios: `${API_BASE_URL}/api/usuarios`,
  auth: `${API_BASE_URL}/api/usuarios/auth`,
  productos: `${API_BASE_URL}/api/productos`,
  carrito: `${API_BASE_URL}/api/carrito`,
  pedidos: `${API_BASE_URL}/api/pedidos`,
  pagos: `${API_BASE_URL}/api/pagos`,
  resenas: `${API_BASE_URL}/api/resenas`,
  logs: `${API_BASE_URL}/api/logs`,
  admin: `${API_BASE_URL}/api/admin`,
};
```

### 5.2. Build del Frontend

```bash
# Para React/Vite
npm run build
# Genera carpeta: dist/

# Para React (Create React App)
npm run build
# Genera carpeta: build/

# Para Angular
ng build --prod
# Genera carpeta: dist/
```

### 5.3. Crear Bucket S3

```powershell
# Crear bucket (nombre debe ser único globalmente)
$bucketName = "appmoviles-frontend-$(Get-Date -Format 'yyyyMMdd')"
aws s3 mb "s3://$bucketName" --region us-east-1

# Configurar para hosting estático
aws s3 website "s3://$bucketName" `
  --index-document index.html `
  --error-document index.html

# Configurar política pública
$policy = @"
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "PublicReadGetObject",
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::$bucketName/*"
  }]
}
"@

$policy | Out-File -FilePath policy.json -Encoding UTF8
aws s3api put-bucket-policy --bucket $bucketName --policy file://policy.json

# Subir archivos
aws s3 sync ./dist "s3://$bucketName" --acl public-read

# Obtener URL
Write-Host "Frontend URL: http://$bucketName.s3-website-us-east-1.amazonaws.com"
```

### 5.4. Crear CloudFront Distribution (Opcional - para HTTPS y CDN)

1. **CloudFront → Create Distribution**
   - Origin domain: `appmoviles-frontend-20251127.s3.amazonaws.com`
   - Origin access: Public
   - Viewer protocol policy: Redirect HTTP to HTTPS
   - Allowed HTTP methods: GET, HEAD, OPTIONS
   - Cache policy: CachingOptimized

2. **Error pages** (para SPA routing)
   - Custom error response: 403 → /index.html (200)
   - Custom error response: 404 → /index.html (200)

3. **Crear distribution** (toma 10-15 minutos)

4. **Obtener URL CloudFront**
   ```
   https://d1234567890abc.cloudfront.net
   ```

### 5.5. Actualizar CORS en Backend

```powershell
# Para cada microservicio, actualizar CORS_ORIGINS
eb setenv CORS_ORIGINS=https://d1234567890abc.cloudfront.net,http://localhost:5173

# O si tienes dominio propio
eb setenv CORS_ORIGINS=https://tudominio.com,https://www.tudominio.com
```

---

## 🔒 PASO 6: Configurar HTTPS con ACM (Opcional)

### 6.1. Solicitar Certificado SSL

1. **AWS Certificate Manager → Request certificate**
   - Certificate type: Public certificate
   - Fully qualified domain name: `tudominio.com`
   - Add another name: `*.tudominio.com`
   - Validation method: DNS validation

2. **Validar dominio**
   - Copiar CNAME records
   - Agregar en tu proveedor de dominio (GoDaddy, Namecheap, etc.)
   - Esperar validación (puede tomar minutos u horas)

### 6.2. Asociar Certificado a ALB

1. **EC2 → Load Balancers → appmoviles-alb**
2. **Listeners → Add listener**
   - Protocol: HTTPS
   - Port: 443
   - Default SSL certificate: Seleccionar certificado ACM
   - Default action: Forward to target group

### 6.3. Configurar Route 53 (si usas dominio)

1. **Route 53 → Hosted zones → Create hosted zone**
   - Domain name: `tudominio.com`

2. **Create record**
   - Record name: `api` (para api.tudominio.com)
   - Record type: A
   - Alias: Yes
   - Route traffic to: ALB
   - Select: appmoviles-alb

3. **Create record** (frontend)
   - Record name: `www`
   - Record type: A
   - Alias: Yes
   - Route traffic to: CloudFront distribution

---

## 📊 PASO 7: Monitoreo y Mantenimiento

### 7.1. CloudWatch (Monitoreo automático)

```powershell
# Ver métricas de un servicio
aws cloudwatch get-metric-statistics `
  --namespace AWS/ElasticBeanstalk `
  --metric-name CPUUtilization `
  --dimensions Name=EnvironmentName,Value=appmoviles-usuarios-prod `
  --start-time 2025-11-27T00:00:00Z `
  --end-time 2025-11-27T23:59:59Z `
  --period 3600 `
  --statistics Average
```

### 7.2. Configurar Alarmas

1. **CloudWatch → Alarms → Create alarm**
2. Configurar alertas para:
   - CPU > 80%
   - Memoria > 80%
   - Health check failures
   - HTTP 5xx errors

### 7.3. Ver Logs

```powershell
# Ver logs de un servicio
eb logs -e appmoviles-usuarios-prod

# Ver logs en tiempo real
eb logs -e appmoviles-usuarios-prod --stream

# Logs de RDS
aws rds download-db-log-file-portion `
  --db-instance-identifier appmoviles-db `
  --log-file-name error/mysql-error.log
```

---

## 💰 ESTIMACIÓN DE COSTOS (Mensual)

### Opción 1: Free Tier (Primer año)
```
EC2 t3.micro (8 instancias × 750 hrs):  $0 (Free tier)
RDS db.t3.micro (750 hrs):              $0 (Free tier)
S3 (5 GB):                              $0 (Free tier)
CloudFront (50 GB):                     $0 (Free tier)
Application Load Balancer:              ~$22/mes
Total estimado:                         ~$22-25/mes
```

### Opción 2: Producción Pequeña
```
EC2 t3.small (8 instancias):            ~$120/mes
RDS db.t3.small:                        ~$30/mes
S3 + CloudFront (100 GB):               ~$10/mes
Application Load Balancer:              ~$22/mes
Total estimado:                         ~$180-200/mes
```

### Opción 3: Producción Media
```
EC2 t3.medium (8 instancias):           ~$240/mes
RDS db.t3.medium:                       ~$60/mes
S3 + CloudFront (500 GB):               ~$40/mes
Application Load Balancer:              ~$22/mes
Total estimado:                         ~$360-400/mes
```

---

## 🔧 Comandos Útiles

### Gestión de Environments
```powershell
# Listar todos los environments
eb list

# Ver estado de un environment
eb status -e appmoviles-usuarios-prod

# Actualizar configuración
eb config -e appmoviles-usuarios-prod

# Escalar instancias
eb scale 2 -e appmoviles-usuarios-prod

# Reiniciar
eb restart -e appmoviles-usuarios-prod

# Eliminar environment
eb terminate -e appmoviles-usuarios-prod
```

### Actualizar Aplicación
```powershell
# Después de cambios en el código
cd msvc-usuarios
.\mvnw.cmd clean package -Dmaven.test.skip=true
eb deploy -e appmoviles-usuarios-prod
```

### Ver Información
```powershell
# URL del environment
eb open -e appmoviles-usuarios-prod

# Eventos recientes
eb events -e appmoviles-usuarios-prod

# Health status
eb health -e appmoviles-usuarios-prod
```

---

## ✅ Checklist Final

### Backend
- [ ] Base de datos RDS creada y accesible
- [ ] 8 microservicios desplegados en Elastic Beanstalk
- [ ] Variables de entorno configuradas en cada servicio
- [ ] Health checks funcionando (`/actuator/health`)
- [ ] Application Load Balancer configurado (opcional)
- [ ] HTTPS configurado con ACM (opcional)

### Frontend
- [ ] URLs actualizadas para apuntar a AWS
- [ ] Build generado
- [ ] Archivos subidos a S3
- [ ] CloudFront distribution creada (opcional)
- [ ] CORS configurado en backend

### Seguridad
- [ ] Security groups configurados correctamente
- [ ] Passwords seguros y guardados
- [ ] IAM user con permisos mínimos necesarios
- [ ] Backup de RDS habilitado

### Monitoreo
- [ ] CloudWatch alarmas configuradas
- [ ] Logs accesibles
- [ ] Métricas revisadas

---

## 🆘 Troubleshooting Común

### Error: "Environment health degraded"
```powershell
# Ver logs detallados
eb logs -e [environment-name]

# Verificar health check
curl http://[url]/actuator/health

# Revisar security groups
aws ec2 describe-security-groups --group-ids [sg-id]
```

### Error: "Cannot connect to RDS"
```powershell
# Verificar security group de RDS permite conexiones
# Verificar endpoint es correcto
# Verificar usuario/contraseña

# Test de conexión
mysql -h appmoviles-db.xxx.rds.amazonaws.com -u admin -p appmoviles
```

### Error: "CORS blocked"
```powershell
# Actualizar CORS_ORIGINS en todos los microservicios
eb setenv CORS_ORIGINS=https://tu-cloudfront-url.com -e [environment-name]
```

### Alto costo inesperado
```powershell
# Ver facturación actual
aws ce get-cost-and-usage --time-period Start=2025-11-01,End=2025-11-27 --granularity MONTHLY --metrics "BlendedCost"

# Identificar servicios costosos
# EC2 → Running instances
# RDS → DB instances
# Load Balancers
```

---

## 🎉 ¡Listo!

Tu aplicación ahora está desplegada en AWS con:
- ✅ 8 microservicios funcionando
- ✅ Base de datos MySQL en la nube
- ✅ Frontend accesible globalmente
- ✅ Alta disponibilidad
- ✅ Escalabilidad automática
- ✅ Monitoreo incluido

**URLs de acceso:**
- Backend: `http://appmoviles-alb-xxx.elb.amazonaws.com/api/[servicio]`
- Frontend: `https://dxxxx.cloudfront.net` o `https://tudominio.com`
- Swagger: `http://[url]/swagger-ui.html`

---

## 📚 Recursos Adicionales

- [AWS Elastic Beanstalk Docs](https://docs.aws.amazon.com/elasticbeanstalk/)
- [AWS RDS Docs](https://docs.aws.amazon.com/rds/)
- [AWS S3 Docs](https://docs.aws.amazon.com/s3/)
- [AWS CloudFront Docs](https://docs.aws.amazon.com/cloudfront/)
- [AWS Pricing Calculator](https://calculator.aws/)
