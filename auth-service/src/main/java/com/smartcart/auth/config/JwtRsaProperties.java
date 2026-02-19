package com.smartcart.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Component
@ConfigurationProperties(prefix = "jwt.rsa")
public class JwtRsaProperties {

    private String issuer;
    private String keyId;
    private long accessTokenExpiration;
    private String privateKeyFile;
    private String publicKeyFile;

    // These will be loaded from PEM
    private Resource privateKey;
    private Resource publicKey;
}
