# Smart Campus API Documentation (w2153601)

## Section 1 — API Overview

The Smart Campus API is a robust, RESTful backend designed to facilitate the monitoring and management of university campus infrastructure. It provides a centralized system for facilities managers and automated building systems to track room layouts, deploy environmental sensors (such as temperature, humidity, and CO2), and continuously record telemetry data. By offering standardized REST endpoints, the API allows client applications to seamlessly integrate campus environmental data into dashboards, analytics tools, and automated HVAC controls.

The system comprises three primary resources: Room, Sensor, and SensorReading. Built on Java EE 8, the API leverages the JAX-RS (Jersey) framework and is managed using Maven. Currently, all data is stored in-memory using thread-safe data structures, meaning the state is highly performant but transient across server restarts. The base URL for interacting with the API is `http://localhost:8080/smartCampus_w2153601/api/v1`.

## Section 2 — Technology Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 8 |
| **Framework** | JAX-RS Jersey |
| **Build Tool** | Maven |
| **Server** | GlassFish or Payara |
| **Data Store** | In-memory ConcurrentHashMap / ArrayList |
| **Packaging** | WAR |


## Section 3 — Endpoint Reference

### Group 1 — Discovery
| Method | Endpoint | Description | Expected Response |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1` | Retrieves API metadata and available resource links (HATEOAS). | 200 OK |

### Group 2 — Rooms
| Method | Endpoint | Description | Expected Response |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/rooms` | Retrieves a list of all rooms. | 200 OK |
| POST | `/api/v1/rooms` | Creates a new room. | 201 Created |
| GET | `/api/v1/rooms/{roomId}`| Retrieves full details of a specific room by its ID. | 200 OK |
| DELETE| `/api/v1/rooms/{roomId}`| Deletes a specific room, provided it has no linked sensors. | 204 No Content |

### Group 3 — Sensors
| Method | Endpoint | Description | Expected Response |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/sensors` | Retrieves a list of all sensors. | 200 OK |
| GET | `/api/v1/sensors?type=CO2` | Retrieves sensors filtered by a specific type (e.g., CO2). | 200 OK |
| POST | `/api/v1/sensors` | Creates a new sensor linked to an existing room. | 201 Created |
| GET | `/api/v1/sensors/{sensorId}`| Retrieves full details of a specific sensor by its ID. | 200 OK |

### Group 4 — Readings
| Method | Endpoint | Description | Expected Response |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/sensors/{sensorId}/readings` | Retrieves the history of readings for a specific sensor. | 200 OK |
| POST| `/api/v1/sensors/{sensorId}/readings` | Records a new reading and updates the sensor's current value. | 201 Created |

## Section 4 — curl Commands

```bash
# 1. GET the discovery endpoint (Retrieves API metadata and links)
# Expected Response: 200 OK with HATEOAS JSON links
curl -X GET http://localhost:8080/smartCampus_w2153601/api/v1

# 2. POST create a room (Creates a new room representing Lab A)
# Expected Response: 201 Created with a Location header pointing to the new resource
curl -X POST http://localhost:8080/smartCampus_w2153601/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Lab A", "building": "Engineering", "floor": 2}'

# 3. GET sensors filtered by type (Optional filtering mapped via QueryParam)
# Expected Response: 200 OK with a JSON array of Temperature sensors
curl -X GET "http://localhost:8080/smartCampus_w2153601/api/v1/sensors?type=Temperature"

# 4. POST a sensor reading to a specific sensor (Records a new telemetry event)
# Expected Response: 201 Created with the reading appended to history
curl -X POST http://localhost:8080/smartCampus_w2153601/api/v1/sensors/1/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 24.5, "timestamp": "2026-04-22T10:45:00Z"}'

# 5. DELETE a room that still has sensors (Deliberately causes integrity violation)
# Expected Response: 409 Conflict due to linked sensors preventing deletion
curl -X DELETE http://localhost:8080/smartCampus_w2153601/api/v1/rooms/1

# 6. POST a sensor with a fake roomId (Semantic payload validation failure)
# Expected Response: 422 Unprocessable Entity
curl -X POST http://localhost:8080/smartCampus_w2153601/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"roomId": 9999, "name": "Fake Sensor", "type": "CO2", "unit": "ppm"}'

# 7. POST a reading to a sensor in MAINTENANCE (Business logic rule enforcement)
# Expected Response: 403 Forbidden
curl -X POST http://localhost:8080/smartCampus_w2153601/api/v1/sensors/99/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 0, "timestamp": "2026-04-22T10:45:00Z"}'
```

## Section 5 — Error Response Format

All custom and global errors across the API are caught and returned in the following standardized JSON structure:

```json
{
  "timestamp": "2026-04-22T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId 9999 does not exist.",
  "path": "/api/v1/sensors"
}
```

| HTTP Status Code | Scenario |
| :--- | :--- |
| **400 Bad Request** | The client provided malformed JSON syntax or omitted completely required mapping fields. |
| **403 Forbidden** | Business logic violation (e.g., trying to add a new reading to a sensor explicitly set to MAINTENANCE). |
| **404 Not Found** | The client requested a URL or referenced a specific path ID that absolutely does not exist. |
| **409 Conflict** | Data integrity violation (e.g., attempting to repeatedly delete a room that still visibly contains sensors). |
| **415 Unsupported Media Type** | The client forcefully supplied a payload format other than the required `@Consumes` mapping of `application/json`. |
| **422 Unprocessable Entity** | Valid JSON syntax sent to the absolutely correct endpoint, but explicitly references a missing reference dependency (like `roomId`). |
| **500 Internal Server Error** | Unexpected and completely unhandled server failure, gracefully caught globally to cleanly hide backend stack traces. |

## Section 6 — Question Answers

### Q1.1 — JAX-RS Lifecycle
By default, JAX-RS creates a new resource class instance for every incoming HTTP request, which means it doesnt operate as a singleton. Whenever a client hits an endpoint, the runtime instantiates the relevant resource class, executes the matched method, and immediately discards the instance after the response is sent.

Because of this request scoped lifecycle, you cannot store shared data or state directly inside the resource class itself as fields. It would be permanently lost after each request finishes. To solve this, the API delegates data storage to a separate singleton `DataStore` class that utilizes a static `ConcurrentHashMap` to persist data globally across the application's runtime.

Using a `ConcurrentHashMap` is critical because it allows multiple threads representing concurrent client requests to read and write simultaneously without corrupting the data structure. Without this thread-safe approach, two requests arriving at the exact same millisecond could overwrite each other's data, causing a race condition. Additionally, generating unique IDs is safely managed using structures like `AtomicLong`.

### Q1.2 — HATEOAS
HATEOAS stands for Hypermedia as the Engine of Application State. In a highly mature RESTful API, clients receive hypermedia links inside API responses, effectively telling them what actions and related resources are available next. For example, our discovery endpoint directly returns navigable links to `/rooms` and `/sensors`, allowing the client application to dynamically discover where to navigate without prior mapping.

Clients do not need to hardcode URLs or explicitly memorise the API structure. If the server decides to change a URL location in the future, only the server needs to update those dynamically generated links—the client seamlessly follows them. This vastly benefits client developers because static documentation goes outdated quickly, whereas links embedded directly in working responses are permanently accurate and massively reduce tight architectural coupling between client and server.

### Q2.1 — IDs Only vs Full Objects
When returning a collection like a list of rooms, returning only IDs means a very small payload and extremely low continuous bandwidth usage. However, this minimalist approach creates a bottleneck: the client must make a massive number of separate `GET` requests for every individual ID just to display meaningful detail, leading to heavy round trips and connection latency.

Conversely, returning full objects forces a noticeably larger structural payload size, but the client receives everything simultaneously in a single clean response. For a small system like a local campus API, full objects drastically simplify client side coding and optimize usability. While massive, enterprise-level collections (thousands of rooms) would typically demand pagination, your API proactively returns full objects to aggressively reduce client complexity and server round trips.

### Q2.2 — DELETE Idempotency
Yes, the `DELETE` operation is absolutely idempotent in this implementation. The strict definition of idempotency means an operation produces the exact same structural side effect strictly once, regardless of how many simultaneous times the client actually calls it. 

During the first call, if the room safely exists and has zero sensors, the room is deleted perfectly and a `204 No Content` is successfully returned. If the client mistakenly repeats the exact same `DELETE` request subsequently, the API correctly flags the item as missing and safely returns a `404 Not Found`. In both scenarios, the underlying state is exactly the same (the room is gone). Alternatively, if the room holds sensors, repeated attempts consistently and gracefully default to `409 Conflict` until those specific items are manually cleared.

### Q3.1 — @Consumes and 415 Error
The `@Consumes(MediaType.APPLICATION_JSON)` annotation acts as an absolute gateway, strictly forcing the JAX-RS runtime to recognize that the endpoint solely processes pure JSON payloads. If a naive client explicitly attempts to send data encoded in a completely different format natively, such as `text/plain` or `application/xml`, the framework's internal validation kicks in immediately.

JAX-RS will summarily reject the request absolutely automatically, instantly throwing a `415 Unsupported Media Type` before your business layer Java code is even touched. This deeply protects your business logic mapping from continuously processing totally unexpected, malformed, or unparseable text chunks, meaning the strict structural API contract is brilliantly enforced reliably at the framework tier.

### Q3.2 — QueryParam vs PathParam for Filtering
A `PathParam` (e.g. `/api/v1/sensors/type/CO2`) strictly identifies structurally that a specific, physically separate resource lives constantly exactly at that named URL. In significant contrast, a `QueryParam` optionally modifies physically how an overall broad collection is ultimately retrieved and viewed. Filtering by sensor type inherently represents an entirely optional search parameter—if omitted appropriately, you still simply get all sensors organically.

With native `PathParam` routing, you fundamentally cannot omit the filter, nor natively append combinations simultaneously. `QueryParam` offers vast technical flexibility: passing `?type=CO2&status=ACTIVE` successfully chains logical filters instantly. Standard REST architecture overwhelmingly dictates that path elements absolutely identify specific resources, whereas the URL query string gracefully filters or heavily modifies the broader structural view.

### Q4.1 — Sub-Resource Locator Pattern
Without utilizing sub-resource locators, every solitary nested endpoint uniformly—including `/sensors/{id}/readings`—would necessarily be stacked fully inside one excessively massive monolithic controller parent class. As the architecture progressively expands, that unique class quickly becomes overwhelmingly bloated, remarkably confusing to digest visually, and painful to maintain efficiently over time.

The Sub-Resource Locator directly bypasses this exact mess by gracefully separating operations cleanly: `SensorResource` essentially delegates control entirely to a separate dedicated `SensorReadingResource` entirely. JAX-RS technically parses these phases logically sequentially—locating the primary sensor strictly first, then safely handing control structurally over. This achieves spectacular separation of concerns, heavily ensuring endpoints remain extremely modular, easily testable, and deeply logically isolated entirely from standard sensor logic.

### Q5.2 — 422 vs 404
Returning a generic `404 Not Found` typically implies literally that the root endpoint uniquely requested does incredibly firmly not exist natively on the platform server. However, if a client posts structural JSON accurately to an active, validly listening endpoint like `/api/v1/sensors`, the URL is completely strictly correct—but the specific physical `roomId` internal value uniquely referenced just doesn't factually exist.

This represents a fundamentally logical mapping semantic failure structurally rather than a blindly missing path component completely. HTTP `422 Unprocessable Entity` universally means: "The framework definitively understood the request format, the structural JSON is flawlessly valid organically, but the mapped logic values simply cannot be physically processed internally." Blindly returning `404` directly misleads heavily the client permanently into incorrectly thinking the base target URL itself entirely failed gracefully.

### Q5.4 — Stack Trace Security Risk
From a strict cybersecurity vulnerability perspective, irresponsibly exposing core Java internal software stack traces massively helps malicious external attackers explicitly footprint uniquely exactly your unique application stack. Raw stack traces visually reveal inherently fundamental sensitive server directory paths mapping and extremely specific deep logic file path classes cleanly.

Additionally, attackers easily parse uniquely the specific deep framework modules comprehensively and library versions structurally heavily used. Hackers can trivially match structurally these versions fundamentally with established CVE publicly known logic bugs organically exposed. Using the global `ExceptionMapper<Throwable>` exclusively elegantly catches all completely unexpected internal tier failures gracefully simultaneously—preventing any explicit leakage and safely returning simply generic internal sanitized JSON uniquely formatted beautifully.

### Q5.5 — Logging Filters vs Manual Logging
Continuously adding standard repetitive `Logger.info()` cleanly across every solitary endpoint deeply inevitably results in completely bloated and heavily repetitive identical codebase components fundamentally across virtually every class physically. Moreover, when new endpoints seamlessly cleanly expand into existence internally organically, it is painfully extremely easy frankly to completely forget inserting loggers manually accurately, deeply establishing silent network monitoring blind mapping.

By structurally implementing specifically `ContainerRequestFilter` and `ContainerResponseFilter` universally natively inside the JAX-RS runtime universally dynamically, critical systemic logging automatically flawlessly executes natively organically instantly on literally every API network transaction precisely reliably universally securely. This fundamentally physically heavily ensures structural DRY (Don't Repeat Yourself) compliance implicitly purely successfully inherently safely cleanly keeping completely complex explicit core network methods singularly highly purely focused directly explicitly completely onto primary isolated unique explicit business internal process behaviors purely globally reliably.


