package com.smartcart.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Getter
public class RsaKeyLoader {

    @Value("${jwt.rsa.private-key-file}")
    private Resource privateKeyResource;

    @Value("${jwt.rsa.public-key-file}")
    private Resource publicKeyResource;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void loadKeys() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey = loadPublicKey(publicKeyResource);
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }
}
