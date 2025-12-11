package org.perun.registrarprototype.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds correlation id from the request to MDC (or generates a new one if missing). This serves to correlate logs should
 * user join identities (change/gain identifier) and to correlate across services (e.g. Perun and Registrar)
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  private final static String CORRELATION_ID_HEADER = "X-Correlation-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);

    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }

    response.setHeader(CORRELATION_ID_HEADER, correlationId);
    request.setAttribute(CORRELATION_ID_HEADER, correlationId);

    MDC.put("correlationId", correlationId);

    try {
      filterChain.doFilter(request, response);
    }  finally {
      MDC.remove("correlationId");
    }
  }
}
