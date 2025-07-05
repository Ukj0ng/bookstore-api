package ukjong.bookstore_api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ukjong.bookstore_api.service.UserService;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserValidationInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자만 검증
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Long userId = (Long) auth.getPrincipal();

            // 사용자 유효성 검사 (탈퇴, 정지 등)
            if (!userService.isValidUser(userId)) {
                log.warn("유효하지 않은 사용자 접근 시도 - UserId: {}", userId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");

                String jsonResponse = String.format(
                        "{\"success\": false, \"message\": \"접근 권한이 없습니다.\", \"data\": null, \"timestamp\": \"%s\"}",
                        java.time.LocalDateTime.now().toString()
                );

                response.getWriter().write(jsonResponse);
                response.getWriter().flush();
                return false;
            }
        }

        return true;
    }
}