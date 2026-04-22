package com.mycompany.smartcampus_w2153601.mappers;

import com.mycompany.smartcampus_w2153601.exceptions.ApiConflictException;
import com.mycompany.smartcampus_w2153601.model.ApiError;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ApiConflictException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ApiConflictException exception) {
        ApiError error = new ApiError(409, "Conflict", exception.getMessage(), getPath());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }

    private String getPath() {
        if (uriInfo == null || uriInfo.getPath() == null || uriInfo.getPath().isEmpty()) {
            return "/";
        }
        return "/" + uriInfo.getPath();
    }
}