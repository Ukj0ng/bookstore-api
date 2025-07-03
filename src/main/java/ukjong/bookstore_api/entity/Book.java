package ukjong.bookstore_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 200, message = "제목은 1-200자 사이여야 합니다")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "저자는 필수입니다")
    @Size(min = 1, max = 100, message = "저자는 1-100자 사이여야 합니다")
    @Column(nullable = false)
    private String author;

    @Size(max = 50, message = "ISBN은 50자를 초과할 수 없습니다")
    @Column(unique = true)
    private String isbn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
    @Digits(integer = 10, fraction = 2, message = "가격 형식이 올바르지 않습니다")
    private BigDecimal price;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Size(max = 100, message = "출판사는 100자를 초과할 수 없습니다")
    private String publisher;

    @Min(value = 1, message = "페이지 수는 1 이상이어야 합니다")
    @Column(name = "page_count")
    private Integer pageCount;

    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계 - 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // PreUpdate 콜백
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public boolean isInStock() {
        return this.stock > 0;
    }
}