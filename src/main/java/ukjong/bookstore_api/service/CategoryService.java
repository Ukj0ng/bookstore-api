package ukjong.bookstore_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ukjong.bookstore_api.dto.request.CategoryRequest;
import ukjong.bookstore_api.dto.response.CategoryResponse;
import ukjong.bookstore_api.entity.Category;
import ukjong.bookstore_api.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return null;
    }

    /**
     * 카테고리 상세 조회
     */
    public CategoryResponse getCategoryById(Long id) {
        return null;
    }

    /**
     * 카테고리 생성
     */
    public CategoryResponse createCategory(CategoryRequest request) {
        return null;
    }

    /**
     * 카테고리 수정
     */
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        return null;
    }

    /**
     * 카테고리 삭제
     */
    public void deleteCategory(Long id) {

    }

    /**
     * 도서가 있는 카테고리만 조회
     */
    public List<CategoryResponse> getCategoriesWithBooks() {
        return null;
    }

    /**
     * 카테고리 검색
     */
    public List<CategoryResponse> searchCategories(String name) {
        return null;
    }

    /**
     * 카테고리 Entity 조회 (내부용)
     */
    public Category findById(Long categoryId) {
        return null;
    }
}
