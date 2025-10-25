// java
package com.authentication.service.auth.security;

import com.authentication.service.auth.service.JWTService;
import com.authentication.service.auth.service.MyUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();  // Changed from getServletPath to getRequestURI
        System.out.println("\n=== Processing request for path: " + path + " ===");

        if (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/validate")
                || path.startsWith("/actuator/health")) {
            System.out.println("Skipping authentication for public endpoint: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header present: " + (authHeader != null));

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
                System.out.println("Successfully extracted username from token: " + username);
                String role = jwtService.extractRole(token);
                System.out.println("Successfully extracted role from token: " + role);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Create authority with ROLE_ prefix
                    org.springframework.security.core.authority.SimpleGrantedAuthority authority =
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role);
                    System.out.println("Created authority: " + authority.getAuthority());

                    org.springframework.security.core.userdetails.User userDetails =
                            new org.springframework.security.core.userdetails.User(
                                    username,
                                    "",
                                    java.util.Collections.singletonList(authority)
                            );
                    System.out.println("Created UserDetails with authorities: " + userDetails.getAuthorities());

                    if (jwtService.isTokenValid(token, userDetails)) {
                        System.out.println("✅ Token is valid for user: " + username);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("✅ Authentication set in SecurityContext with authorities: " + authToken.getAuthorities());
                    } else {
                        System.out.println("❌ Token validation failed for user: " + username);
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Error processing token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ No valid Authorization header found");
        }

        filterChain.doFilter(request, response);
    }
}