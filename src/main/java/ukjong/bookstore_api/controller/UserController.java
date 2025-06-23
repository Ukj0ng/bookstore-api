package ukjong.bookstore_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ukjong.bookstore_api.dto.request.UpdateUserRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.UserResponse;
import ukjong.bookstore_api.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            HttpServletRequest request) {
        Long id = (Long) request.getAttribute("userId");

        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }
        UserResponse profile = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest
            ) {
        Long id = (Long) httpRequest.getAttribute("userId");

        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }
        UserResponse updateUserProfile = userService.updateUserProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success(updateUserProfile));
    }
}
