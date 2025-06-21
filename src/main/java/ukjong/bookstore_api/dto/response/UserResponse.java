package ukjong.bookstore_api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import ukjong.bookstore_api.entity.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    // Entity에서 DTO로 변환하는 생성자
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt();
    }
}