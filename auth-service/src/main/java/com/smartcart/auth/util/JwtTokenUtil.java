package com.smartcart.auth.util;

import com.smartcart.auth.config.JwtRsaProperties;
import com.smartcart.auth.config.RsaKeyLoader;
import com.smartcart.common.dto.UserAuthDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final RsaKeyLoader rsaKeyLoader;
    private final JwtRsaProperties jwtRsaProperties;

    public String generateAccessToken(UserAuthDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtRsaProperties.getAccessTokenExpiration());
        String displayName = String.join(" ",
                        user.getFirstName() == null ? "" : user.getFirstName().trim(),
                        user.getLastName() == null ? "" : user.getLastName().trim())
                .trim();

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("roles", user.getRole())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("name", displayName.isEmpty() ? user.getEmail() : displayName)
                .setIssuer(jwtRsaProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setId(UUID.randomUUID().toString())
                .setHeaderParam("kid", jwtRsaProperties.getKeyId())
                .signWith(rsaKeyLoader.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(rsaKeyLoader.getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        return extractClaims(token).getExpiration().after(new Date());
    }

    public String getUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRoles(String token) {
        return (String) extractClaims(token).get("roles");
    }
}
