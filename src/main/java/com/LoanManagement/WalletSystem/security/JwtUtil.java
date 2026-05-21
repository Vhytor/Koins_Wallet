package com.LoanManagement.WalletSystem.security;

import com.LoanManagement.WalletSystem.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtProperties props;
    private Key key;

    public JwtUtil(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = props.getSecret() == null ? new byte[0] : props.getSecret().getBytes(StandardCharsets.UTF_8);
        // jjwt requires a sufficiently long key for HmacSHA256 (>= 256 bits). If provided secret is shorter,
        // hash it with SHA-256 to derive a 32-byte key.
        if (keyBytes.length < 32) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                keyBytes = md.digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Unable to initialize JWT key", e);
            }
        }
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpirationMs());
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // Convenience methods for testing and other use cases
    public String getEmailFromToken(String token) {
        return getSubject(token);
    }

    public boolean isTokenValid(String token, String email) {
        try {
            String subject = getSubject(token);
            return subject.equals(email) && validateToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get token expiration date
     * @param token JWT token
     * @return Token expiration date
     */
    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }
}

