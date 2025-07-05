package ukjong.bookstore_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ukjong.bookstore_api.dto.request.UpdateUserRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.UserResponse;
import ukjong.bookstore_api.service.UserService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        log.info("프로필 조회 요청 - UserId: {}", userId);

        UserResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회 완료", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request
            ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        log.info("프로필 수정 요청 - UserId: {}", userId);

        UserResponse updatedProfile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 완료", updatedProfile));
    }
}
