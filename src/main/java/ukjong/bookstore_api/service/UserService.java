package ukjong.bookstore_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ukjong.bookstore_api.dto.request.LoginRequest;
import ukjong.bookstore_api.dto.request.RegisterRequest;
import ukjong.bookstore_api.dto.request.UpdateUserRequest;
import ukjong.bookstore_api.dto.response.AuthResponse;
import ukjong.bookstore_api.dto.response.UserResponse;
import ukjong.bookstore_api.entity.User;
import ukjong.bookstore_api.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserResponse registerUser(RegisterRequest request) {
        User savedUser = null;

        return new UserResponse();
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
        return null;
    }

    public UserResponse getUserProfile(Long userId) {
        return null;
    }

    public UserResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        return null;
    }

    public boolean existsByUsername(String username) {
        return false;
    }

    public boolean existsByEmail(String email) {
        return false;
    }

    public User findById(Long userId) {
        return null;
    }
}
