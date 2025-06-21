package ukjong.bookstore_api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import ukjong.bookstore_api.entity.Book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private LocalDate publicationDate;
    private String publisher;
    private Integer pageCount;
    private String imageUrl;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity에서 DTO로 변환하는 생성자
    public BookResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.description = book.getDescription();
        this.price = book.getPrice();
        this.stock = book.getStock();
        this.publicationDate = book.getPublicationDate();
        this.publisher = book.getPublisher();
        this.pageCount = book.getPageCount();
        this.imageUrl = book.getImageUrl();
        this.category = book.getCategory() != null ? new CategoryResponse(book.getCategory()) : null;
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
    }
}