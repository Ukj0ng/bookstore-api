package ukjong.bookstore_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ukjong.bookstore_api.dto.request.BookFilterRequest;
import ukjong.bookstore_api.dto.request.BookRequest;
import ukjong.bookstore_api.dto.response.ApiResponse;
import ukjong.bookstore_api.dto.response.BookListResponse;
import ukjong.bookstore_api.dto.response.BookResponse;
import ukjong.bookstore_api.dto.response.PageResponse;
import ukjong.bookstore_api.entity.User;
import ukjong.bookstore_api.service.BookService;
import ukjong.bookstore_api.service.UserService;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> getAllBooks(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
            ) {
        try {
            PageResponse<BookListResponse> response = bookService.getAllBooks(pageable);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @PathVariable Long id
    ) {
        try {
            BookResponse bookResponse = bookService.getBookById(id);
            return ResponseEntity.ok(ApiResponse.success(bookResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody BookRequest request,
            HttpServletRequest httpRequest
            ) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("관리자 권한이 필요합니다."));
            }

            BookResponse createResult = bookService.createBook(request);

            return ResponseEntity.created(URI.create("/api/books/" + createResult.getId()))
                    .body(ApiResponse.success("도서가 등록되었습니다.", createResult));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("관리자 권한이 필요합니다."));
            }

            BookResponse updateResult = bookService.updateBook(id, request);

            return ResponseEntity.ok(ApiResponse.success("도서가 수정되었습니다.", updateResult));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("관리자 권한이 필요합니다."));
            }

            bookService.deleteBook(id);

            return ResponseEntity.ok(ApiResponse.success("도서가 삭제되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> searchBooks(
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("검색어를 입력해주세요."));
        }

        try {
            PageResponse<BookListResponse> searchResult = bookService.searchBooks(keyword, pageable);

            String message = searchResult.getTotalElements() > 0
                    ? "검색 결과를 찾았습니다."
                    : "검색 결과가 없습니다.";

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(message, searchResult));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("검색 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> getBookByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        try {
            PageResponse<BookListResponse> result = bookService.getBooksByCategory(categoryId, pageable);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> filterBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        try {
            BookFilterRequest filter = new BookFilterRequest();
            filter.setTitle(title);
            filter.setAuthor(author);
            filter.setCategoryId(categoryId);
            filter.setSortBy(sortBy);
            filter.setSortDirection(sortDirection);
            filter.setInStockOnly(inStockOnly);

            if (minPrice != null && !minPrice.trim().isEmpty()) {
                filter.setMinPrice(new BigDecimal(minPrice));
            }

            if (maxPrice != null && !maxPrice.trim().isEmpty()) {
                filter.setMaxPrice(new BigDecimal(maxPrice));
            }

            if (filter.getMinPrice() != null && filter.getMaxPrice() != null) {
                if (filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("최소 가격이 최대 가격보다 클 수 없습니다."));
                }
            }

            if (filter.getMinPrice() != null && filter.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("가격은 0 이상이어야 합니다."));
            }

            if (filter.getMaxPrice() != null && filter.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("가격은 0 이상이어야 합니다."));
            }

            PageResponse<BookListResponse> result = bookService.filterBooks(filter, pageable);

            String message = result.getTotalElements() > 0
                    ? String.format("%d개의 도서를 찾았습니다.", result.getTotalElements())
                    : "조건에 맞는 도서가 없습니다.";

            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("가격 형식이 올바르지 않습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("필터링 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<List<BookListResponse>>> getBestSellers() {
        try {
            List<BookListResponse> result = bookService.getBestSellers();

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<BookListResponse>>> getLatestBooks() {
        try {
            List<BookListResponse> result = bookService.getLatestBooks();

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<BookResponse>> updateBookStock(
            @PathVariable Long id,
            @RequestParam("quantity") int quantity,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        if (quantity < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("재고는 0 이상이어야 합니다."));
        }

        try {
            User user = userService.findById(userId);

            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("관리자 권한이 필요합니다."));
            }

            BookResponse result = bookService.updateBookStock(id, quantity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("재고가 변경되었습니다.", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
