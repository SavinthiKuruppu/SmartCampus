# Smart Campus API

## Coursework Overview

The Smart Campus API is designed to manage campus infrastructure elements, specifically rooms and their associated sensors. The system provides a centralized interface for tracking real-time sensor data and maintaining historical readings.

**Key Architecture Features:**
*   **Versioned API Root**: `/api/v1` for future-proof development.
*   **Discovery Endpoint**: Self-describing system metadata.
*   **Hierarchical Resources**: Nested sensors and readings using RESTful patterns.
*   **State Management**: High-performance in-memory persistence.
*   **Reliability**: Robust error handling via structured JAX-RS Exception Mappers.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 8 compatible |
| Build System | Maven |
| Framework | JAX-RS (Jersey 2.41) |
| JSON Provider | Jackson |
| Deployment | Servlet-compliant (e.g., Apache Tomcat 9) |
| Persistence | In-memory Thread-safe collections |

## Project Structure

```text
smart-campus-api/
|-- pom.xml
|-- README.md
`-- src/
    `-- main/
        |-- java/
        |   `-- com/smartcampus/
        |       |-- config/ (App configuration)
        |       |-- exception/ (Custom mappers)
        |       |-- filter/ (Logging filters)
        |       |-- model/ (POJOs/Entities)
        |       |-- resource/ (API Endpoints)
        |       |-- service/ (Business logic)
        |       |-- store/ (Shared state)
        |       `-- util/ (API utilities)
        `-- webapp/
            `-- META-INF/
                `-- context.xml
```

## Getting Started

### Build
To compile the project and generate the deployment artifact:
```bash
mvn clean package
```
This produces `target/smart-campus-api.war`.

### Execution (NetBeans)
1. Open the project in NetBeans.
2. Ensure Apache Tomcat 9 is configured as the target server.
3. Right-click the project and select **Run**.
4. The API will be available at: `http://localhost:8080/smart-campus-api/api/v1`



## API Design Overview

### API Versioning

The API is versioned using the base path:
```text
/api/v1
```
Versioning ensures backward compatibility and allows the system to evolve without breaking existing client integrations.

### Discovery Endpoint

The API includes a discovery endpoint:
`GET /api/v1`

This serves as a dynamic entry point, providing metadata and navigational links to primary resources, adhering to HATEOAS principles.

## Core Management Features

### Room Management

The API supports full lifecycle management of smart campus rooms. Rooms are treated as independent resources with established relationships to sensors.
*   **Key Constraint**: A room cannot be deleted if it contains active sensors, ensuring data integrity.

### Sensor Management

Sensors are managed as standalone resources logically associated with rooms.
*   **Filtering**: Supports optional filtering by sensor type (e.g., `CO2`, `Temperature`) via query parameters.
*   **Validation**: Sensor creation requires a valid, existing `roomId`.

### Sensor Readings (Sub-Resource Design)

Sensor readings use a hierarchical resource structure, reflecting their dependency on parent sensors.
*   **Sub-Resource Locator**: Uses a dedicated resource class for readings to maintain clean separation of concerns.
*   **Automatic Updates**: Posting a new reading automatically updates the parent sensor's `currentValue`.
*   **Maintenance Guard**: Sensors in a `MAINTENANCE` state will reject new readings with a `403 Forbidden` response.

## Error Handling & Reliability

### Structured JSON Errors

All errors follow a consistent JSON schema, including a timestamp, status code, error type, and descriptive message. This prevents the exposure of internal stack traces, improving both security and client usability.

### Request & Response Logging

Centralized logging is implemented via JAX-RS filters, capturing the HTTP method, URI, and status code for every interaction to support auditing and monitoring.

## Coursework Question Answers

### Part 1: Lifecycle & State

**JAX-RS resource lifecycle and thread safety**

JAX-RS resource classes are request-scoped. A new instance is created for each request, preventing thread-safety issues at the resource level. Shared state (rooms/sensors) is managed in a thread-safe `InMemoryDataStore` using `ConcurrentHashMap` and `CopyOnWriteArrayList`, while service layers synchronize critical multi-step operations.

**API Discoverability (HATEOAS)**

Discoverability is implemented via the `GET /api/v1` endpoint. It provides metadata and hypermedia links that allow clients to navigate the API dynamically without relying solely on static documentation.

### Part 2: Resource Design & Idempotency

**Returning Objects vs IDs**

This API returns full room objects instead of just IDs. While returning IDs saves bandwidth, returning full objects reduces the number of round-trips a client must make to display useful information, which is more efficient for the expected campus management use cases.

**DELETE Idempotency**

The `DELETE /rooms/{id}` operation is idempotent. The first successful request removes the room. Subsequent identical requests return a `404 Not Found` because the resource is gone, but the final state of the server (the room being absent) remains identical.

### Part 3: Protocol & Validation

**`@Consumes` and 415 Status**

Methods are restricted to `application/json`. If a client sends an incorrect `Content-Type`, the API returns `415 Unsupported Media Type` via a custom mapper that ensures the error is returned as structured JSON rather than a default HTML page.

**Query Parameters for Filtering**

Query parameters (`?type=CO2`) are used for filtering collections because they are optional and do not uniquely identify a single resource. Path parameters are reserved for resource identification (`/sensors/{id}`).

**Why 422 for Linked Resources**

`422 Unprocessable Entity` is used when a JSON payload is syntactically correct but semantically invalid (e.g., a missing `roomId`). This is more precise than `404`, as the endpoint itself was found, but the data within the request could not be processed.

### Part 4: Patterns & Logic

**Sub-Resource Locator Pattern**

The API uses `SensorReadingResource` as a sub-resource of `SensorResource`. This pattern improves modularity by delegating reading-specific logic to a dedicated class, keeping the parent sensor logic clean and maintainable.

**Current Value Synchronization**

When a reading is successfully persisted, the system immediately updates the `currentValue` field of the parent sensor. This ensures that any subsequent `GET` request for that sensor reflects the most recent data point.

### Part 5: Error Strategy & Filtering

**Custom Exception Mapping**

The API avoids default framework error pages by using `ExceptionMapper` classes. Specific exceptions like `RoomNotEmptyException` or `SensorUnavailableException` are mapped to appropriate HTTP status codes (409 and 403) with structured JSON bodies.

**Cybersecurity and Stack Traces**

Exposing stack traces provides attackers with information about the system's internal logic, library versions, and file paths. This API suppresses stack traces in client responses, logging them only on the server, to mitigate information leakage risks.

**JAX-RS Filters**

Filters are used for logging instead of manual calls in every method. This centralizes cross-cutting concerns, ensuring that all requests and responses are logged consistently regardless of which endpoint is called.

## References

*   JAX-RS (Jersey) Documentation: [https://eclipse-ee4j.github.io/jersey/](https://eclipse-ee4j.github.io/jersey/)
*   University of Westminster, Client–Server Architectures (5COSC022W.2) Course Materials, 2026.
