package com.example.insurance.config;

import com.example.insurance.interceptor.RequestValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
/**
 * Web configuration class for setting up interceptors and CORS mappings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestValidationInterceptor requestValidationInterceptor;

    /**
     * Registers custom interceptors with the application.
     * In this case, adds a RequestValidationInterceptor to validate requests to /policies/** endpoints.
     *
     * @param registry the InterceptorRegistry to which interceptors are added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestValidationInterceptor)
                .addPathPatterns("/policies/**");
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings for the application.
     * Allows GET requests from the specified frontend origin (http://localhost:5173) to access any backend endpoint.
     *
     * @return a WebMvcConfigurer bean with CORS mappings configured
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173") // frontend URL
                        .allowedMethods("GET")
                        .allowedHeaders("*");
            }
        };
    }

}
