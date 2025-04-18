package com.example.insurance.interceptor;

import com.example.insurance.exception.InvalidRequestException;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
/**
 * Interceptor that validates incoming HTTP requests for insurance policy APIs.
 * Ensures that filtering and pagination parameters are valid before reaching the controller layer.
 */
@Slf4j
@Component
public class RequestValidationInterceptor implements HandlerInterceptor {

    /**
     * Intercepts HTTP requests before they reach the controller.
     * Performs validation for filter and pagination-related requests under /policies.
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param handler  the chosen handler to execute
     * @return true if the request is valid and should proceed; false otherwise
     * @throws InvalidRequestException if validation fails
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        log.info("Intercepting request: {}", uri);

        // Validate that at least one filter parameter is provided for /filter requests
        if (uri.contains("/filter")) {
            Map<String, String[]> params = request.getParameterMap();
            if (params.isEmpty()) {
                log.warn("Filter request with no parameters");
                throw new InvalidRequestException("At least one filter parameter must be provided.");
            }
        }

        // Validate pagination parameters for paginated /policies endpoint (excluding /filter and /search)
        if (uri.contains("/policies") && !uri.contains("/filter") && !uri.contains("/search")) {
            try {
                String page = request.getParameter("page");
                String size = request.getParameter("size");

                // Ensure page is >= 0 and size is > 0
                if ((page != null && Integer.parseInt(page) < 0) ||
                        (size != null && Integer.parseInt(size) <= 0)) {
                    log.warn("Invalid pagination: page={}, size={}", page, size);
                    throw new InvalidRequestException("Page must be >= 0 and size must be > 0.");
                }
            } catch (NumberFormatException e) {
                log.error("Pagination parameter not a number", e);
                throw new InvalidRequestException("Page and size must be valid integers.");
            }
        }

        // Allow request to proceed if all validations pass
        return true;
    }
}
