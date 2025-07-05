package ukjong.bookstore_api.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ukjong.bookstore_api.dto.request.CategoryRequest;
import ukjong.bookstore_api.dto.response.CategoryResponse;
import ukjong.bookstore_api.entity.Category;
import ukjong.bookstore_api.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            log.debug("전체 카테고리 조회 완료 - 총 {}개", categories.size());
            return categories.stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("전체 카테고리 조회 중 오류 발생", e);
            throw new RuntimeException("카테고리 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리 상세 조회
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        try {
            Category category = findById(id);
            log.debug("카테고리 상세 조회 완료 - ID: {}, Name: {}", id, category.getName());
            return new CategoryResponse(category);
        } catch (Exception e) {
            log.error("카테고리 상세 조회 중 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("카테고리 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        try {
            validateCategoryName(request.getName());

            Category category = createCategoryFromRequest(request);
            Category savedCategory = categoryRepository.save(category);

            log.info("새로운 카테고리 생성 완료 - ID: {}, Name: {}", savedCategory.getId(), savedCategory.getName());
            return new CategoryResponse(savedCategory);
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 생성 실패 - 요청 데이터 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("카테고리 생성 중 시스템 오류 발생 - Name: {}", request.getName(), e);
            throw new RuntimeException("카테고리 생성 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        try {
            Category category = findById(id);

            log.info("카테고리 수정 요청 - ID: {}, 기존 Name: '{}' -> 새 Name: '{}'",
                    id, category.getName(), request.getName());

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                if (!category.getName().equals(request.getName().trim()) &&
                        categoryRepository.existsByName(request.getName().trim())) {
                    throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + request.getName());
                }
                category.setName(request.getName().trim());
            }

            if (request.getDescription() != null) {
                category.setDescription(request.getDescription().trim());
            }

            Category updatedCategory = categoryRepository.save(category);
            log.info("카테고리 수정 완료 - ID: {}, Name: {}", updatedCategory.getId(), updatedCategory.getName());
            return new CategoryResponse(updatedCategory);
        } catch (Exception e) {
            log.error("카테고리 수정 중 시스템 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("카테고리 수정 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long id) {
        try {
            Category category = findById(id);

            log.info("카테고리 삭제 요청 - ID: {}, Name: '{}', 연결된 도서 수: {}",
                    id, category.getName(), category.getBooks().size());

            if (!category.getBooks().isEmpty()) {
                throw new IllegalArgumentException("도서가 등록된 카테고리는 삭제할 수 없습니다. 연결된 도서 수: " + category.getBooks().size());
            }

            categoryRepository.delete(category);
            log.info("카테고리 삭제 완료 - ID: {}, Name: {}", id, category.getName());
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("카테고리 삭제 중 시스템 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("카테고리 삭제 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 도서가 있는 카테고리만 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesWithBooks() {
        try {
            List<Category> categories = categoryRepository.findCategoriesWithBooks();
            log.debug("도서가 있는 카테고리 조회 완료 - 총 {}개", categories.size());
            return categories.stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("도서가 있는 카테고리 조회 중 오류 발생", e);
            throw new RuntimeException("카테고리 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리 검색
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("검색어를 입력해주세요");
            }

            List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name.trim());
            log.debug("카테고리 검색 완료 - 검색어: '{}', 결과: {}개", name.trim(), categories.size());

            return categories.stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("카테고리 검색 실패 - 검색어: '{}', 오류: {}", name, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("카테고리 검색 중 오류 발생 - 검색어: '{}'", name, e);
            throw new RuntimeException("카테고리 검색 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리 Entity 조회 (내부용)
     */
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));
    }

    public boolean existsById(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    private void validateCategoryName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + name);
        }
    }

    private Category createCategoryFromRequest(CategoryRequest request) {
        LocalDateTime now = LocalDateTime.now();

        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
