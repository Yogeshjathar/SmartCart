package com.smartcart.auth.security;

import com.smartcart.auth.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String token = (String) authentication.getCredentials();

        // 1️⃣ Validate Token
        if (!jwtTokenUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired JWT token");
        }

        // 2️⃣ Extract Claims
        Claims claims = jwtTokenUtil.extractClaims(token);

        String userId = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        // 3️⃣ Build UserDetails
        CustomUserDetails userDetails =
                new CustomUserDetails(userId, roles);

        // 4️⃣ Return authenticated object
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                token,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
