package ukjong.bookstore_api.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ukjong.bookstore_api.dto.request.LoginRequest;
import ukjong.bookstore_api.dto.request.RegisterRequest;
import ukjong.bookstore_api.dto.request.UpdateUserRequest;
import ukjong.bookstore_api.dto.response.AuthResponse;
import ukjong.bookstore_api.dto.response.UserResponse;
import ukjong.bookstore_api.entity.User;
import ukjong.bookstore_api.exception.UnauthorizedException;
import ukjong.bookstore_api.exception.UserNotFoundException;
import ukjong.bookstore_api.repository.UserRepository;
import ukjong.bookstore_api.security.JwtTokenProvider;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        validateUserRegistration(request);

        User user = createUserFromRequest(request);
        User savedUser = userRepository.save(user);

        log.info("새로운 사용자 등록 완료 - ID: {}, Username: {}", savedUser.getId(), savedUser.getUsername());
        return new UserResponse(savedUser);
    }

    public AuthResponse loginUser(LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));

            if (!encoder.matches(request.getPassword(), user.getPassword())) {
                throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
            }

            log.info("🎫 토큰 생성 시작...");
            String accessToken = jwtTokenProvider.createAccessToken(user);
            log.info("✅ Access Token 생성 완료 - 길이: {}", accessToken.length());
            String refreshToken = jwtTokenProvider.createRefreshToken(user);
            log.info("✅ Refresh Token 생성 완료 - 길이: {}", refreshToken.length());

            log.info("사용자 로그인 성공 - ID: {}, Username: {}", user.getId(), user.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .user(new UserResponse(user))
                    .build();

        } catch (UnauthorizedException e) {
            log.warn("로그인 실패 - Username: {}, Reason: {}", request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - Username: {}", request.getUsername(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
            }

            if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new UnauthorizedException("리프레시 토큰이 아닙니다.");
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = findById(userId);

            String newAccessToken = jwtTokenProvider.createAccessToken(user);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

            log.info("토큰 재발급 완료 - UserId: {}, Username: {}", user.getId(), user.getUsername());

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .user(new UserResponse(user))
                    .build();

        } catch (UnauthorizedException e) {
            log.warn("토큰 재발급 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("토큰 재발급 중 오류 발생", e);
            throw new RuntimeException("토큰 재발급 중 오류가 발생했습니다.", e);
        }
    }

    public UserResponse getUserProfile(Long userId) {
        User user = findById(userId);
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = findById(userId);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }
            user.setEmail(request.getEmail());
        }


        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser);
    }

    public boolean isValidUser(Long userId) {
        try {
            findById(userId);
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    public boolean hasAdminRole(Long userId) {
        try {
            User user = findById(userId);
            return user.getRole() == User.Role.ADMIN;
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
    }

    private void validateUserRegistration(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
    }

    private User createUserFromRequest(RegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
