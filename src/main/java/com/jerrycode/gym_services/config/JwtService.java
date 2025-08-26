package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.AccessTokenRepository;
import com.jerrycode.gym_services.data.vo.AccessToken;
import com.jerrycode.gym_services.data.vo.User;
import com.jerrycode.gym_services.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expirationMs:86400000}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refreshExpirationMs:604800000}")
    private long refreshExpirationMs;

    @Autowired
    private ModelMapper modelMapper;

    private final AccessTokenRepository tokenRepository;

    public JwtService(AccessTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public AccessToken generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("role", user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("User has no roles assigned")));
            claims.put("userId", user.getId());

            // Delete all existing non-revoked tokens for the user
            tokenRepository.deleteByUserIdAndRevokedFalse(user.getId());

            String token = generateToken(claims, userDetails);
            AccessToken accessToken = new AccessToken();
            accessToken.setUser(user);
            accessToken.setToken(token);
            accessToken.setIssuedAt(Instant.now());
            accessToken.setExpiresAt(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS));
            accessToken.setRevoked(false);
            return tokenRepository.save(accessToken);
        }
        return null;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(new HashMap<>(), userPrincipal);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean validateJwtToken(String authToken) throws JwtAuthenticationException {
        Optional<AccessToken> tokenOpt = tokenRepository.findByToken(authToken);
        if (!tokenOpt.isPresent() || tokenOpt.get().isRevoked()) {
            throw new JwtAuthenticationException("Token is invalid or revoked");
        }
        AccessToken accessToken = tokenOpt.get();
        if (accessToken.getExpiresAt() != null && accessToken.getExpiresAt().isBefore(Instant.now())) {
            throw new JwtAuthenticationException("Token has expired");
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid JWT token: " + e.getMessage());
        }
    }

    public String getUserNameFromJwtToken(String token) {
        return extractUsername(token);
    }

    public String getUsernameEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid or expired token", e);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractCompanyId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("company", Long.class);
    }

    public String extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    protected Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Transactional
    public void revokeToken(String token) {
        Optional<AccessToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            AccessToken accessToken = tokenOpt.get();
            accessToken.setRevoked(true);
            tokenRepository.save(accessToken);
        }
    }
}