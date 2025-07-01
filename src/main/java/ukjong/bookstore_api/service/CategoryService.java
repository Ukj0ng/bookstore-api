package ukjong.bookstore_api.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ukjong.bookstore_api.dto.request.CategoryRequest;
import ukjong.bookstore_api.dto.response.CategoryResponse;
import ukjong.bookstore_api.entity.Category;
import ukjong.bookstore_api.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(CategoryResponse::new).collect(Collectors.toList());
    }

    /**
     * 카테고리 상세 조회
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = findById(id);

        return new CategoryResponse(category);
    }

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        validateCategoryName(request.getName());

        Category category = createCategoryFromRequest(request);
        Category savedCategory = categoryRepository.save(category);

        log.info("새로운 카테고리 생성 완료 - ID: {}, Name: {}", savedCategory.getId(), savedCategory.getName());
        return new CategoryResponse(savedCategory);
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findById(id);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (!category.getName().equals(request.getName()) &&
                    categoryRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("이미 존재하는 카테고리명입니다");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("카테고리 수정 완료 - ID: {}, Name: {}", updatedCategory.getId(), updatedCategory.getName());
        return new CategoryResponse(updatedCategory);
    }

    /**
     * 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findById(id);

        if (!category.getBooks().isEmpty()) {
            throw new IllegalArgumentException("도서가 등록된 카테고리는 삭제할 수 없습니다");
        }

        categoryRepository.delete(category);
        log.info("카테고리 삭제 완료 - ID: {}, Name: {}", category.getId(), category.getName());
    }

    /**
     * 도서가 있는 카테고리만 조회
     */
    public List<CategoryResponse> getCategoriesWithBooks() {
        return categoryRepository.findCategoriesWithBooks()
                .stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 검색
     */
    public List<CategoryResponse> searchCategories(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 Entity 조회 (내부용)
     */
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));
    }

    private void validateCategoryName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + name);
        }
    }

    private Category createCategoryFromRequest(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }
}
