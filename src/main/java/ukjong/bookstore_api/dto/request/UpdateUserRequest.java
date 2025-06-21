package ukjong.bookstore_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;
}