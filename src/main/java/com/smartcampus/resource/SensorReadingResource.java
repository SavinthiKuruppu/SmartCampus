package com.smartcampus.resource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.smartcampus.model.SensorReading;
import com.smartcampus.service.SensorReadingService;
import com.smartcampus.util.ApiMessage;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final SensorReadingService readingService = new SensorReadingService();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Map<String, Object> getReadings(@Context UriInfo uriInfo) {
        List<SensorReading> readings = readingService.getReadingsForSensor(sensorId);
        return ApiMessage.collection("Sensor readings retrieved successfully.", readings, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        SensorReading created = readingService.addReading(sensorId, reading);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        Map<String, Object> body = ApiMessage.success(
                "Sensor reading created successfully and sensor currentValue updated.",
                created,
                uriInfo);
        return Response.created(location).entity(body).build();
    }
}
