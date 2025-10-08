package org.perun.registrarprototype.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.perun.registrarprototype.security.CurrentUser;
import org.perun.registrarprototype.security.RegistrarAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class UnauthenticatedUserFilter extends OncePerRequestFilter {
  @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only run if NO Authentication object has been successfully placed by previous filters (like the JWT filter).
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            CurrentUser unauthenticatedUser = new CurrentUser();
            RegistrarAuthenticationToken unauthenticatedToken =
                new RegistrarAuthenticationToken(unauthenticatedUser, Collections.emptyList());
            unauthenticatedToken.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(unauthenticatedToken);
            // TODO consider identifiers for unauthenticated users to consolidate later? probably can do this only based on
            //  filled item data
        }

        filterChain.doFilter(request, response);
    }
}
