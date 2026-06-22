# Store GO — Plataforma de Marketplace de Skins CS2
Proyecto de Arquitectura de Microservicios/Store GO — DSY1103 Desarrollo FullStack 1

## Integrantes del equipo
- Ignacio Vilches
- Willian Zela
- Felix Santillan
- Javier Mendez

## Descripción del dominio

Store GO es una plataforma de compra, venta de skins del juego Counter-Strike 2.
Los usuarios pueden registrarse, gestionar su perfil, publicar y vender skins en el marketplace,
agregarlas al carrito y calificarlas. El sistema está construido sobre una arquitectura de
microservicios independientes que se comunican a través de un API Gateway centralizado.

## Microservicios implementados

| Servicio           | Puerto | Descripción                                              |
|--------------------|--------|----------------------------------------------------------|
| EurekaServer       | 8761   | Service discovery — registro y localización de servicios |
| Gateway            | 8765   | API Gateway — enrutamiento centralizado de peticiones    |
| AuthService        | 8080   | Autenticación y autorización con JWT                     |
| ProfileService     | 8081   | Gestión de perfiles de usuario                           |
| InventoryService   | 8082   | Inventario personal de skins por usuario                 |
| CatalogService     | 8083   | Catálogo público de skins disponibles                    |
| CartService        | 8084   | Carrito de compras                                       |
| MarketplaceService | 8089   | Publicaciones de venta de skins entre usuarios           |
| RatingService      | 8087   | Calificaciones y reseñas de skins                        |

## Rutas principales del Gateway (puerto 8765)

| Ruta Gateway         | Microservicio destino  |
|----------------------|------------------------|
| `/api/auth/**`       | AuthService            |
| `/api/profiles/**`   | ProfileService         |
| `/api/skins/**`      | InventoryService       |
| `/api/catalog/**`    | CatalogService         |
| `/api/cart/**`       | CartService            |
| `/api/marketplace/**`| MarketplaceService     |
| `/api/ratings/**`    | RatingService          |

## Documentación Swagger / OpenAPI

Con los servicios corriendo localmente:

- AuthService: http://localhost:8080/swagger-ui.html
- ProfileService: http://localhost:8081/swagger-ui.html
- InventoryService: http://localhost:8082/swagger-ui.html
- CatalogService: http://localhost:8083/swagger-ui.html
- CartService: http://localhost:8084/swagger-ui.html
- MarketplaceService: http://localhost:8089/swagger-ui.html
- RatingService: http://localhost:8087/swagger-ui.html


### Requisitos previos
- Java 17/21
- VS Code

### Ejecutar con Maven desde terminal
```bash
cd AuthService
./mvnw spring-boot:run
```

### Ejecutar pruebas unitarias
```bash
cd AuthService
./mvnw test
```