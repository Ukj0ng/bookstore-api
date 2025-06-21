package ukjong.bookstore_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(min = 2, max = 50, message = "카테고리명은 2-50자 사이여야 합니다")
    private String name;

    @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
    private String description;
}