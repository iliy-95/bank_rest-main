package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtToken {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private Long jwtExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public String generatorToken(UserDetails userDetails) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
    } catch (JwtException e) {
      log.error("Error extracting username from token: {}", e.getMessage());
      return null;
    }
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  public List<String> getRolesFromToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();

      return claims.get("roles", List.class);
    } catch (JwtException e) {
      log.error("Error extracting roles from token: {}", e.getMessage());
      return List.of();
    }
  }
}