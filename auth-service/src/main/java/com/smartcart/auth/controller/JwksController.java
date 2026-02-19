package com.smartcart.auth.controller;

import com.smartcart.auth.config.RsaKeyLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
public class JwksController {

    private final RsaKeyLoader rsaKeyLoader;

    public JwksController(RsaKeyLoader rsaKeyLoader) {
        this.rsaKeyLoader = rsaKeyLoader;
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> getKeys() {
        RSAPublicKey publicKey = (RSAPublicKey) rsaKeyLoader.getPublicKey();

        Map<String, Object> key = new HashMap<>();
        key.put("kty", "RSA");
        key.put("alg", "RS256");
        key.put("use", "sig");
        key.put("kid", "smartcart-key");
        key.put("n", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getModulus().toByteArray()));
        key.put("e", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getPublicExponent().toByteArray()));

        return Map.of("keys", List.of(key));
    }
}
