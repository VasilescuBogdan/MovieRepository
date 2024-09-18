package com.ucv.ace.security;

import com.ucv.ace.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWT {

    private static final long EXPIRE_DURATION = 24L * 60 * 60 * 1000; // 24 hour

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    public String generateAccessToken(User user) {

        return Jwts.builder()
                .setSubject(String.format("%s,%s", user.getUserId(), user.getEmail()))
                .setIssuer("MyApp")
                .claim("role", user.getUserRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_DURATION))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

}
