package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtToken jwtToken;

  private final UserDetailsServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    extractJwtFromRequest(request).ifPresent(jwt -> {
      if (jwtToken.validateToken(jwt)) {
        authenticateUser(jwt, request);
      }
    });

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractJwtFromRequest(HttpServletRequest request) {

    return extractJwtFromHeader(request)
        .or(() -> extractJwtFromParameter(request));
  }

  private Optional<String> extractJwtFromHeader(HttpServletRequest request) {

    return Optional.ofNullable(request.getHeader("Authorization"))
        .filter(header -> header.startsWith("Bearer "))
        .map(header -> header.substring(7))
        .filter(StringUtils::hasText);
  }

  private Optional<String> extractJwtFromParameter(HttpServletRequest request) {

    return Optional.ofNullable(request.getParameter("token"))
        .filter(StringUtils::hasText);
  }

  private void authenticateUser(String jwt, HttpServletRequest request) {
    try {
      String username = jwtToken.getUsernameFromToken(jwt);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated user: {}", username);
      }
    } catch (Exception ex) {
      log.error("Failed to authenticate user with token", ex);
    }
  }

}