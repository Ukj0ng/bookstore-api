package ukjong.bookstore_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(min = 2, max = 50, message = "카테고리명은 2-50자 사이여야 합니다")
    @Column(unique = true, nullable = false)
    private String name;

    @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계 - 카테고리에 속한 도서들
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    // 기본 생성자
    public Category() {
        this.createdAt = LocalDateTime.now();
    }

    // 생성자
    public Category(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // PreUpdate 콜백
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 연관관계 편의 메서드
    public void addBook(Book book) {
        books.add(book);
        book.setCategory(this);
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.setCategory(null);
    }
}