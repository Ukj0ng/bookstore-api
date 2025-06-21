package ukjong.bookstore_api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ukjong.bookstore_api.entity.Book;
import ukjong.bookstore_api.entity.Category;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ISBN으로 조회
    Optional<Book> findByIsbn(String isbn);

    // 제목으로 검색 (부분 일치, 대소문자 무시)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 저자로 검색 (부분 일치, 대소문자 무시)
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // 카테고리별 도서 조회
    Page<Book> findByCategory(Category category, Pageable pageable);

    // 카테고리 ID로 도서 조회
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    // 재고가 있는 도서만 조회
    Page<Book> findByStockGreaterThan(Integer stock, Pageable pageable);

    // 가격 범위로 조회
    Page<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // 복합 검색 (제목 또는 저자)
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> findByTitleOrAuthorContaining(@Param("keyword") String keyword, Pageable pageable);

    // 고급 검색
    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "(:inStockOnly = false OR b.stock > 0)")
    Page<Book> findBooksWithFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStockOnly") boolean inStockOnly,
            Pageable pageable
    );

    // 베스트셀러 (재고 기준 상위 10개)
    List<Book> findTop10ByOrderByStockDesc();

    // 최신 도서 (등록일 기준 상위 10개)
    List<Book> findTop10ByOrderByCreatedAtDesc();
}