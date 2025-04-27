package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.response.TokenValidationResponse;
import com.sas.sas_backend.exceptions.token.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

@Service
public class TokenService {

    private String jwtSecret = "ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341ahsdqweuqwyijhgvfçp982341";

    private HashMap<String, String> forbiddenToken = new HashMap<>(); // para teste local, dados em memória

    public String gerarToken(String id, String role) {
        if (forbiddenToken.containsKey(id)) {
            forbiddenToken.remove(id);
        }
        return Jwts.builder().subject(id).claim("role", role)
                .setIssuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(12, ChronoUnit.HOURS)))
                .signWith(getSecretKey())
                .compact();
    }

    public TokenValidationResponse validarTokenRetornandoDados(String token) {
        try {
            var parser = Jwts.parser().verifyWith(getSecretKey()).build();
            var claims = parser.parseSignedClaims(token).getPayload();

            String id = claims.getSubject();
            String role = claims.get("role", String.class);

            if (forbiddenToken.containsKey(buildCacheSufix(role, id))) {
                throw new InvalidTokenException("Token inválido!");
            }

            return new TokenValidationResponse(id, role, true);

        } catch (Exception e) {
            throw new InvalidTokenException("Token inválido!");
        }
    }

    public void logout(String token) {
        var payload =
                Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
        if (forbiddenToken.containsKey(payload.getSubject())) {
            throw new InvalidTokenException("Token já deslogado!");
        }

        forbiddenToken.put(buildCacheSufix(payload.get("role", String.class), payload.getSubject()), token);
    }

    private String buildCacheSufix(String id, String role) {
        return role + "_" + id;
    }

    private SecretKey getSecretKey() {
        return new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
    }

}
