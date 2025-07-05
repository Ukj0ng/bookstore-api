package ukjong.bookstore_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ukjong.bookstore_api.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret:bookstore-secret-key-for-jwt-token-generation-very-long-secret-key}") String secret,
            @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds:604800}") long refreshTokenValidityInSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;

        log.info("JWT Token Provider 초기화 완료 - Access Token 유효시간: {}초, Refresh Token 유효시간: {}초",
                accessTokenValidityInSeconds, refreshTokenValidityInSeconds);
    }

    public String createAccessToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.warn("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new InvalidTokenException("유효하지 않은 토큰입니다");
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("username", String.class);
        } catch (Exception e) {
            log.warn("토큰에서 사용자명 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.warn("토큰에서 역할 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            log.warn("토큰 타입 확인 실패: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("토큰 보안 검증 실패: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("토큰이 비어있거나 null: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("토큰 검증 중 알 수 없는 오류: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        String tokenType = getTokenType(token);
        return "ACCESS".equals(tokenType);
    }

    public boolean isRefreshToken(String token) {
        String tokenType = getTokenType(token);
        return "REFRESH".equals(tokenType);
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("토큰 만료 시간 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private Claims parseToken(String token) {
        try {
            // 먼저 서명된 토큰으로 시도
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                log.debug("✅ 서명된 토큰 파싱 성공 - Subject: {}", claims.getSubject());
                return claims;
            } catch (UnsupportedJwtException e) {
                // 서명된 토큰이 아니면 서명 없는 토큰으로 시도
                log.debug("🔄 서명 없는 토큰으로 파싱 시도...");

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJwt(token)
                        .getBody();

                log.debug("✅ 서명 없는 토큰 파싱 성공 - Subject: {}", claims.getSubject());
                return claims;
            }
        } catch (Exception e) {
            log.error("❌ 토큰 파싱 실패: {}", e.getMessage());
            throw e;
        }
    }

    public void logTokenInfo(String token) {
        try {
            Claims claims = parseToken(token);
            log.debug("토큰 정보 - Subject: {}, Username: {}, Role: {}, Type: {}, Expiration: {}",
                    claims.getSubject(),
                    claims.get("username"),
                    claims.get("role"),
                    claims.get("type"),
                    claims.getExpiration());
        } catch (Exception e) {
            log.warn("토큰 정보 로깅 실패: {}", e.getMessage());
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
