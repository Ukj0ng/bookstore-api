package ukjong.bookstore_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ukjong.bookstore_api.dto.request.CategoryRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.CategoryResponse;
import ukjong.bookstore_api.service.CategoryService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long id) {
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();

            log.info("카테고리 생성 요청 - UserId: {}, Name: {}", userId, request.getName());

            CategoryResponse response = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("카테고리가 생성되었습니다.", response));
        } catch (Exception e) {
            log.error("카테고리 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 생성 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();

            log.info("카테고리 수정 요청 - UserId: {}, CategoryId: {}", userId, id);

            CategoryResponse response = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("카테고리가 수정되었습니다.", response));
        } catch (Exception e) {
            log.error("카테고리 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();

            log.info("카테고리 삭제 요청 - UserId: {}, CategoryId: {}", userId, id);

            categoryService.deleteCategory(id);

            return ResponseEntity.ok(ApiResponse.success("카테고리 삭제가 완료되었습니다.", null));
        } catch (Exception e) {
            log.error("카테고리 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 삭제 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/with-books")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesWithBooks() {
        try {
            List<CategoryResponse> categories = categoryService.getCategoriesWithBooks();
            return ResponseEntity.ok(ApiResponse.success("도서가 있는 카테고리 조회 완료", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(
            @RequestParam("name") String name) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("검색어를 입력해주세요."));
        }

        try {
            List<CategoryResponse> categories = categoryService.searchCategories(name);
            String message = categories.isEmpty() ? "검색 결과가 없습니다." : "검색 완료";
            return ResponseEntity.ok(ApiResponse.success(message, categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 검색 중 오류가 발생했습니다."));
        }
    }
}