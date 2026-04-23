package com.smartcampus.resource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.smartcampus.model.Sensor;
import com.smartcampus.service.SensorService;
import com.smartcampus.util.ApiMessage;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorService sensorService = new SensorService();

    @GET
    public Map<String, Object> getSensors(@QueryParam("type") String type, @Context UriInfo uriInfo) {
        List<Sensor> sensors = sensorService.getAllSensors(type);
        String message = type == null || type.trim().isEmpty()
                ? "Sensors retrieved successfully."
                : "Sensors filtered by type successfully.";
        return ApiMessage.collection(message, sensors, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        Sensor created = sensorService.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        Map<String, Object> body = ApiMessage.success("Sensor created successfully.", created, uriInfo);
        return Response.created(location).entity(body).build();
    }

    @GET
    @Path("/{sensorId}")
    public Map<String, Object> getSensor(@PathParam("sensorId") String sensorId, @Context UriInfo uriInfo) {
        Sensor sensor = sensorService.getSensorById(sensorId);
        return ApiMessage.success("Sensor retrieved successfully.", sensor, uriInfo);
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        sensorService.getSensorById(sensorId);
        return new SensorReadingResource(sensorId);
    }
}
