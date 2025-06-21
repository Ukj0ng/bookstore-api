package ukjong.bookstore_api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import ukjong.bookstore_api.entity.Book;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookListResponse {

    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String categoryName;
    private LocalDateTime createdAt;

    // Entity에서 DTO로 변환하는 생성자 (목록용 - 간단한 정보만)
    public BookListResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.price = book.getPrice();
        this.stock = book.getStock();
        this.imageUrl = book.getImageUrl();
        this.categoryName = book.getCategory() != null ? book.getCategory().getName() : null;
        this.createdAt = book.getCreatedAt();
    }
}

