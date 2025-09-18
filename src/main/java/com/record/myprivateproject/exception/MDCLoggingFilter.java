package com.record.myprivateproject.exception;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

public class MDCLoggingFilter implements Filter {
    private static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
        try {
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);
            if (req instanceof HttpServletRequest request) {
                MDC.put("requestURI", request.getRequestURI());
                MDC.put("method", request.getMethod());
            }
            chain.doFilter(req, rep);
        }finally {
            MDC.clear();
        }
    }
}
