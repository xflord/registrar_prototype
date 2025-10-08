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

            // 1. Create the unauthenticated CurrentUser.
            // Using ID 0 and empty groups for a guest user. The constructor sets 'principal = null'.
            CurrentUser unauthenticatedUser = new CurrentUser();

            // 2. Create the token that holds the CurrentUser.
            RegistrarAuthenticationToken unauthenticatedToken =
                new RegistrarAuthenticationToken(unauthenticatedUser, Collections.emptyList());

            // 3. CRUCIAL: Mark the token as NOT authenticated.
            // The CurrentUser.isAuthenticated() method will then also return false.
            unauthenticatedToken.setAuthenticated(false);

            // 4. Set the token into the security context.
            SecurityContextHolder.getContext().setAuthentication(unauthenticatedToken);
        }

        filterChain.doFilter(request, response);
    }
}
