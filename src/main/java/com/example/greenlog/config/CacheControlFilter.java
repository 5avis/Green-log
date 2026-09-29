package com.example.greenlog.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ensures all HTTP responses include aggressive cache-invalidation headers
 * so that browsers (Chrome, Vivaldi, Firefox) never serve cached pages from other
 * projects that previously ran on localhost:8080.
 */
@Component
public class CacheControlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpServletResponse) {
            httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            httpServletResponse.setHeader("Pragma", "no-cache");
            httpServletResponse.setDateHeader("Expires", 0);
        }
        chain.doFilter(request, response);
    }
}
