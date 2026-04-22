package com.mycompany.smartcampus_w2153601.resources;

import com.mycompany.smartcampus_w2153601.model.SensorReading;
import com.mycompany.smartcampus_w2153601.store.InMemoryStore;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Long sensorId;
    private final InMemoryStore store;

    public SensorReadingResource(Long sensorId) {
        this.sensorId = sensorId;
        this.store = InMemoryStore.getInstance();
    }

    @GET
    public Response getReadings() {
        return Response.ok(store.getReadings(sensorId)).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        SensorReading createdReading = store.addReading(sensorId, reading);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdReading.getId())).build();

        return Response.created(location)
                .entity(createdReading)
                .build();
    }
}