package ukjong.bookstore_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ukjong.bookstore_api.interceptor.RequestLoggingInterceptor;
import ukjong.bookstore_api.interceptor.UserValidationInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final UserValidationInterceptor userValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 요청 로깅 인터셉터 (모든 API 요청)
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        // 2. 사용자 검증 인터셉터 (인증이 필요한 API만)
        registry.addInterceptor(userValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/books/**",      // GET 요청은 Spring Security에서 허용
                        "/api/categories/**"  // GET 요청은 Spring Security에서 허용
                )
                .order(2);
    }
}