package ukjong.bookstore_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ukjong.bookstore_api.constants.BookConstants;
import ukjong.bookstore_api.dto.request.BookFilterRequest;
import ukjong.bookstore_api.dto.request.BookRequest;
import ukjong.bookstore_api.dto.response.BookListResponse;
import ukjong.bookstore_api.dto.response.BookResponse;
import ukjong.bookstore_api.dto.response.PageResponse;
import ukjong.bookstore_api.entity.Book;
import ukjong.bookstore_api.entity.Category;
import ukjong.bookstore_api.exception.BookNotFoundException;
import ukjong.bookstore_api.exception.DuplicateBookException;
import ukjong.bookstore_api.exception.InvalidBookDataException;
import ukjong.bookstore_api.exception.InsufficientStockException;
import ukjong.bookstore_api.repository.BookRepository;
import ukjong.bookstore_api.validator.BookValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final BookValidator bookValidator;

    /**
     * 전체 도서 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public PageResponse<BookListResponse> getAllBooks(Pageable pageable) {
        try {
            bookValidator.validatePageable(pageable);

            log.debug("도서 목록 조회 요청 - 페이지: {}, 크기: {}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Book> bookPage = bookRepository.findAll(pageable);
            Page<BookListResponse> responsePage = bookPage.map(BookListResponse::new);

            log.debug("도서 목록 조회 완료 - 총 {}개 중 {}개 조회",
                    responsePage.getTotalElements(), responsePage.getNumberOfElements());

            return new PageResponse<>(responsePage);
        } catch (Exception e) {
            log.error("도서 목록 조회 중 오류 발생", e);
            throw new RuntimeException("도서 목록 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 도서 상세 조회
     */
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = findById(id);
        return new BookResponse(book);
    }

    /**
     * 도서 등록
     */
    @Transactional
    public BookResponse createBook(BookRequest request) {
        try {
            // 입력 검증
            bookValidator.validateCreateBookRequest(request);

            // 비즈니스 규칙 검증
            validateBookBusinessRules(request);

            Category category = validateAndGetCategory(request.getCategoryId());

            log.info("도서 등록 요청 - 제목: '{}', 저자: '{}', ISBN: '{}', 카테고리: '{}'",
                    request.getTitle(), request.getAuthor(), request.getIsbn(), category.getName());

            Book book = createBookFromRequest(request, category);
            Book savedBook = bookRepository.save(book);

            updateCategoryBookRelation(category, savedBook);

            log.info("도서 등록 완료 - ID: {}, 제목: '{}', 저자: '{}', 카테고리: '{}'",
                    savedBook.getId(), savedBook.getTitle(), savedBook.getAuthor(), category.getName());

            return new BookResponse(savedBook);
        } catch (InvalidBookDataException | DuplicateBookException e) {
            log.warn("도서 등록 요청 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("도서 등록 중 시스템 오류 발생 - 제목: '{}'", request != null ? request.getTitle() : "N/A", e);
            throw new RuntimeException("도서 등록 중 시스템 오류가 발생했습니다", e);
        }
    }

    /**
     * 도서 수정
     */
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        try {
            bookValidator.validateUpdateBookRequest(id, request);

            Book existingBook = findById(id);

            log.info("도서 수정 요청 - ID: {}, 기존 제목: '{}' -> 새 제목: '{}'",
                    id, existingBook.getTitle(), request.getTitle());

            // 비즈니스 규칙 검증
            validateUpdateBookBusinessRules(existingBook, request);

            // 카테고리 변경 시 검증
            Category newCategory = null;
            if (request.getCategoryId() != null &&
                    !request.getCategoryId().equals(existingBook.getCategory().getId())) {
                newCategory = validateAndGetCategory(request.getCategoryId());
            }

            // 도서 정보 업데이트
            updateBookFields(existingBook, request, newCategory);

            Book updatedBook = bookRepository.save(existingBook);

            log.info("도서 수정 완료 - ID: {}, 제목: '{}', 저자: '{}'",
                    updatedBook.getId(), updatedBook.getTitle(), updatedBook.getAuthor());

            return new BookResponse(updatedBook);

        } catch (BookNotFoundException | InvalidBookDataException | DuplicateBookException e) {
            log.warn("도서 수정 요청 오류 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("도서 수정 중 시스템 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("도서 수정 중 시스템 오류가 발생했습니다", e);
        }
    }

    /**
     * 도서 삭제
     */
    @Transactional
    public void deleteBook(Long id) {
        try {
            bookValidator.validateId(id, "도서 ID");

            Book book = findById(id);

            log.info("도서 삭제 요청 - ID: {}, 제목: '{}', 저자: '{}'",
                    id, book.getTitle(), book.getAuthor());

            validateBookDeletionRules(book);

            // 카테고리에서 도서 제거
            Category category = book.getCategory();
            if (category != null) {
                category.removeBook(book);
            }

            bookRepository.delete(book);

            log.info("도서 삭제 완료 - ID: {}, 제목: '{}'", id, book.getTitle());

        } catch (BookNotFoundException e) {
            log.warn("도서 삭제 요청 오류 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("도서 삭제 중 시스템 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("도서 삭제 중 시스템 오류가 발생했습니다", e);
        }
    }

    /**
     * 도서 검색 (제목, 저자)
     */
    @Transactional(readOnly = true)
    public PageResponse<BookListResponse> searchBooks(String keyword, Pageable pageable) {
        try {
            bookValidator.validateSearchKeyword(keyword);
            bookValidator.validatePageable(pageable);

            log.debug("도서 검색 요청 - 키워드: '{}', 페이지: {}, 크기: {}",
                    keyword.trim(), pageable.getPageNumber(), pageable.getPageSize());

            Page<Book> bookPage = bookRepository.findByTitleOrAuthorContaining(keyword.trim(), pageable);
            Page<BookListResponse> responsePage = bookPage.map(BookListResponse::new);

            log.debug("도서 검색 완료 - 키워드: '{}', 총 {}개 중 {}개 조회",
                    keyword.trim(), responsePage.getTotalElements(), responsePage.getNumberOfElements());

            return new PageResponse<>(responsePage);

        } catch (InvalidBookDataException e) {
            log.warn("도서 검색 요청 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("도서 검색 중 오류 발생 - 키워드: '{}'", keyword, e);
            throw new RuntimeException("도서 검색 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 카테고리별 도서 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<BookListResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        try {
            bookValidator.validateId(categoryId, "카테고리 ID");
            bookValidator.validatePageable(pageable);

            Category category = categoryService.findById(categoryId);

            log.debug("카테고리별 도서 조회 요청 - 카테고리: '{}' (ID: {}), 페이지: {}, 크기: {}",
                    category.getName(), categoryId, pageable.getPageNumber(), pageable.getPageSize());

            Page<Book> bookPage = bookRepository.findByCategoryId(categoryId, pageable);

            if (bookPage.isEmpty()) {
                log.info("카테고리 '{}' (ID: {})에 등록된 도서가 없습니다", category.getName(), categoryId);
            }

            Page<BookListResponse> responsePage = bookPage.map(BookListResponse::new);

            log.debug("카테고리별 도서 조회 완료 - 카테고리: '{}', 총 {}개 중 {}개 조회, 전체 페이지: {}",
                    category.getName(), responsePage.getTotalElements(),
                    responsePage.getNumberOfElements(), responsePage.getTotalPages());

            return new PageResponse<>(responsePage);
        } catch (Exception e) {
            log.error("카테고리별 도서 조회 중 오류 발생 - 카테고리 ID: {}", categoryId, e);
            throw new RuntimeException("카테고리별 도서 조회 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 고급 필터링
     */
    @Transactional(readOnly = true)
    public PageResponse<BookListResponse> filterBooks(BookFilterRequest filter, Pageable pageable) {
        try {
            bookValidator.validateFilterRequest(filter, pageable);

            log.debug("도서 필터링 요청 - 필터: {}, 페이지: {}, 크기: {}",
                    filter, pageable.getPageNumber(), pageable.getPageSize());

            Sort sort = createSortFromFilter(filter, pageable.getSort());
            Pageable pageableWithSort = createPageableWithSort(pageable, sort);

            Page<Book> bookPage = bookRepository.findBooksWithFilters(
                    filter.getTitle(),
                    filter.getAuthor(),
                    filter.getCategoryId(),
                    filter.getMinPrice(),
                    filter.getMaxPrice(),
                    filter.getInStockOnly() != null ? filter.getInStockOnly() : false,
                    pageableWithSort
            );

            Page<BookListResponse> responsePage = bookPage.map(BookListResponse::new);

            log.debug("도서 필터링 완료 - 총 {}개 중 {}개 조회, 필터 조건: {}",
                    responsePage.getTotalElements(), responsePage.getNumberOfElements(), filter);

            return new PageResponse<>(responsePage);

        } catch (InvalidBookDataException e) {
            log.warn("도서 필터링 요청 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("도서 필터링 중 오류 발생 - 필터: {}", filter, e);
            throw new RuntimeException("도서 필터링 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 베스트셀러 목록
     */
    @Transactional(readOnly = true)
    public List<BookListResponse> getBestSellers() {
        return bookRepository.findTop10ByOrderByStockDesc()
                .stream()
                .map(BookListResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 최신 도서 목록
     */
    @Transactional(readOnly = true)
    public List<BookListResponse> getLatestBooks() {
        return bookRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(BookListResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 재고 업데이트
     */
    @Transactional
    public BookResponse updateBookStock(Long id, int quantity) {
        try {
            bookValidator.validateStockUpdate(id, quantity);

            Book book = findById(id);

            log.info("도서 재고 업데이트 요청 - ID: {}, 제목: '{}', 기존 재고: {} -> 새 재고: {}",
                    id, book.getTitle(), book.getStock(), quantity);

            book.setStock(quantity);
            Book updatedBook = bookRepository.save(book);

            log.info("도서 재고 업데이트 완료 - ID: {}, 제목: '{}', 재고: {}",
                    updatedBook.getId(), updatedBook.getTitle(), updatedBook.getStock());

            return new BookResponse(updatedBook);

        } catch (BookNotFoundException | InvalidBookDataException e) {
            log.warn("재고 업데이트 요청 오류 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("재고 업데이트 중 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("재고 업데이트 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 재고 증가
     */
    @Transactional
    public BookResponse increaseStock(Long id, int quantity) {
        try {
            bookValidator.validateStockChange(id, quantity, "증가");

            Book book = findById(id);
            int oldStock = book.getStock();
            int newStock = oldStock + quantity;

            if (newStock > BookConstants.MAX_STOCK) {
                throw new InvalidBookDataException("재고는 " + BookConstants.MAX_STOCK + "개를 초과할 수 없습니다");
            }

            log.info("도서 재고 증가 요청 - ID: {}, 제목: '{}', 재고: {} + {} = {}",
                    id, book.getTitle(), oldStock, quantity, newStock);

            book.setStock(newStock);
            Book updatedBook = bookRepository.save(book);

            log.info("도서 재고 증가 완료 - ID: {}, 제목: '{}', 재고: {}",
                    updatedBook.getId(), updatedBook.getTitle(), updatedBook.getStock());

            return new BookResponse(updatedBook);

        } catch (BookNotFoundException | InvalidBookDataException e) {
            log.warn("재고 증가 요청 오류 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("재고 증가 중 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("재고 증가 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 재고 감소
     */
    @Transactional
    public BookResponse decreaseStock(Long id, int quantity) {
        try {
            bookValidator.validateStockChange(id, quantity, "감소");

            Book book = findById(id);
            int oldStock = book.getStock();
            int newStock = oldStock - quantity;

            if (newStock < 0) {
                throw new InsufficientStockException("재고가 부족합니다. 현재 재고: " + oldStock + ", 요청 수량: " + quantity);
            }

            log.info("도서 재고 감소 요청 - ID: {}, 제목: '{}', 재고: {} - {} = {}",
                    id, book.getTitle(), oldStock, quantity, newStock);

            book.setStock(newStock);
            Book updatedBook = bookRepository.save(book);

            log.info("도서 재고 감소 완료 - ID: {}, 제목: '{}', 재고: {}",
                    updatedBook.getId(), updatedBook.getTitle(), updatedBook.getStock());

            return new BookResponse(updatedBook);

        } catch (BookNotFoundException | InvalidBookDataException | InsufficientStockException e) {
            log.warn("재고 감소 요청 오류 - ID: {}, 오류: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("재고 감소 중 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("재고 감소 중 오류가 발생했습니다", e);
        }
    }

    // ========== 공개 헬퍼 메서드 ==========

    public Book findById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("존재하지 않는 도서입니다. ID: " + bookId));
    }

    // ========== 비즈니스 로직 검증 메서드들 ==========

    private void validateBookBusinessRules(BookRequest request) {
        // ISBN 중복 검증
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByIsbn(request.getIsbn().trim());
            if (existingBook.isPresent()) {
                throw new DuplicateBookException("이미 등록된 ISBN입니다: " + request.getIsbn());
            }
        }

        // 제목+저자 중복 검증
        if (isDuplicateBookExists(request.getTitle().trim(), request.getAuthor().trim())) {
            throw new DuplicateBookException("동일한 제목과 저자의 도서가 이미 존재합니다");
        }
    }

    private void validateUpdateBookBusinessRules(Book existingBook, BookRequest request) {
        // ISBN 중복 검증 (기존 책과 다른 ISBN으로 변경하는 경우)
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
            String newIsbn = request.getIsbn().trim();
            if (!newIsbn.equals(existingBook.getIsbn())) {
                Optional<Book> duplicateBook = bookRepository.findByIsbn(newIsbn);
                if (duplicateBook.isPresent()) {
                    throw new DuplicateBookException("이미 등록된 ISBN입니다: " + newIsbn);
                }
            }
        }

        // 제목+저자 중복 검증
        String newTitle = request.getTitle() != null ? request.getTitle().trim() : existingBook.getTitle();
        String newAuthor = request.getAuthor() != null ? request.getAuthor().trim() : existingBook.getAuthor();

        if (!newTitle.equals(existingBook.getTitle()) || !newAuthor.equals(existingBook.getAuthor())) {
            Optional<Book> duplicateBook = bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase(newTitle, newAuthor);
            if (duplicateBook.isPresent() && !duplicateBook.get().getId().equals(existingBook.getId())) {
                throw new DuplicateBookException("동일한 제목과 저자의 도서가 이미 존재합니다");
            }
        }
    }

    private void validateBookDeletionRules(Book book) {
        // 실제 운영환경에서는 주문 내역, 대출 내역 등을 확인해야 함
        if (book.getStock() > BookConstants.HIGH_STOCK_THRESHOLD) {
            log.warn("재고가 많은 도서 삭제 시도 - ID: {}, 재고: {}", book.getId(), book.getStock());
            // 경고만 로그로 남기고 삭제는 허용 (비즈니스 규칙에 따라 조정)
        }
    }

    private boolean isDuplicateBookExists(String title, String author) {
        return bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase(title, author).isPresent();
    }

    private Category validateAndGetCategory(Long categoryId) {
        if (categoryId == null) {
            throw new InvalidBookDataException("카테고리는 필수입니다");
        }

        if (categoryId <= 0) {
            throw new InvalidBookDataException("올바르지 않은 카테고리 ID입니다");
        }

        Category category = categoryService.findById(categoryId);

        if (!isCategoryActive(category)) {
            throw new InvalidBookDataException("비활성화된 카테고리에는 도서를 등록할 수 없습니다");
        }

        return category;
    }

    private boolean isCategoryActive(Category category) {
        // 실제로는 Category에 active 필드가 있다고 가정
        // return category.isActive();
        return true; // 현재는 모든 카테고리를 활성화된 것으로 처리
    }

    // ========== 헬퍼 메서드들 ==========

    private Book createBookFromRequest(BookRequest request, Category category) {
        LocalDateTime now = LocalDateTime.now();

        return Book.builder()
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .isbn(request.getIsbn() != null ? request.getIsbn().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                .stock(request.getStock() != null ? request.getStock() : 0)
                .publicationDate(request.getPublicationDate())
                .publisher(request.getPublisher() != null ? request.getPublisher().trim() : null)
                .pageCount(request.getPageCount())
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void updateBookFields(Book book, BookRequest request, Category newCategory) {
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle().trim());
        }

        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor().trim());
        }

        if (request.getIsbn() != null) {
            book.setIsbn(request.getIsbn().trim().isEmpty() ? null : request.getIsbn().trim());
        }

        if (request.getDescription() != null) {
            book.setDescription(request.getDescription().trim().isEmpty() ? null : request.getDescription().trim());
        }

        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            book.setStock(request.getStock());
        }

        if (request.getPublicationDate() != null) {
            book.setPublicationDate(request.getPublicationDate());
        }

        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher().trim().isEmpty() ? null : request.getPublisher().trim());
        }

        if (request.getPageCount() != null) {
            book.setPageCount(request.getPageCount());
        }

        if (newCategory != null) {
            // 기존 카테고리에서 제거
            if (book.getCategory() != null) {
                book.getCategory().removeBook(book);
            }
            // 새 카테고리에 추가
            book.setCategory(newCategory);
            newCategory.addBook(book);
        }
    }

    private void updateCategoryBookRelation(Category category, Book savedBook) {
        category.addBook(savedBook);

        log.debug("카테고리 '{}' (ID: {})에 도서 '{}' (ID: {}) 추가됨",
                category.getName(), category.getId(),
                savedBook.getTitle(), savedBook.getId());
    }

    private Sort createSortFromFilter(BookFilterRequest filter, Sort defaultSort) {
        if (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty()) {
            return createSort(filter.getSortBy(), filter.getSortDirection());
        }

        if (defaultSort != null && !defaultSort.isUnsorted()) {
            return defaultSort;
        }

        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private Pageable createPageableWithSort(Pageable pageable, Sort sort) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        String validatedSortBy = bookValidator.validateAndNormalizeSortField(sortBy);
        return Sort.by(direction, validatedSortBy);
    }
}