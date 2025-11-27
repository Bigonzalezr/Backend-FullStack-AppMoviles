# ☁️ Despliegue Full-Stack en AWS

## 🏗️ Arquitectura en AWS

```
┌─────────────────────────────────────────────────────────────┐
│                        Internet                              │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
        ▼                                     ▼
┌───────────────┐                    ┌───────────────┐
│  CloudFront   │                    │   Route 53    │
│  (Frontend)   │                    │    (DNS)      │
└───────┬───────┘                    └───────────────┘
        │
        ▼
┌───────────────┐                    ┌───────────────┐
│   S3 Bucket   │                    │  API Gateway  │
│  React Build  │                    │  (opcional)   │
└───────────────┘                    └───────┬───────┘
                                             │
                                             ▼
                                ┌────────────────────────┐
                                │   Application Load     │
                                │      Balancer          │
                                └────────┬───────────────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ▼                    ▼                    ▼
            ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
            │   EC2 / ECS   │   │   EC2 / ECS   │   │   EC2 / ECS   │
            │ msvc-usuarios │   │ msvc-productos│   │  msvc-carrito │
            │  (Puerto 8008)│   │  (Puerto 8002)│   │  (Puerto 8003)│
            └───────┬───────┘   └───────┬───────┘   └───────┬───────┘
                    │                    │                    │
                    └────────────────────┼────────────────────┘
                                         ▼
                                ┌────────────────┐
                                │   RDS MySQL    │
                                │   (Base Datos) │
                                └────────────────┘
```

---

## 📦 Opción 1: AWS Elastic Beanstalk (Más Fácil)

### 🎯 Ventajas
- Despliegue automático
- Escalado automático
- Balanceo de carga incluido
- Fácil de configurar

### 📝 Pasos

#### 1. Preparar el Backend (Spring Boot)

**Crear archivo `application-prod.properties` en cada microservicio:**

```properties
# msvc-usuarios/src/main/resources/application-prod.properties
spring.application.name=msvc-usuarios
server.port=5000

# Base de datos RDS MySQL
spring.datasource.url=jdbc:mysql://${RDS_HOSTNAME}:${RDS_PORT}/${RDS_DB_NAME}
spring.datasource.username=${RDS_USERNAME}
spring.datasource.password=${RDS_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# CORS - Permitir frontend en S3/CloudFront
cors.allowed.origins=${CORS_ORIGINS}

logging.level.root=INFO
```

**Agregar dependencia MySQL en `pom.xml`:**

```xml
<!-- En cada microservicio -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Crear archivo `.ebextensions/01-environment.config`:**

```yaml
# .ebextensions/01-environment.config
option_settings:
  aws:elasticbeanstalk:application:environment:
    SERVER_PORT: 5000
    SPRING_PROFILES_ACTIVE: prod
```

**Empaquetar cada microservicio:**

```powershell
# En cada carpeta de microservicio
cd msvc-usuarios
.\mvnw.cmd clean package -DskipTests
# Esto genera: target/msvc-usuarios-0.0.1-SNAPSHOT.jar

cd ..\msvc-productos
.\mvnw.cmd clean package -DskipTests

cd ..\msvc-carrito
.\mvnw.cmd clean package -DskipTests
```

#### 2. Crear Base de Datos RDS MySQL

```bash
# AWS Console → RDS → Create database
Tipo: MySQL 8.0
Template: Free tier (para empezar)
DB Instance Identifier: appmoviles-db
Master username: admin
Master password: [tu-password-seguro]
DB name: appmoviles

# Security Group: Permitir puerto 3306 desde Elastic Beanstalk
```

#### 3. Desplegar Backend en Elastic Beanstalk

**Usando AWS CLI:**

```powershell
# Instalar AWS CLI
# https://aws.amazon.com/cli/

# Configurar credenciales
aws configure

# Instalar EB CLI
pip install awsebcli

# Inicializar Elastic Beanstalk
cd msvc-usuarios
eb init -p corretto-21 appmoviles-usuarios --region us-east-1

# Crear environment
eb create appmoviles-usuarios-env

# Configurar variables de entorno
eb setenv `
  RDS_HOSTNAME=appmoviles-db.xxxxxx.us-east-1.rds.amazonaws.com `
  RDS_PORT=3306 `
  RDS_DB_NAME=appmoviles `
  RDS_USERNAME=admin `
  RDS_PASSWORD=tu-password `
  JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm `
  CORS_ORIGINS=https://tu-dominio.cloudfront.net,https://tu-dominio.com

# Desplegar
eb deploy

# Ver logs
eb logs

# Obtener URL
eb status
```

**Repetir para cada microservicio:**

```powershell
# msvc-productos
cd ..\msvc-productos
eb init -p corretto-21 appmoviles-productos --region us-east-1
eb create appmoviles-productos-env
eb setenv [mismas variables]
eb deploy

# msvc-carrito
cd ..\msvc-carrito
eb init -p corretto-21 appmoviles-carrito --region us-east-1
eb create appmoviles-carrito-env
eb setenv [mismas variables]
eb deploy
```

#### 4. Configurar Application Load Balancer

**Crear ALB que apunte a los Elastic Beanstalk environments:**

```yaml
# AWS Console → EC2 → Load Balancers → Create Load Balancer
Type: Application Load Balancer
Name: appmoviles-alb
Scheme: Internet-facing
Listeners: HTTP (80), HTTPS (443)

# Target Groups:
/api/usuarios/*    → appmoviles-usuarios-env
/api/productos/*   → appmoviles-productos-env
/api/carrito/*     → appmoviles-carrito-env
/api/pedidos/*     → appmoviles-pedidos-env
```

#### 5. Desplegar Frontend en S3 + CloudFront

**Actualizar configuración del frontend:**

```javascript
// src/config/api.js
export const API_URLS = {
  USUARIOS: 'https://api.tu-dominio.com/api/usuarios',
  AUTH: 'https://api.tu-dominio.com/api/auth',
  PRODUCTOS: 'https://api.tu-dominio.com/api/productos',
  CARRITO: 'https://api.tu-dominio.com/api/carrito',
  PEDIDOS: 'https://api.tu-dominio.com/api/pedidos',
  PAGOS: 'https://api.tu-dominio.com/api/pagos',
  RESENAS: 'https://api.tu-dominio.com/api/resenas',
  LOGS: 'https://api.tu-dominio.com/api/logs',
  ADMIN: 'https://api.tu-dominio.com/api/admin',
};
```

**Build del frontend:**

```bash
cd C:\Users\PC01\OneDrive\Desktop\ReactApp-Fullstack-II-1

# Build para producción
npm run build
# Esto crea la carpeta 'dist' con los archivos estáticos
```

**Crear bucket S3:**

```bash
# Crear bucket
aws s3 mb s3://appmoviles-frontend --region us-east-1

# Habilitar hosting estático
aws s3 website s3://appmoviles-frontend --index-document index.html --error-document index.html

# Subir archivos
cd dist
aws s3 sync . s3://appmoviles-frontend --acl public-read

# Configurar política del bucket
aws s3api put-bucket-policy --bucket appmoviles-frontend --policy '{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "PublicReadGetObject",
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::appmoviles-frontend/*"
  }]
}'
```

**Configurar CloudFront:**

```yaml
# AWS Console → CloudFront → Create Distribution
Origin Domain: appmoviles-frontend.s3.amazonaws.com
Origin Path: /
Viewer Protocol Policy: Redirect HTTP to HTTPS
Allowed HTTP Methods: GET, HEAD, OPTIONS
Compress Objects: Yes
Price Class: Use All Edge Locations
Alternate Domain Names (CNAMEs): www.tu-dominio.com
SSL Certificate: Request ACM certificate

# Behaviors:
Default (*): Origin → S3 bucket
/api/*: Origin → ALB (backend)
```

#### 6. Configurar Route 53 (DNS)

```yaml
# AWS Console → Route 53 → Hosted Zones → Create Record
Name: www.tu-dominio.com
Type: A - IPv4 address
Alias: Yes
Alias Target: [Tu CloudFront distribution]

Name: api.tu-dominio.com
Type: A - IPv4 address
Alias: Yes
Alias Target: [Tu Application Load Balancer]
```

---

## 📦 Opción 2: AWS ECS (Elastic Container Service) con Docker

### 🎯 Ventajas
- Más control
- Mejor para microservicios
- Escalado granular
- CI/CD más fácil

### 📝 Pasos

#### 1. Crear Dockerfiles para cada microservicio

**`msvc-usuarios/Dockerfile`:**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8008
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Crear imágenes Docker:**

```powershell
# En cada microservicio
cd msvc-usuarios
docker build -t appmoviles-usuarios:latest .

cd ..\msvc-productos
docker build -t appmoviles-productos:latest .

cd ..\msvc-carrito
docker build -t appmoviles-carrito:latest .
```

#### 2. Subir a Amazon ECR (Elastic Container Registry)

```bash
# Crear repositorios en ECR
aws ecr create-repository --repository-name appmoviles-usuarios --region us-east-1
aws ecr create-repository --repository-name appmoviles-productos --region us-east-1
aws ecr create-repository --repository-name appmoviles-carrito --region us-east-1

# Autenticar Docker con ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin [ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com

# Tag y push
docker tag appmoviles-usuarios:latest [ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com/appmoviles-usuarios:latest
docker push [ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com/appmoviles-usuarios:latest

docker tag appmoviles-productos:latest [ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com/appmoviles-productos:latest
docker push [ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com/appmoviles-productos:latest
```

#### 3. Crear Cluster ECS

```yaml
# AWS Console → ECS → Create Cluster
Cluster name: appmoviles-cluster
Infrastructure: AWS Fargate (serverless)
```

#### 4. Crear Task Definitions

**Para cada microservicio:**

```json
{
  "family": "appmoviles-usuarios-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "usuarios-container",
      "image": "[ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com/appmoviles-usuarios:latest",
      "portMappings": [
        {
          "containerPort": 8008,
          "protocol": "tcp"
        }
      ],
      "environment": [
        { "name": "SPRING_PROFILES_ACTIVE", "value": "prod" },
        { "name": "RDS_HOSTNAME", "value": "appmoviles-db.xxx.rds.amazonaws.com" },
        { "name": "RDS_PORT", "value": "3306" },
        { "name": "RDS_DB_NAME", "value": "appmoviles" },
        { "name": "RDS_USERNAME", "value": "admin" },
        { "name": "RDS_PASSWORD", "value": "tu-password" },
        { "name": "JWT_SECRET", "value": "tu-secret-jwt" }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/appmoviles-usuarios",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

#### 5. Crear Services ECS

```yaml
# Para cada microservicio
Service name: appmoviles-usuarios-service
Launch type: Fargate
Task Definition: appmoviles-usuarios-task
Desired tasks: 2 (para alta disponibilidad)
Load balancer: Application Load Balancer
Target group: Create new (usuarios-tg)
Health check path: /actuator/health
```

#### 6. Configurar Auto Scaling

```yaml
# Para cada servicio ECS
Minimum tasks: 2
Maximum tasks: 10
Target metric: CPU utilization
Target value: 70%
```

---

## 📦 Opción 3: AWS Lambda + API Gateway (Serverless)

### 🎯 Ventajas
- Sin servidores que administrar
- Pago por uso (muy económico)
- Escalado automático infinito

### Desventajas
- Cold start (latencia inicial)
- Límite de 15 minutos por ejecución
- Más complejo para Spring Boot

**No recomendado para Spring Boot tradicional. Mejor usar Spring Cloud Function.**

---

## 💰 Estimación de Costos AWS (Mensual)

### Opción 1: Elastic Beanstalk
```
EC2 t3.small (3 instancias):    ~$45
RDS db.t3.micro:                ~$15
S3 (5GB):                       ~$0.12
CloudFront (100GB):             ~$8.50
Route 53:                       ~$0.50
Application Load Balancer:      ~$22
Total estimado:                 ~$91/mes
```

### Opción 2: ECS Fargate
```
Fargate (3 tasks, 0.25 vCPU):   ~$25
RDS db.t3.micro:                ~$15
S3 + CloudFront:                ~$8.62
ALB:                            ~$22
Total estimado:                 ~$71/mes
```

### Opción 3: Free Tier (Primer año)
```
EC2 t2.micro (750 hrs/mes):     Gratis
RDS db.t2.micro (750 hrs/mes):  Gratis
S3 (5GB):                       Gratis
CloudFront (50GB):              Gratis
Total:                          ~$0-5/mes
```

---

## 🔒 Seguridad

### 1. Usar AWS Secrets Manager

```bash
# Crear secreto para DB
aws secretsmanager create-secret \
  --name appmoviles/db/credentials \
  --secret-string '{"username":"admin","password":"tu-password"}'

# Crear secreto para JWT
aws secretsmanager create-secret \
  --name appmoviles/jwt/secret \
  --secret-string 'mySecretKeyForJWT...'
```

**Actualizar application-prod.properties:**

```properties
spring.datasource.username=${sm://appmoviles/db/credentials#username}
spring.datasource.password=${sm://appmoviles/db/credentials#password}
jwt.secret=${sm://appmoviles/jwt/secret}
```

### 2. Configurar Security Groups

```yaml
# RDS Security Group
Inbound: MySQL (3306) desde EC2/ECS Security Group

# EC2/ECS Security Group
Inbound: HTTP (80), HTTPS (443) desde ALB
Outbound: All traffic

# ALB Security Group
Inbound: HTTP (80), HTTPS (443) desde 0.0.0.0/0
```

### 3. Habilitar HTTPS

```bash
# Solicitar certificado SSL en ACM
aws acm request-certificate \
  --domain-name tu-dominio.com \
  --subject-alternative-names www.tu-dominio.com api.tu-dominio.com \
  --validation-method DNS

# Configurar en ALB y CloudFront
```

---

## 🚀 CI/CD con GitHub Actions

**`.github/workflows/deploy-backend.yml`:**

```yaml
name: Deploy Backend to AWS

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: |
        cd msvc-usuarios
        ./mvnw clean package -DskipTests
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1
    
    - name: Deploy to Elastic Beanstalk
      run: |
        cd msvc-usuarios
        eb deploy appmoviles-usuarios-env
```

**`.github/workflows/deploy-frontend.yml`:**

```yaml
name: Deploy Frontend to S3

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    
    - name: Install dependencies
      run: npm install
    
    - name: Build
      run: npm run build
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1
    
    - name: Deploy to S3
      run: aws s3 sync dist/ s3://appmoviles-frontend --delete
    
    - name: Invalidate CloudFront
      run: |
        aws cloudfront create-invalidation \
          --distribution-id ${{ secrets.CLOUDFRONT_DISTRIBUTION_ID }} \
          --paths "/*"
```

---

## 📝 Checklist de Despliegue

### Backend
- [ ] Dependencia MySQL agregada a cada microservicio
- [ ] `application-prod.properties` creado
- [ ] Base de datos RDS MySQL creada
- [ ] Elastic Beanstalk environments creados
- [ ] Variables de entorno configuradas
- [ ] Application Load Balancer configurado
- [ ] Security Groups configurados
- [ ] Health checks funcionando

### Frontend
- [ ] URLs de API actualizadas para producción
- [ ] Build de producción creado (`npm run build`)
- [ ] Bucket S3 creado y configurado
- [ ] Archivos subidos a S3
- [ ] CloudFront distribution creada
- [ ] Certificado SSL configurado
- [ ] Route 53 configurado

### Seguridad
- [ ] HTTPS habilitado
- [ ] Secrets Manager configurado
- [ ] CORS configurado correctamente
- [ ] Security Groups restrictivos
- [ ] IAM roles con permisos mínimos

---

## 🔧 Comandos Útiles

```bash
# Ver logs de Elastic Beanstalk
eb logs

# SSH a instancia EC2
eb ssh

# Ver estado
eb status

# Actualizar variables de entorno
eb setenv KEY=VALUE

# Ver events
eb events

# Escalar instancias
eb scale 3

# Reiniciar
eb restart
```

---

## 📚 Recursos

- [AWS Elastic Beanstalk](https://docs.aws.amazon.com/elasticbeanstalk/)
- [AWS ECS](https://docs.aws.amazon.com/ecs/)
- [AWS RDS](https://docs.aws.amazon.com/rds/)
- [AWS S3](https://docs.aws.amazon.com/s3/)
- [AWS CloudFront](https://docs.aws.amazon.com/cloudfront/)

