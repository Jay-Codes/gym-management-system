package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.business.service.CustomUserDetailsService;
import com.jerrycode.gym_services.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String requestInfo = String.format("%s %s from %s", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found for request: {}", requestInfo);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtService.validateJwtToken(token)) {
                logger.warn("Invalid or expired JWT token for request: {}", requestInfo);
                sendErrorResponse(response, "Token is invalid or expired", "TOKEN_EXPIRED");
                return;
            }

            String email = jwtService.getUsernameEmailFromToken(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authenticated user '{}' for request: {}", email, requestInfo);
                }
            }
        } catch (JwtAuthenticationException e) {
            logger.error("JWT authentication failed for request {}: {}", requestInfo, e.getMessage());
            sendErrorResponse(response, e.getMessage(), "TOKEN_EXPIRED");
            return;
        } catch (Exception e) {
            logger.error("Unexpected error validating JWT for request {}: {}", requestInfo, e.getMessage());
            sendErrorResponse(response, "Unexpected error during token validation", "UNEXPECTED_ERROR");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, String errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"success\": false, \"message\": \"%s\", \"errorCode\": \"%s\"}", message, errorCode));
    }
}