package OneQ.OnSurvey.global.auth.utils;


import OneQ.OnSurvey.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static OneQ.OnSurvey.global.exception.ErrorCode.INVALID_PARAMETER;
import static OneQ.OnSurvey.global.exception.TokenExceptionMessage.INVALID_TOKEN;

@Component
@Slf4j
public class JWTUtil {

    @Value("${spring.jwt.iss}")
    private String tokenIss;

    @Value("${spring.token.access.category}")
    private String accessTokenCategory;

    @Value("${spring.token.access.expire-ms}")
    private Long accessTokenExpireMs;

    @Value("${spring.token.refresh.category}")
    private String refreshTokenCategory;

    @Value("${spring.token.refresh.expire-ms}")
    private Long refreshTokenExpireMs;

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public <T> T getClaimFromToken(String token, String key, Class<T> clazz) throws AuthenticationException {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(key, clazz);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException(INVALID_TOKEN.getMessage(), e);
        }
    }


    public boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (SignatureException | ExpiredJwtException e){
            throw new BadCredentialsException(INVALID_TOKEN.getMessage(), e);
        }
    }

    public String createJWT(String category, Long userKey, String role) {
        if(Objects.equals(category, accessTokenCategory)) {
            return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userKey.toString())
                .issuer(tokenIss)
                .claim("category", category)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpireMs))
                .signWith(secretKey)
                .compact();
        }
        else if(Objects.equals(category, refreshTokenCategory)) {
            return Jwts.builder()
                .subject(userKey.toString())
                .issuer(tokenIss)
                .claim("category", category)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpireMs))
                .signWith(secretKey)
                .compact();
        }
        else {
            throw new CustomException(INVALID_PARAMETER);
        }
    }

    /** Request에서  Token 추출 */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public void validateIssuer(String token, String expectedIssuer) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String iss = claims.getIssuer(); // 표준 iss
            if (iss == null || !iss.equals(expectedIssuer)) {
                throw new BadCredentialsException(INVALID_TOKEN.getMessage());
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException(INVALID_TOKEN.getMessage());
        }
    }

    public Long getUserKeyFromSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String sub = claims.getSubject();
            if (sub == null) {
                throw new BadCredentialsException(INVALID_TOKEN.getMessage());
            }
            return Long.parseLong(sub);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException(INVALID_TOKEN.getMessage());
        }
    }

    public Date getExpiration(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getExpiration();
    }

    public String getJti(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getId();
    }
}
