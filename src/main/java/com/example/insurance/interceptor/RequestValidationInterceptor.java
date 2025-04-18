package com.example.insurance.interceptor;

import com.example.insurance.exception.InvalidRequestException;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Slf4j
@Component
public class RequestValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        log.info("Intercepting request: {}", uri);

        if (uri.contains("/filter")) {
            Map<String, String[]> params = request.getParameterMap();
            if (params.isEmpty()) {
                log.warn("Filter request with no parameters");
                throw new InvalidRequestException("At least one filter parameter must be provided.");
            }
        }

        if (uri.contains("/policies") && !uri.contains("/filter") && !uri.contains("/search")) {
            try {
                String page = request.getParameter("page");
                String size = request.getParameter("size");

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

        return true;
    }
}
