package com.smartcampus.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ErrorResponse build(Response.Status status, String message, UriInfo uriInfo) {
        return new ErrorResponse(
                System.currentTimeMillis(),
                status.getStatusCode(),
                status.getReasonPhrase(),
                message,
                pathFrom(uriInfo)
        );
    }

    public static ErrorResponse build(int statusCode, String reason, String message, UriInfo uriInfo) {
        return new ErrorResponse(
                System.currentTimeMillis(),
                statusCode,
                reason,
                message,
                pathFrom(uriInfo)
        );
    }

    private static String pathFrom(UriInfo uriInfo) {
        return uriInfo == null || uriInfo.getRequestUri() == null
                ? ""
                : uriInfo.getRequestUri().getPath();
    }
}
