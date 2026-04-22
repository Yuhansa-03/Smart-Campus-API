package com.mycompany.smartcampus_w2153601.mappers;

import com.mycompany.smartcampus_w2153601.model.ApiError;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GlobalThrowableMapper implements ExceptionMapper<Throwable> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException webException = (WebApplicationException) exception;
            int statusCode = webException.getResponse().getStatus();
            String message = webException.getMessage();

            if (message == null || message.trim().isEmpty() || statusCode >= 500) {
                message = statusCode >= 500
                        ? "An unexpected error occurred. Please contact support if the issue persists."
                        : "Request could not be completed.";
            }

            ApiError clientOrWebError = new ApiError(
                    statusCode,
                    resolveReason(statusCode),
                    message,
                    getPath()
            );

            return Response.status(statusCode)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(clientOrWebError)
                    .build();
        }

        ApiError serverError = new ApiError(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact support if the issue persists.",
                getPath()
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(serverError)
                .build();
    }

    private String resolveReason(int statusCode) {
        Response.Status status = Response.Status.fromStatusCode(statusCode);
        return status != null ? status.getReasonPhrase() : "Error";
    }

    private String getPath() {
        if (uriInfo == null || uriInfo.getPath() == null || uriInfo.getPath().isEmpty()) {
            return "/";
        }
        return "/" + uriInfo.getPath();
    }
}