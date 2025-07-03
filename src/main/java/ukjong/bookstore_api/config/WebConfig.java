package ukjong.bookstore_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ukjong.bookstore_api.interceptor.JwtAuthInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 인증이 필요 없는 경로들 (인터셉터에서도 처리하지만 성능상 제외)
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/check-username",
                        "/api/auth/check-email",
                        "/api/auth/refresh",  // 토큰 재발급

                        // 정적 리소스 및 문서
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/health",
                        "/actuator/**"
                );
    }
}
