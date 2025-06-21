package ukjong.bookstore_api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import ukjong.bookstore_api.entity.Category;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private int bookCount;
    private LocalDateTime createdAt;

    // Entity에서 DTO로 변환하는 생성자
    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.bookCount = category.getBooks().size();
        this.createdAt = category.getCreatedAt();
    }
}