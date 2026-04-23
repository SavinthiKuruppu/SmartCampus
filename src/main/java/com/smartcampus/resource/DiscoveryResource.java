package com.smartcampus.resource;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> discover(@Context UriInfo uriInfo) {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensorReadingsTemplate", "/api/v1/sensors/{sensorId}/readings");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Smart Campus API discovery information.");
        response.put("apiName", "Smart Campus Sensor and Room Management REST API");
        response.put("version", "1.0");
        response.put("adminContact", "smart-campus-admin@westminster.example");
        response.put("description", "Coursework-ready JAX-RS API for managing campus rooms, sensors and sensor readings using in-memory Java collections.");
        response.put("links", links);
        response.put("path", uriInfo.getRequestUri().getPath());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
