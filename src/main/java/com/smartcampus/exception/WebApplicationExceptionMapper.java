package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.smartcampus.util.ErrorResponseFactory;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse() == null
                ? Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                : exception.getResponse().getStatus();
        Response.Status statusType = Response.Status.fromStatusCode(status);
        String reason = statusType == null ? "HTTP Error" : statusType.getReasonPhrase();
        String message = cleanMessage(exception.getMessage());
        message = message == null || message.trim().isEmpty()
                ? reason
                : message;

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErrorResponseFactory.build(status, reason, message, uriInfo))
                .build();
    }

    private String cleanMessage(String rawMessage) {
        if (rawMessage == null) {
            return null;
        }
        int separator = rawMessage.indexOf(": ");
        if (separator >= 0 && rawMessage.startsWith("HTTP ")) {
            return rawMessage.substring(separator + 2);
        }
        return rawMessage;
    }
}
