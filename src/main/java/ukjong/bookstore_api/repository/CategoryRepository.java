package ukjong.bookstore_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ukjong.bookstore_api.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 카테고리명으로 조회
    Optional<Category> findByName(String name);

    // 카테고리명 존재 여부 확인
    boolean existsByName(String name);

    // 도서가 있는 카테고리만 조회
    @Query("SELECT c FROM Category c WHERE SIZE(c.books) > 0")
    List<Category> findCategoriesWithBooks();

    // 카테고리명으로 검색 (부분 일치)
    List<Category> findByNameContainingIgnoreCase(String name);
}