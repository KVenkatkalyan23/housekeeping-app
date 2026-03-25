package com.ibe.housekeeping.auth.security;

import com.ibe.housekeeping.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMillis
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());

        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).get("userId", String.class));
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, User user) {
        Claims claims = extractAllClaims(token);
        return user.getUsername().equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
