package com.mycompany.smartcampus_w2153601.resources;

import com.mycompany.smartcampus_w2153601.model.Sensor;
import com.mycompany.smartcampus_w2153601.store.InMemoryStore;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("sensors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        return Response.ok(store.getSensors(type)).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        Sensor createdSensor = store.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdSensor.getId())).build();

        return Response.created(location)
                .entity(createdSensor)
                .build();
    }

    @GET
    @Path("{sensorId}")
    public Response getSensorById(@PathParam("sensorId") Long sensorId) {
        Sensor sensor = store.getSensor(sensorId);

        if (sensor == null) {
            throw new NotFoundException("Sensor " + sensorId + " was not found.");
        }

        return Response.ok(sensor).build();
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") Long sensorId) {
        store.getSensorOrThrow(sensorId);
        return new SensorReadingResource(sensorId);
    }
}