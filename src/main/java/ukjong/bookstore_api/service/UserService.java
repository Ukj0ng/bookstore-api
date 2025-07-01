package ukjong.bookstore_api.service;

import jakarta.transaction.Transactional;
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
import ukjong.bookstore_api.repository.UserRepository;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        validateUserRegistration(request);

        User user = createUserFromRequest(request);
        User savedUser = userRepository.save(user);

        log.info("새로운 사용자 등록 완료 - ID: {}, Username: {}", savedUser.getId(), savedUser.getUsername());
        return new UserResponse(savedUser);
    }

    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        String token = "temporary-jwt-token";

        return new AuthResponse(token, "Bearer", new UserResponse(user));
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
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();
    }
}
