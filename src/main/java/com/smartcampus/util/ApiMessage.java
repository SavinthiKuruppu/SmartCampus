package com.smartcampus.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

public final class ApiMessage {

    private ApiMessage() {
    }

    public static Map<String, Object> of(String message, Object data) {
        return success(message, data, null);
    }

    public static Map<String, Object> success(String message, Object data, UriInfo uriInfo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);
        body.put("path", pathFrom(uriInfo));
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }

    public static Map<String, Object> collection(String message, Collection<?> data, UriInfo uriInfo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("count", data == null ? 0 : data.size());
        body.put("data", data);
        body.put("path", pathFrom(uriInfo));
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }

    private static String pathFrom(UriInfo uriInfo) {
        return uriInfo == null || uriInfo.getRequestUri() == null
                ? ""
                : uriInfo.getRequestUri().getPath();
    }
}
