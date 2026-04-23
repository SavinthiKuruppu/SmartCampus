package com.smartcampus.resource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.smartcampus.model.Room;
import com.smartcampus.service.RoomService;
import com.smartcampus.util.ApiMessage;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomService roomService = new RoomService();

    @GET
    public Map<String, Object> getRooms(@Context UriInfo uriInfo) {
        List<Room> rooms = roomService.getAllRooms();
        return ApiMessage.collection("Rooms retrieved successfully.", rooms, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Room created = roomService.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        Map<String, Object> body = ApiMessage.success("Room created successfully.", created, uriInfo);
        return Response.created(location).entity(body).build();
    }

    @GET
    @Path("/{roomId}")
    public Map<String, Object> getRoom(@PathParam("roomId") String roomId, @Context UriInfo uriInfo) {
        Room room = roomService.getRoomById(roomId);
        return ApiMessage.success("Room retrieved successfully.", room, uriInfo);
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId, @Context UriInfo uriInfo) {
        Room deleted = roomService.deleteRoom(roomId);
        return Response.ok(ApiMessage.success("Room deleted successfully.", deleted, uriInfo)).build();
    }
}
