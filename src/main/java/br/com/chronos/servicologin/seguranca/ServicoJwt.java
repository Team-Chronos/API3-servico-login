package br.com.chronos.servicologin.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.chronos.servicologin.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class ServicoJwt {

    private static final Logger log = LoggerFactory.getLogger(ServicoJwt.class);

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey secretKey;

    private SecretKey getSignInKey() {
        if (secretKey == null) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-384");
                byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
                secretKey = new SecretKeySpec(hash, "HmacSHA384");
                log.info("Chave JWT derivada com SHA-384 (tamanho: {} bytes)", hash.length);
            } catch (Exception e) {
                log.error("Erro ao derivar chave JWT", e);
                throw new RuntimeException("Falha na inicialização da chave JWT", e);
            }
        }
        return secretKey;
    }

    private List<String> obterRolesPorCargo(Long cargoId) {
        if (cargoId == null) return List.of("ROLE_USER");
        switch (cargoId.intValue()) {
            case 1:
                return List.of("ROLE_USER");   
            case 2:
                return List.of("ROLE_GERENTE_PROJETO");
            case 3:
                return List.of("ROLE_FINANCE");
            default:
                return List.of("ROLE_USER");
        }
    }

    public String generateToken(Usuario user) {
        try {
            Instant now = Instant.now();
            Date issuedAt = Date.from(now);
            Date expiration = Date.from(now.plusMillis(expirationMs));

            List<String> roles = obterRolesPorCargo(user.cargoId());

            String token = Jwts.builder()
                    .subject(user.email())
                    .claim("id", user.id())
                    .claim("nome", user.nome())
                    .claim("cargo_id", user.cargoId())
                    .claim("roles", roles)  
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(getSignInKey())
                    .compact();

            log.info("Token gerado para {} com roles: {}", user.email(), roles);
            return token;
        } catch (Exception e) {
            log.error("Erro ao gerar token", e);
            throw new RuntimeException("Falha na geração do token", e);
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            boolean valid = claims.getExpiration().after(new Date());
            if (!valid) log.warn("Token expirado");
            return valid;
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}