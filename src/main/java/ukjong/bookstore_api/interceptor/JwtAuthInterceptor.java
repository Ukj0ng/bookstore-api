package ukjong.bookstore_api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ukjong.bookstore_api.security.JwtTokenProvider;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("JWT 인증 인터셉터 시작 - URI: {}, Method: {}", requestURI, method);
        if (isPublicPath(requestURI, method)) {
            log.debug("공개 경로로 인증 생략 - URI: {}", requestURI);
            return true;
        }

        if ("OPTIONS".equals(method)) {
            return true;
        }

        try {
            String bearerToken = request.getHeader("Authorization");
            String token = jwtTokenProvider.resolveToken(bearerToken);

            if (token == null) {
                log.warn("토큰이 없음 - URI: {}, Authorization Header: {}", requestURI, bearerToken);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "인증 토큰이 필요합니다");
                return false;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("유효하지 않은 토큰 - URI: {}, Token: {}", requestURI, token.substring(0, Math.min(token.length(), 20)) + "...");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다");
                return false;
            }

            if (!jwtTokenProvider.isAccessToken(token)) {
                log.warn("Access Token이 아님 - URI: {}, Token Type: {}", requestURI, jwtTokenProvider.getTokenType(token));
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Access Token이 필요합니다");
                return false;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            request.setAttribute("token", token);

            log.debug("JWT 인증 성공 - URI: {}, UserId: {}, Username: {}, Role: {}",
                    requestURI, userId, username, role);

            return true;
        } catch (JwtTokenProvider.InvalidTokenException e) {
            log.warn("토큰 검증 실패 - URI: {}, Error: {}", requestURI, e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생 - URI: {}", requestURI, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다");
            return false;
        }
    }

    private boolean isPublicPath(String requestURI, String method) {
        if (requestURI.equals("/api/auth/register") ||
                requestURI.equals("/api/auth/login") ||
                requestURI.equals("/api/auth/check-username") ||
                requestURI.equals("/api/auth/check-email")) {
            return true;
        }

        if ("GET".equals(method)) {
            if (requestURI.equals("/api/books") ||
                    requestURI.startsWith("/api/books/") && requestURI.matches("/api/books/\\d+") ||
                    requestURI.equals("/api/books/search") ||
                    requestURI.startsWith("/api/books/category/") ||
                    requestURI.equals("/api/books/filter") ||
                    requestURI.equals("/api/books/bestsellers") ||
                    requestURI.equals("/api/books/latest")) {
                return true;
            }

            if (requestURI.equals("/api/categories") ||
                    requestURI.startsWith("/api/categories/") && requestURI.matches("/api/categories/\\d+")) {
                return true;
            }
        }

        if (requestURI.equals("/health") ||
                requestURI.equals("/actuator/health") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/v3/api-docs")) {
            return true;
        }

        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"data\": null, \"timestamp\": \"%s\"}",
                message,
                java.time.LocalDateTime.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.debug("JWT 인증 인터셉터 완료 - URI: {}, Status: {}", request.getRequestURI(), response.getStatus());
    }
}
