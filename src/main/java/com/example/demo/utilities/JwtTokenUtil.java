package com.example.demo.utilities;

import com.example.demo.model.MyAppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private static final long EXPIRATION_TIME = 12 * 60 * 60 * 1000L; // 12 hours in milliseconds

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long accessTokenValidity = 10 * 60 * 60 * 1000; // 10 hours
    private final long refreshTokenValidity = 30 * 24 * 60 * 60 * 1000L; // 30 days

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)); // Use the configured key
    }

    public String generateAccessToken(MyAppUser user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(MyAppUser user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("Unsupported token", e);
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("Malformed token", e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Invalid signature", e);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = getClaims(token);
            String username = claims.getSubject();
            Date expiration = claims.getExpiration();
            return (username.equals(userDetails.getUsername()) && !expiration.before(new Date()));
        } catch (JwtException e) {
            return false;
        }
    }
}
