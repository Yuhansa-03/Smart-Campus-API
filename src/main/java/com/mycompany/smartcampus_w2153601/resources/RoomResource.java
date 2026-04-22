package com.mycompany.smartcampus_w2153601.resources;

import com.mycompany.smartcampus_w2153601.model.Room;
import com.mycompany.smartcampus_w2153601.store.InMemoryStore;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @GET
    public Response getAllRooms() {
        return Response.ok(store.getRooms()).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Room createdRoom = store.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createdRoom.getId())).build();

        return Response.created(location)
                .entity(createdRoom)
                .build();
    }

    @GET
    @Path("{roomId}")
    public Response getRoomById(@PathParam("roomId") Long roomId) {
        Room room = store.getRoom(roomId);

        if (room == null) {
            throw new NotFoundException("Room " + roomId + " was not found.");
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") Long roomId) {
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }
}