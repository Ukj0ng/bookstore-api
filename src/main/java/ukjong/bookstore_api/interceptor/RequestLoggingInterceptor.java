package ukjong.bookstore_api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 요청 시작 시간 기록
        request.setAttribute("startTime", System.currentTimeMillis());

        // 인증된 사용자 정보 로깅
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userInfo = "익명";

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Long userId = (Long) auth.getPrincipal();
            String username = (String) request.getAttribute("username");
            String role = (String) request.getAttribute("role");
            userInfo = String.format("ID:%d, Username:%s, Role:%s", userId, username, role);
        }

        log.info("🚀 API 요청 시작 - Method: {}, URI: {}, User: {}", method, requestURI, userInfo);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        String requestURI = request.getRequestURI();
        int status = response.getStatus();

        if (ex != null) {
            log.error("❌ API 완료 (오류) - URI: {}, Status: {}, Duration: {}ms, Error: {}",
                    requestURI, status, duration, ex.getMessage());
        } else {
            log.info("✅ API 완료 - URI: {}, Status: {}, Duration: {}ms", requestURI, status, duration);
        }
    }
}
