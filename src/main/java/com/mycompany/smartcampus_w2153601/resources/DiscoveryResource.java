package com.mycompany.smartcampus_w2153601.resources;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo;

    @GET
    public Response discover() {
        String baseUrl = uriInfo.getBaseUri().toString();

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("name", "Smart Campus API");
        response.put("version", "v1");

        Map<String, String> contact = new LinkedHashMap<String, String>();
        contact.put("module", "CSA Coursework");
        contact.put("owner", "w2153601");
        contact.put("support", "academic-support@example.com");
        response.put("contact", contact);

        Map<String, Object> resources = new LinkedHashMap<String, Object>();
        resources.put("rooms", resourceMap(baseUrl + "rooms", "GET, POST"));
        resources.put("roomDetail", resourceMap(baseUrl + "rooms/{id}", "GET, DELETE"));
        resources.put("sensors", resourceMap(baseUrl + "sensors", "GET, POST"));
        resources.put("sensorDetail", resourceMap(baseUrl + "sensors/{id}", "GET"));
        resources.put("sensorReadings", resourceMap(baseUrl + "sensors/{id}/readings", "GET, POST"));
        resources.put("simulateError", resourceMap(baseUrl + "simulate-error", "GET"));
        response.put("resources", resources);

        return Response.ok(response).build();
    }

    @GET
    @Path("simulate-error")
    public Response simulateError() {
        throw new RuntimeException("Simulated internal failure for mapper verification.");
    }

    private Map<String, String> resourceMap(String href, String methods) {
        Map<String, String> details = new LinkedHashMap<String, String>();
        details.put("href", href);
        details.put("methods", methods);
        return details;
    }
}