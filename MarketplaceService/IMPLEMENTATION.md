# MarketplaceService - Documentación Técnica

## Descripción General

MarketplaceService es un microservicio que forma parte de la arquitectura de **Store_Go**. Proporciona funcionalidades de marketplace para que los usuarios puedan comprar y vender skins.

## Características Principales

- **Gestión de Publicaciones**: Crear, leer, actualizar y eliminar publicaciones de venta de skins
- **Autenticación JWT**: Seguridad mediante tokens JWT con integración con Spring Security
- **Validación de Inventario**: Comunicación con InventoryService usando WebClient para validar la propiedad de skins
- **Paginación**: Soporte completo para búsquedas paginadas
- **Documentación API**: Swagger/OpenAPI integrado con autenticación Bearer JWT
- **Manejo Global de Excepciones**: Respuestas de error consistentes con mensajes descriptivos
- **Registro de Eventos**: Logging con SLF4J/Logback

## Stack Tecnológico

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Acceso a base de datos
- **Hibernate** - ORM
- **Spring WebClient** - Comunicación inter-servicios
- **PostgreSQL** - Base de datos
- **JWT (JJWT)** - Tokens de autenticación
- **OpenAPI/Swagger** - Documentación de API
- **Lombok** - Reducción de código boilerplate
- **JUnit 5 & Mockito** - Pruebas unitarias
- **Maven** - Gestor de dependencias

## Estructura del Proyecto

```
MarketplaceService/
├── src/main/java/com/storego/MarketplaceService/
│   ├── client/                          # Clientes de servicios externos
│   │   └── InventoryClient.java        # Cliente para InventoryService con WebClient
│   ├── config/                          # Configuraciones de aplicación
│   │   └── OpenApiConfig.java          # Configuración de Swagger/OpenAPI
│   ├── controller/                      # Controladores REST
│   │   └── MarketplaceController.java  # Endpoints de marketplace
│   ├── dto/                             # Data Transfer Objects
│   │   ├── MarketplaceRequestDTO.java  # DTO para crear publicaciones
│   │   └── MarketplaceResponseDTO.java # DTO para respuestas
│   ├── entity/                          # Entidades JPA
│   │   ├── MarketplacePost.java        # Entidad principal
│   │   └── PublicationStatus.java      # Enum de estados
│   ├── exception/                       # Excepciones personalizadas
│   │   ├── GlobalExceptionHandler.java # Manejador global de excepciones
│   │   ├── ErrorResponse.java          # Modelo de respuesta de error
│   │   ├── MarketplaceNotFoundException.java
│   │   ├── InventoryValidationException.java
│   │   ├── UnauthorizedException.java
│   │   └── InvalidJwtException.java
│   ├── repository/                      # Data Access Objects
│   │   └── MarketplacePostRepository.java # Queries personalizadas
│   ├── security/                        # Configuración de seguridad
│   │   ├── JwtUtil.java                # Utilidades para JWT
│   │   ├── JwtAuthFilter.java          # Filtro de autenticación
│   │   └── SecurityConfig.java         # Configuración de Spring Security
│   ├── service/                         # Lógica de negocio
│   │   ├── IMarketplaceService.java   # Interfaz del servicio
│   │   └── impl/
│   │       └── MarketplaceServiceImpl.java # Implementación
│   └── MarketplaceServiceApplication.java # Clase principal
├── src/test/java/com/storego/MarketplaceService/
│   └── service/
│       └── MarketplaceServiceImplTest.java # Pruebas unitarias
└── src/main/resources/
    ├── application.properties           # Configuración por defecto
    └── application-local.properties     # Configuración para desarrollo local
```

## Configuración

### application.properties (Producción)

```properties
spring.application.name=MarketplaceService
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/marketplace_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=YourSecureSecretKey...
jwt.issuer=storego

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# InventoryService
inventory.service.url=http://localhost:8082
```

### application-local.properties (Desarrollo)

Configuración similar pero con Eureka deshabilitado y más verbosidad en logs:
```
eureka.client.enabled=false
logging.level.com.storego=DEBUG
logging.level.org.springframework.security=DEBUG
```

## API Endpoints

### Crear Publicación
```
POST /marketplace/posts
Authorization: Bearer {token}
Content-Type: application/json

{
  "skinId": "skin_001",
  "nombreSkin": "Dragon Scale",
  "descripcion": "Skin exclusiva",
  "precio": 49.99,
  "nombreUsuario": "player123"
}

Response: 201 Created
{
  "id": 1,
  "ownerId": 123,
  "skinId": "skin_001",
  "nombreSkin": "Dragon Scale",
  "descripcion": "Skin exclusiva",
  "precio": 49.99,
  "publicacionEstado": "ACTIVE",
  "publicacionFecha": "2024-06-15T10:30:00",
  "nombreUsuario": "player123",
  "createdAt": "2024-06-15T10:30:00",
  "updatedAt": "2024-06-15T10:30:00"
}
```

### Listar Publicaciones
```
GET /marketplace/posts?page=0&size=20

Response: 200 OK
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### Obtener Publicación por ID
```
GET /marketplace/posts/{id}

Response: 200 OK
{...}
```

### Buscar por Skin
```
GET /marketplace/posts/weapon/{skinId}?page=0&size=20

Response: 200 OK
{...}
```

### Obtener Mis Publicaciones
```
GET /marketplace/my-posts?page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{...}
```

### Obtener Publicaciones por Estado
```
GET /marketplace/my-posts/{status}
Authorization: Bearer {token}

Response: 200 OK
[...]
```

### Actualizar Estado
```
PUT /marketplace/posts/{id}/status?status=SOLD
Authorization: Bearer {token}

Response: 200 OK
{...}
```

### Eliminar Publicación
```
DELETE /marketplace/posts/{id}
Authorization: Bearer {token}

Response: 204 No Content
```

### Obtener Skins Activas
```
GET /marketplace/skins/{skinId}/active

Response: 200 OK
[...]
```

## Estados de Publicación

- **ACTIVE**: Publicación disponible para venta
- **SOLD**: La skin ha sido vendida
- **CANCELLED**: La publicación fue cancelada

## Seguridad

### JWT Token

Los tokens JWT deben enviarse en el header `Authorization` con el formato:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Claims del Token

El token debe contener:
- `sub` (subject): Nombre de usuario
- `userId`: ID del usuario (Long)
- `roles`: Lista de roles del usuario

### Endpoints Públicos

- `GET /marketplace/posts`
- `GET /marketplace/posts/{id}`
- `GET /marketplace/posts/weapon/{skinId}`
- `GET /marketplace/posts/active`
- `GET /marketplace/skins/{skinId}/active`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### Endpoints Protegidos

Requieren token JWT válido:
- `POST /marketplace/posts`
- `PUT /marketplace/posts/{id}/status`
- `DELETE /marketplace/posts/{id}`
- `GET /marketplace/my-posts`
- `GET /marketplace/my-posts/{status}`

## Validación de Inventario

Cuando se crea una publicación, MarketplaceService realiza una llamada a InventoryService:

```
GET http://inventory-service:8082/inventory/validate/{ownerId}/{skinId}
Authorization: Bearer {userToken}
```

InventoryService debe responder:
- **true**: El usuario posee la skin
- **false**: El usuario no posee la skin
- **Error HTTP**: Problema al validar

## Manejo de Errores

### Formato de Respuesta de Error

```json
{
  "timestamp": "2024-06-15T10:30:00",
  "status": 400,
  "error": "Nombre del error",
  "message": "Descripción del error",
  "path": "/marketplace/posts",
  "validationErrors": {
    "fieldName": "Error de validación"
  }
}
```

### Códigos HTTP

- **200 OK**: Solicitud exitosa
- **201 Created**: Recurso creado exitosamente
- **204 No Content**: Solicitud exitosa sin contenido
- **400 Bad Request**: Datos inválidos
- **401 Unauthorized**: Token no válido
- **403 Forbidden**: Acceso denegado
- **404 Not Found**: Recurso no encontrado
- **500 Internal Server Error**: Error del servidor

## Pruebas Unitarias

Las pruebas se ejecutan con JUnit 5 y Mockito:

```bash
mvn test
```

Cobertura de pruebas:
- ✅ Crear publicación exitosamente
- ✅ Validación de inventario fallida
- ✅ Eliminación de publicación
- ✅ Autorización
- ✅ Búsqueda con paginación
- ✅ Actualización de estado
- ✅ Excepciones

## Ejecutar el Servicio

### Desarrollo Local

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

### Producción

```bash
mvn clean install
java -jar target/MarketplaceService-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
docker build -t marketplace-service:latest .
docker run -p 8080:8080 marketplace-service:latest
```

## Base de Datos

### Tabla: marketplace_posts

```sql
CREATE TABLE marketplace_posts (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    skin_id VARCHAR(255) NOT NULL,
    nombre_skin VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL,
    publicacion_estado VARCHAR(50) NOT NULL,
    publicacion_fecha TIMESTAMP NOT NULL,
    nombre_usuario VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## Logging

Los eventos se registran en:
- **Console**: Todas las aplicaciones
- **File**: `logs/marketplace-service.log`

Niveles de log:
- `ERROR`: Errores críticos
- `WARN`: Advertencias
- `INFO`: Información importante
- `DEBUG`: Detalles para desarrollo (com.storego)

## Integración con Otros Servicios

### InventoryService

**Método**: GET  
**URL**: `http://inventory-service:8082/inventory/validate/{ownerId}/{skinId}`  
**Headers**: `Authorization: Bearer {token}`  
**Respuesta**: Boolean

### API Gateway

El MarketplaceService se registra automáticamente en Eureka y puede ser accedido a través del API Gateway.

### AuthService

La autenticación se maneja a través de JWT generados por AuthService.

## Performance

- **Paginación**: Por defecto 20 items por página (máximo 100)
- **Índices de base de datos**: Recomendados en owner_id, skin_id, publicacion_estado
- **Caché**: Configurar Redis para publicaciones activas (opcional)

## Mejoras Futuras

- [ ] Implementar caché para publicaciones activas
- [ ] Agregar búsqueda full-text
- [ ] Implementar sistema de recomendaciones
- [ ] Agregar notificaciones en tiempo real
- [ ] Implementar sistema de calificaciones
- [ ] Agregar auditoría de cambios

## Soporte

Para reportar problemas o sugerencias, contactar al equipo de desarrollo de Store_Go.
