# Smart Campus API - Coursework Project

University of Westminster **5COSC022W.2 Client-Server Architectures**

This repository contains a RESTful API for managing smart campus infrastructure, including rooms, sensors, and historical sensor readings. The project is built using JAX-RS (Jersey) and follows a clean, layered architecture without the use of external database frameworks or Spring Boot, as per the coursework specification.

## API Overview

The Smart Campus API provides a centralized management system for:
- **Rooms**: Creation, retrieval, and safe decommissioning of campus rooms.
- **Sensors**: Registration and filtering of smart sensors linked to specifically identified rooms.
- **Sensor Readings**: A historical logging system using the Sub-Resource Locator pattern to track activities over time.

**Core Technical Features:**
- **Versioned Entry Point**: All endpoints are prefixed with `/api/v1`.
- **HATEOAS Discovery**: A root discovery endpoint providing metadata and navigational links.
- **Data Integrity**: Business rules preventing orphaned sensors and enforcing maintenance states.
- **Observability**: Centralized request/response logging via JAX-RS filters.
- **Leak-Proof Error Handling**: Custom Exception Mappers to prevent sensitive stack trace exposure.

## Getting Started

### Prerequisites
- Java 8 or higher
- Apache Maven
- Apache Tomcat 9 (or a compatible JAX-RS container)

### Build Instructions
To compile the project and generate the deployment WAR file, run the following command from the project root:
```bash
mvn clean package
```
This will generate the file `target/smart-campus-api.war`.

### Launch Instructions (NetBeans)
1. Open NetBeans IDE.
2. Select **File > Open Project** and navigate to this directory.
3. Right-click the project in the Projects pane and select **Run**.
4. If prompted, select **Apache Tomcat 9** as your server.
5. The API root will be accessible at: `http://localhost:8080/smart-campus-api/api/v1`

---

## Sample API Interactions (Curl)

Below are five sample commands demonstrating key interactions with the API.

### 1. API Discovery
```bash
curl -i http://localhost:8080/smart-campus-api/api/v1
```

### 2. Create a Room
```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"R-101\",\"name\":\"Main Lecture Hall\",\"capacity\":150}"
```

### 3. List Sensors (Filtered by Type)
```bash
curl -i "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

### 4. Append a Sensor Reading
```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S-CO2-01/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":415.5}"
```

### 5. Attempt Safe Room Deletion (Conflict Demo)
```bash
curl -i -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/R-101
```

---

## Conceptual Report (Coursework Questions)

### Part 1: Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this impacts state management.**

**A:** By default, JAX-RS resource classes are **request-scoped**. This means a new instance of the resource class is instantiated for every incoming HTTP request and destroyed once the response is sent. This architectural decision ensures that resource-level fields are not shared between concurrent requests, significantly simplifying thread safety for the controller logic. 

However, because the resource itself is short-lived, we cannot store application data (like rooms or sensors) within the resource class. To handle this, our implementation uses a **Singleton pattern** for the `InMemoryDataStore`. We utilize thread-safe data structures such as `ConcurrentHashMap` and `CopyOnWriteArrayList` to manage our maps and lists. This ensures that even though multiple resource instances are being created and destroyed, the underlying data remains persistent and protected against race conditions during concurrent updates.

**Q: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of HATEOAS? How does this benefit client developers?**

**A:** Hypermedia is the "Engine of Application State" (HATEOAS) because it allows the API to be self-descriptive. Instead of the client needing hard-coded knowledge of every URI in the system, the server provides links within the JSON responses that guide the client to relevant next steps. 

For client developers, this reduces dependency on static documentation. If an endpoint changes, the client can follow the provided links dynamically rather than breaking due to a hard-coded URL. It makes the API more discoverable and allows for a "crawlable" interface where a developer (or a Postman consumer) can start at the root and navigate naturally through the entire resource hierarchy.

### Part 2: Room Management

**Q: What are the implications of returning only IDs versus returning the full room objects in a list?**

**A:** Returning only **IDs** minimizes network bandwidth usage, which is beneficial for mobile devices or very large datasets. However, it forces the client to perform multiple subsequent "GET" requests to fetch the details of each room, increasing latency and server load. 

Returning **full objects** (as implemented here) increases the initial response size but allows the client to display a comprehensive list immediately without further round-trips. For a Smart Campus management interface, returning full objects provides a better user experience for typically small-to-medium collections, reducing complexity in client-side processing.

**Q: Is the DELETE operation idempotent in your implementation? Provide a justification.**

**A:** Yes, the `DELETE` operation is **idempotent**. An operation is idempotent if the side effect on the server is the same regardless of how many times it is called. 
- The first DELETE request successfully removes the room from the `InMemoryDataStore` (provided it has no sensors).
- If the client mistakenly sends the exact same request again, the system will return a `404 Not Found` error because the room is already gone. 

While the HTTP status code changes (from 200/204 to 404), the **state of the server** remains the same: the room no longer exists. No additional data is deleted or corrupted by the duplicate request, fulfilling the idempotency requirement.

### Part 3: Sensor Operations & Linking

**Q: Explain the technical consequences if a client attempts to send data in a format other than JSON (e.g., XML) despite the @Consumes annotation.**

**A:** The `@Consumes(MediaType.APPLICATION_JSON)` annotation acts as a strict filter. If a client attempts to send `application/xml` or `text/plain`, the JAX-RS runtime (Jersey) will immediately detect a "Media Type Mismatch." 

The runtime will automatically reject the request before it even reaches our business logic, returning an **HTTP 415 Unsupported Media Type** status code. In our implementation, we have a `WebApplicationExceptionMapper` that catches this and wraps it in a consistent JSON error body, ensuring the client receives a structured response explaining that the format was incorrect.

**Q: Contrast @QueryParam with Path Parameters for filtering. Why is the query parameter approach superior for searching collections?**

**A:** Path parameters (e.g., `/sensors/type/CO2`) imply a hierarchical, unique location for a resource. This approach is rigid; it suggests that "CO2" is a fixed part of the system's address space. 

In contrast, **Query Parameters** (e.g., `?type=CO2`) are designed for filtering and searching. They are optional and allow for flexible combinations (e.g., `?type=CO2&status=ACTIVE`). Using query parameters leaves the primary resource path (`/sensors`) clean and intuitive, following the RESTful principle that the path identifies the *what* (the collection) and the query identifies the *how* (the filter).

### Part 4: Deep Nesting with Sub-Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern compared to a massive controller class.**

**A:** The **Sub-Resource Locator** pattern promotes "Separation of Concerns." By delegating logical handling for readings to a dedicated `SensorReadingResource` class, we keep our `SensorResource` focused strictly on sensor-level operations. 

In a massive controller, managing paths like `sensors/{id}/readings`, `sensors/{id}/alerts`, and `sensors/{id}/maintenance/logs` would lead to "God Objects" that are difficult to debug, test, and maintain. Delegation makes the code more modular, allows for easier unit testing of specific domains, and improves scalability as new types of sub-resources are added to the system.

### Part 5: Error Handling, Mapping & Logging

**Q: Why is HTTP 422 considered more semantically accurate than 404 when a reference is missing inside a valid JSON payload?**

**A:** A **404 Not Found** status implies that the URI itself (the "address") does not exist. However, when a client POSTs to `/sensors`, they have found a valid endpoint. If the `roomId` inside their JSON body refers to a missing room, the request is "well-formed" (it is valid JSON) but "semantically incorrect" (the business relationship is impossible). **422 Unprocessable Entity** (or a 400 Bad Request) accurately communicates that the server understands the content type and the request is syntactically correct, but the data provided violates business logic.

**Q: Explain the cybersecurity risks of exposing internal Java stack traces to external consumers.**

**A:** Exposing stack traces is a significant information leakage risk. It provides an attacker with:
1. **Infrastructure Details**: Framework names and versions (e.g., Jersey 2.41, Tomcat 9).
2. **Internal Logic**: Class and method names that reveal how the application is structured.
3. **Internal Paths**: Physical filesystem paths where the application is deployed.
With this "insider knowledge," an attacker can search for known vulnerabilities in specific library versions or craft targeted exploits to bypass security checks revealed by the code structure.

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging?**

**A:** Using JAX-RS filters (implementing `ContainerRequestFilter` and `ContainerResponseFilter`) ensures **DRY (Don't Repeat Yourself)** code. Cross-cutting concerns are tasks that apply to every endpoint. 

If we manually inserted `Logger.info()` into every method, we would introduce code duplication and increase the risk of missing an endpoint. A filter centralizes this logic in one place, ensuring that **every** incoming request and **every** outgoing response is logged automatically, regardless of who developed the resource class or when it was added to the system.

---

## References

*   *Jersey (JAX-RS) Official Documentation*: [https://eclipse-ee4j.github.io/jersey/](https://eclipse-ee4j.github.io/jersey/)
*   *University of Westminster 5COSC022W.2 Lecture & Tutorial Material (2026).*
*   *RFC 4918 (Standard for HTTP 422 status code).*
