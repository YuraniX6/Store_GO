# Build and Run Guide - MarketplaceService

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL 12+
- Git

## Compilación

### Compilar el Proyecto

```bash
cd MarketplaceService
mvn clean install
```

### Compilar sin Ejecutar Tests

```bash
mvn clean install -DskipTests
```

### Compilar solo el JAR

```bash
mvn clean package
```

## Ejecución

### Desarrollo Local

#### 1. Configurar Base de Datos

```sql
CREATE DATABASE marketplace_db;
GRANT ALL PRIVILEGES ON DATABASE marketplace_db TO postgres;
```

#### 2. Configurar Variables de Entorno (Opcional)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/marketplace_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=MySecretKeyForJWTTokenGenerationAndValidationMySecretKeyForJWTTokenGenerationAndValidationMySecretKeyForJWTTokenGenerationAndValidation
```

#### 3. Ejecutar con Maven

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

#### 4. Ejecutar el JAR Compilado

```bash
java -jar target/MarketplaceService-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Producción

```bash
java -jar target/MarketplaceService-0.0.1-SNAPSHOT.jar
```

### Con Docker

#### Compilar la Imagen

```bash
docker build -t marketplace-service:latest .
```

#### Ejecutar el Contenedor

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/marketplace_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  --name marketplace-service \
  marketplace-service:latest
```

### Con Docker Compose

```bash
docker-compose -f docker-compose.yml up
```

## Verificación

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### API Docs

```
http://localhost:8080/v3/api-docs
```

### Crear una Publicación (Ejemplo)

```bash
# 1. Obtener un token JWT (debe ser obtenido de AuthService)
TOKEN="your_jwt_token_here"

# 2. Crear publicación
curl -X POST http://localhost:8080/marketplace/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "skinId": "skin_001",
    "nombreSkin": "Dragon Scale",
    "descripcion": "Skin especial",
    "precio": 49.99,
    "nombreUsuario": "player123"
  }'
```

## Pruebas

### Ejecutar Todas las Pruebas

```bash
mvn test
```

### Ejecutar Pruebas Específicas

```bash
mvn test -Dtest=MarketplaceServiceImplTest
```

### Ejecutar con Cobertura

```bash
mvn test jacoco:report
```

## Troubleshooting

### Error: "Connection refused" a Base de Datos

- Verificar que PostgreSQL está corriendo
- Verificar las credenciales en application.properties
- Verificar que la base de datos existe

### Error: "Port 8080 already in use"

```bash
# Cambiar puerto en application.properties
server.port=8081

# O matar el proceso en Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Error: "JWT signature does not match"

- Verificar que el secret en application.properties coincide con el generador del token
- Verificar que el token no haya expirado

### Logs del Servicio

```bash
# Ver logs en tiempo real
tail -f logs/marketplace-service.log

# Ver últimas 100 líneas
tail -100 logs/marketplace-service.log
```

## Control de Versiones

### Versión Actual

- MarketplaceService: 0.0.1-SNAPSHOT

### Cambiar Versión

```bash
mvn versions:set -DnewVersion=1.0.0
mvn versions:commit
```

## Deployment

### Build para Producción

```bash
mvn clean install -P prod
```

### Generar WAR para Servidor de Aplicaciones

```bash
# Cambiar en pom.xml <packaging>jar</packaging> a <packaging>war</packaging>
mvn clean package -DskipTests
```

## Métricas y Monitoreo

### Exponer Endpoints de Actuator

Agregar a application.properties:
```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=always
```

### Acceder a Métricas

```
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/health
```

## Notas Importantes

1. **Seguridad**: Nunca guardar secretos en control de versiones
2. **Logs**: Revisar logs regularmente para detectar problemas
3. **Paginación**: Siempre usar paginación para queries grandes
4. **JWT**: Validar tokens en cada solicitud protegida
5. **CORS**: Configurado para localhost en desarrollo

## Soporte Rápido

Para más información, consultar:
- IMPLEMENTATION.md - Documentación técnica completa
- README.md - Descripción general del proyecto
- API Reference - Detalles de endpoints
