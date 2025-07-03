package ukjong.bookstore_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ukjong.bookstore_api.dto.request.LoginRequest;
import ukjong.bookstore_api.dto.request.RegisterRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.AuthResponse;
import ukjong.bookstore_api.dto.response.UserResponse;
import ukjong.bookstore_api.exception.UnauthorizedException;
import ukjong.bookstore_api.security.JwtTokenProvider;
import ukjong.bookstore_api.service.UserService;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse userResponse = userService.registerUser(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = userService.loginUser(request);

            return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody Map<String, String> request
    ) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("리프레시 토큰이 필요합니다."));
            }

            AuthResponse authResponse = userService.refreshToken(refreshToken);

            return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", authResponse));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("토큰 재발급 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 실제로는 토큰을 블랙리스트에 추가하거나 Redis에서 제거하는 로직이 필요
            // 현재는 클라이언트에서 토큰을 제거하도록 안내

            String token = jwtTokenProvider.resolveToken(authorization);
            if (token != null) {
                // 향후 토큰 블랙리스트 기능 구현 시 사용
                // tokenBlacklistService.addToBlacklist(token);
            }

            return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(
            @RequestBody Map<String, String> request
    ) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자명을 입력해주세요."));
        }
        boolean exists = userService.existsByUsername(username);
        boolean available = !exists;

        String message = available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("이메일을 입력해주세요."));
        }

        if (!email.contains("@") || !email.contains(".")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("올바른 이메일 형식이 아닙니다."));
        }

        boolean exists = userService.existsByEmail(email);
        boolean available = !exists;

        String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            String token = jwtTokenProvider.resolveToken(authorization);

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("토큰이 없습니다."));
            }

            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("유효하지 않은 토큰입니다."));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            Map<String, Object> tokenInfo = Map.of(
                    "userId", userId,
                    "username", username,
                    "role", role,
                    "valid", true);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.error("토큰 검증 중 오류가 발생했습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("토큰 검증 중 오류가 발생했습니다."));
        }
    }
}
