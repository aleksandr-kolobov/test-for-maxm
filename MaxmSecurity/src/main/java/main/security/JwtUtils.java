package main.security;

import io.jsonwebtoken.*;
import main.model.Role;
import main.model.User;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtils {
    @Value("${jwt.secretkey}")
    private String jwtSecret;
    @Value("${jwt.expirationmin}")
    private Integer jwtExpirationMin;
    @Value("${jwt.refreshexpirationmin}")
    private Integer refreshExpirationMin;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public String createAccessToken(String email, Role role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMin * 60_000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String createRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString();
        Long refreshExpiration = (new Date()).getTime() + refreshExpirationMin * 60_000;
        User user = userRepository.getById(userId);
        user.setRefreshToken(refreshToken);
        user.setRefreshExpiration(refreshExpiration);
        userRepository.save(user);
        return refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken, Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();
        if (new Date(user.getRefreshExpiration()).before(new Date())) {
            return false;
        }
        return refreshToken.equals(user.getRefreshToken());
    }
}