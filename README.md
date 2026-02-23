# Challenge Intuit / Yappa - ABM Clientes

Este repositorio contiene la solución al challenge técnico, implementado con una arquitectura moderna separando el Backend (Java/Spring Boot) y el Frontend (Vue 3/Vite), e integrando todo con una base de datos PostgreSQL en Docker.

## Stack Tecnológico

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, PostgreSQL, JUnit 5 + Mockito, Swagger/OpenAPI.
- **Frontend:** Vue 3, TypeScript, Vite, Axios, CSS.
- **Base de Datos & Infraestructura:** PostgreSQL 15 orquestado con Docker Compose.

---

## Estructura del Proyecto

- `/backend`: API REST en Spring Boot.
- `/frontend`: SPA en Vue 3.
- `/database`: Scripts SQL de inicialización, incluyendo la estructura de la base de datos y un **Stored Procedure** requerido por el challenge.
- `docker-compose.yml`: Archivo para levantar la infraestructura de base de datos.

---

## Instrucciones para Ejecutar Localmente

### 1. Iniciar la Base de Datos

Es necesario tener **Docker** y **Docker Compose** instalados (por ejemplo, vía Docker Desktop).

Desde la raíz del proyecto, ejecuta:
```bash
docker-compose up -d
```
*Esto iniciará la base de datos PostgreSQL en el puerto 5432 y ejecutará automáticamente el script `schema.sql` (que contiene las tablas, datos de prueba y el Stored Procedure `search_clientes_by_name`).*

### 2. Iniciar el Backend (Spring Boot)

Asegúrate de tener **Java 17** y **Maven** instalados.

Navega a la carpeta `/backend` y ejecuta:
```bash
cd backend
mvn spring-boot:run
```
*La API estará disponible en `http://localhost:8080`.*

**Documentación Swagger:**
Puedes explorar y probar los endpoints desde: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**Testing:**
Para correr las pruebas unitarias y verificar la cobertura del código:
```bash
mvn test
```

### 3. Iniciar el Frontend (Vue 3)

Asegúrate de tener **Node.js** (v18 o superior) instalado.

1. Navega a la carpeta `/frontend`.
2. Crea un archivo `.env` copiando el contenido de `.env.example` (este paso es obligatorio para definir la URL de conexión a la API, ya que el archivo `.env` se excluye del control de versiones).
3. Instala las dependencias y levanta el entorno:

```bash
cd frontend
npm install
npm run dev
```
*La aplicación web estará disponible en `http://localhost:5173`.*

---

## Puntos Desarrollados

- **API Clientes Completa:** Endpoints GET, GET by ID, SEARCH, POST, PUT, DELETE.
- **Stored Procedure:** Incorporado para realizar la búsqueda flexible por nombre.
- **Validaciones:** Unicidad de CUIT y Email controlados en capa de servicio. Formatos y campos obligatorios validados en capa web.
- **Manejo de Errores:** Centralizado globalmente usando `@ControllerAdvice`. Trazabilidad de logs mediante SLF4J.
- **Testing:** Pruebas unitarias de la lógica de negocio empleando JUnit 5 y Mockito.
- **Frontend SPA:** Aplicación web embebida para el consumo asíncrono de la API REST.
- **UI/UX:** Interfaz responsiva y moderna implementada con CSS nativo.
