package com.jerrycode.gym_services.config;


import com.jerrycode.gym_services.utils.RoleCheck;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RoleCheckAspect {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpServletRequest request;

    @Before("@annotation(roleCheck)")
    public void checkRole(RoleCheck roleCheck) {
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtService.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String role = claims.get("role", String.class);

        // Set role in request attribute so controller can access it
        request.setAttribute("userRole", role);

        // Enforce restriction only if method is exclusive to certain roles
        boolean authorized = false;
        for (String requiredRole : roleCheck.value()) {
            if (role.equals(requiredRole)) {
                authorized = true;
                break;
            }
        }

        if (!authorized && roleCheck.value().length > 0) {
            throw new SecurityException("Required roles: " + String.join(", ", roleCheck.value()));
        }
    }

}