package com.mycompany.smartcampus_w2153601.filters;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Centralized API request/response logging.
 */
@Provider
@Priority(Priorities.USER)
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());
    private static final String REQUEST_ID_PROPERTY = "requestId";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = UUID.randomUUID().toString();
        requestContext.setProperty(REQUEST_ID_PROPERTY, requestId);

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(String.format("[%s] Incoming request: %s %s", requestId, method, path));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        Object requestIdValue = requestContext.getProperty(REQUEST_ID_PROPERTY);
        String requestId = requestIdValue != null ? requestIdValue.toString() : "N/A";

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();

        LOGGER.info(String.format("[%s] Outgoing response: %s %s -> %d", requestId, method, path, status));
    }
}
