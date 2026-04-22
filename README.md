# Smart Campus API Coursework Submission (w2153601)

This project is a Java EE 8 REST API built with JAX-RS (Jersey) and Maven (WAR packaging). It is structured to satisfy the Smart Campus coursework requirements, including discovery, room and sensor management, sub-resources, and robust exception handling.

## 1. Technology and Setup

- Java EE 8
- JAX-RS (Jersey)
- Maven
- WAR deployment
- In-memory data store (thread-safe maps and counters)

Project context path (from application config):

- `http://localhost:8080/smartCampus_w2153601`

API base path:

- `http://localhost:8080/smartCampus_w2153601/api/v1`

Note: depending on server/runtime settings, the deployed app URL may include a version suffix. Always copy the exact base URL from your server output and append `/api/v1`.

## 2. Run Instructions (NetBeans)

1. Open NetBeans.
2. Choose `File > Open Project` and select this folder.
3. Configure a Java EE 8 compatible server (GlassFish/Payara/Tomcat with Jersey setup).
4. Clean and Build.
5. Run project.
6. Verify API by opening: `http://localhost:8080/smartCampus_w2153601/api/v1`

## 3. Rubric Coverage Checklist

### Part 1: Setup and Discovery

- [x] JAX-RS bootstrapped with `@ApplicationPath("api/v1")`
- [x] Discovery endpoint `GET /api/v1`
- [x] Discovery JSON includes version/contact/resource links

### Part 2: Room Management

- [x] `GET /api/v1/rooms`
- [x] `POST /api/v1/rooms` returns `201 Created` + `Location` header
- [x] `GET /api/v1/rooms/{id}`
- [x] `DELETE /api/v1/rooms/{id}`
- [x] Prevents deleting rooms with linked sensors (`409 Conflict`)

### Part 3: Sensors and Filtering

- [x] `POST /api/v1/sensors` validates `roomId`
- [x] Non-existent room reference returns `422 Unprocessable Entity`
- [x] `GET /api/v1/sensors`
- [x] `GET /api/v1/sensors?type=...` optional filtering

### Part 4: Sub-Resources

- [x] Sub-resource locator: `/api/v1/sensors/{id}/readings`
- [x] `GET` reading history
- [x] `POST` new reading
- [x] Posting a reading updates parent sensor `currentValue`

### Part 5: Error Handling

- [x] Specific mapper for `409 Conflict`
- [x] Specific mapper for `422 Unprocessable Entity`
- [x] Specific mapper for `403 Forbidden`
- [x] Global `ExceptionMapper<Throwable>` for safe `500`
- [x] Structured JSON error body with timestamp/status/error/message/path

## 4. Endpoint Summary

### 4.1 Discovery

- `GET /api/v1`

Returns API metadata and navigable resource links.

### 4.2 Rooms

- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`

Create room example:

```json
{
  "name": "Lab A",
  "building": "Engineering",
  "floor": 2
}
```

### 4.3 Sensors

- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=temperature`
- `POST /api/v1/sensors`
- `GET /api/v1/sensors/{sensorId}`

Create sensor example:

```json
{
  "roomId": 1,
  "name": "Temp Sensor 1",
  "type": "temperature",
  "unit": "C",
  "currentValue": 22.1
}
```

Validation behavior:

- invalid `roomId` -> `422 Unprocessable Entity`
- restricted type (`restricted`) -> `403 Forbidden`

### 4.4 Sensor Readings (Sub-Resource)

- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

Reading example:

```json
{
  "value": 24.7,
  "timestamp": "2026-04-22T10:45:00Z"
}
```

POST result:

- `201 Created`
- reading added to history
- parent sensor `currentValue` updated to reading value

### 4.5 Global Mapper Demonstration

- `GET /api/v1/simulate-error`

Expected:

- `500 Internal Server Error`
- clean JSON response
- no stack trace leakage

## 5. Standard Error Response Format

All custom and global errors return JSON in this structure:

```json
{
  "timestamp": "2026-04-22T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId 99 does not exist.",
  "path": "/api/v1/sensors"
}
```

## 6. Coursework Questions and Answers

### Q1. Explain JAX-RS lifecycle and synchronization strategy in this API.

JAX-RS resource classes are request-scoped by default, so each HTTP request receives a fresh resource instance. Shared data is therefore held in a singleton in-memory store. To preserve thread safety, the store uses `ConcurrentHashMap` and `AtomicLong`. For reading updates, synchronization is applied on each sensor object to prevent race conditions while appending to reading history and updating `currentValue`.

### Q2. What is the value of HATEOAS in your discovery endpoint?

HATEOAS allows clients to navigate the API through runtime links rather than hardcoded assumptions. The discovery endpoint returns resource links and supported methods, improving self-documentation and reducing coupling between client and server.

### Q3. Compare returning IDs only vs full objects.

ID-only responses reduce payload size but force extra client round-trips for details. Full-object responses increase payload size but reduce follow-up calls and improve client usability. This API returns full objects on create operations so clients immediately receive the created entity and server-assigned ID.

### Q4. Is DELETE idempotent in this design?

Yes. Repeating `DELETE /rooms/{id}` for an already-deleted room keeps state unchanged. If business constraints are not satisfied (room still has sensors), repeated calls consistently return `409` until linked sensors are removed.

### Q5. What is the role of `@Consumes` and how does 415 occur?

`@Consumes(MediaType.APPLICATION_JSON)` declares that the endpoint accepts JSON payloads. If a client sends an unsupported media type, JAX-RS can reject the request with `415 Unsupported Media Type`, enforcing API contract correctness before business logic executes.

### Q6. Why use QueryParam instead of PathParam for filtering?

`PathParam` identifies a specific resource instance, while `QueryParam` modifies collection retrieval. Filtering by sensor type is optional search behavior on a collection, so query parameters are the correct REST design choice.

### Q7. Why use a sub-resource locator for readings?

The sub-resource locator delegates `/sensors/{id}/readings` to a dedicated `SensorReadingResource`, separating reading-history behavior from top-level sensor operations. This improves modularity, readability, and maintainability as the API grows.

### Q8. Why return 422 for invalid payload references instead of 404?

`422 Unprocessable Entity` is appropriate when the endpoint exists and JSON syntax is valid, but semantic validation fails (for example, `roomId` does not exist). `404 Not Found` is better reserved for missing URL resources.

### Q9. Why is exposing stack traces risky?

Raw stack traces can reveal internal package names, library details, and implementation behavior, which may help attackers profile the system. The global mapper avoids this by returning sanitized JSON for unexpected failures.

## 7. Suggested Marker Demonstration Order

1. `GET /api/v1` (show metadata and links)
2. `POST /api/v1/rooms` then show `201` + `Location`
3. `GET /api/v1/rooms/{id}` for created room
4. `POST /api/v1/sensors` with invalid `roomId` -> `422`
5. `POST /api/v1/sensors` with valid `roomId` -> `201`
6. `GET /api/v1/sensors?type=temperature`
7. `GET /api/v1/sensors/{id}/readings`
8. `POST /api/v1/sensors/{id}/readings` then verify sensor `currentValue`
9. `DELETE /api/v1/rooms/{id}` with linked sensor -> `409`
10. `GET /api/v1/simulate-error` -> safe `500` response

## 8. Notes

- Data storage is in-memory and resets on restart.
- This is intentional for coursework focus on API behavior and error-handling design.
