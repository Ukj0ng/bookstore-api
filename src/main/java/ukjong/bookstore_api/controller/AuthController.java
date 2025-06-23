package ukjong.bookstore_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ukjong.bookstore_api.dto.request.LoginRequest;
import ukjong.bookstore_api.dto.request.RegisterRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.AuthResponse;
import ukjong.bookstore_api.dto.response.UserResponse;
import ukjong.bookstore_api.service.UserService;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private final UserService userService;

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
}
