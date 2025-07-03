package ukjong.bookstore_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ukjong.bookstore_api.dto.request.CategoryRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.CategoryResponse;
import ukjong.bookstore_api.entity.User;
import ukjong.bookstore_api.exception.ForbiddenException;
import ukjong.bookstore_api.exception.UnauthorizedException;
import ukjong.bookstore_api.service.CategoryService;
import ukjong.bookstore_api.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final UserService userService;
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
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                throw new ForbiddenException("관리자 권한이 필요합니다.");
            }

            CategoryResponse response = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("카테고리가 생성되었습니다.", response));
        } catch (UnauthorizedException | ForbiddenException e) {
            throw e; // GlobalExceptionHandler에서 처리
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 생성 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                throw new ForbiddenException("관리자 권한이 필요합니다.");
            }

            CategoryResponse response = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("카테고리가 수정되었습니다.", response));
        } catch (UnauthorizedException | ForbiddenException e) {
            throw e; // GlobalExceptionHandler에서 처리
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("카테고리 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                throw new ForbiddenException("관리자 권한이 필요합니다.");
            }

            categoryService.deleteCategory(id);

            return ResponseEntity.ok(ApiResponse.success("카테고리 삭제가 완료되었습니다.", null));
        } catch (UnauthorizedException | ForbiddenException e) {
            throw e; // GlobalExceptionHandler에서 처리
        } catch (Exception e) {
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