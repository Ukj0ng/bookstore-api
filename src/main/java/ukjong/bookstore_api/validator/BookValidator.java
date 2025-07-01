package ukjong.bookstore_api.validator;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ukjong.bookstore_api.constants.BookConstants;
import ukjong.bookstore_api.dto.request.BookFilterRequest;
import ukjong.bookstore_api.dto.request.BookRequest;
import ukjong.bookstore_api.exception.InvalidBookDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 도서 관련 검증 로직을 담당하는 클래스
 */
@Component
public class BookValidator {

    // 정렬 필드 매핑
    private static final Map<String, String> SORT_FIELD_MAP = new HashMap<>();

    static {
        // 영어 -> 필드명
        SORT_FIELD_MAP.put("title", BookConstants.SORT_BY_TITLE);
        SORT_FIELD_MAP.put("author", BookConstants.SORT_BY_AUTHOR);
        SORT_FIELD_MAP.put("price", BookConstants.SORT_BY_PRICE);
        SORT_FIELD_MAP.put("stock", BookConstants.SORT_BY_STOCK);
        SORT_FIELD_MAP.put("createdat", BookConstants.SORT_BY_CREATED_AT);
        SORT_FIELD_MAP.put("created_at", BookConstants.SORT_BY_CREATED_AT);
        SORT_FIELD_MAP.put("publicationdate", BookConstants.SORT_BY_PUBLICATION_DATE);
        SORT_FIELD_MAP.put("publication_date", BookConstants.SORT_BY_PUBLICATION_DATE);
        SORT_FIELD_MAP.put("pagecount", BookConstants.SORT_BY_PAGE_COUNT);
        SORT_FIELD_MAP.put("page_count", BookConstants.SORT_BY_PAGE_COUNT);

        // 한글 -> 필드명
        SORT_FIELD_MAP.put("제목", BookConstants.SORT_BY_TITLE);
        SORT_FIELD_MAP.put("저자", BookConstants.SORT_BY_AUTHOR);
        SORT_FIELD_MAP.put("가격", BookConstants.SORT_BY_PRICE);
        SORT_FIELD_MAP.put("재고", BookConstants.SORT_BY_STOCK);
        SORT_FIELD_MAP.put("등록일", BookConstants.SORT_BY_CREATED_AT);
        SORT_FIELD_MAP.put("출판일", BookConstants.SORT_BY_PUBLICATION_DATE);
        SORT_FIELD_MAP.put("페이지수", BookConstants.SORT_BY_PAGE_COUNT);
    }

    /**
     * 도서 생성 요청 검증
     */
    public void validateCreateBookRequest(BookRequest request) {
        if (request == null) {
            throw new InvalidBookDataException("도서 정보는 필수입니다");
        }

        validateTitle(request.getTitle(), true);
        validateAuthor(request.getAuthor(), true);
        validateIsbn(request.getIsbn());
        validatePrice(request.getPrice());
        validateStock(request.getStock());
        validatePageCount(request.getPageCount());
        validatePublisher(request.getPublisher());
        validateDescription(request.getDescription());
        validatePublicationDate(request.getPublicationDate());
    }

    /**
     * 도서 수정 요청 검증
     */
    public void validateUpdateBookRequest(Long id, BookRequest request) {
        validateId(id, "도서 ID");

        if (request == null) {
            throw new InvalidBookDataException("수정할 도서 정보는 필수입니다");
        }

        // 수정 시에는 null 허용 (부분 수정)
        validateTitle(request.getTitle(), false);
        validateAuthor(request.getAuthor(), false);
        validateIsbn(request.getIsbn());
        validatePrice(request.getPrice());
        validateStock(request.getStock());
        validatePageCount(request.getPageCount());
        validatePublisher(request.getPublisher());
        validateDescription(request.getDescription());
        validatePublicationDate(request.getPublicationDate());
    }

    /**
     * 필터 요청 검증
     */
    public void validateFilterRequest(BookFilterRequest filter, Pageable pageable) {
        if (filter == null) {
            throw new InvalidBookDataException("필터 조건은 필수입니다");
        }

        validatePageable(pageable);
        validatePriceRange(filter.getMinPrice(), filter.getMaxPrice());
    }

    /**
     * 페이징 검증
     */
    public void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new InvalidBookDataException("Pageable은 null일 수 없습니다");
        }

        if (pageable.getPageSize() > BookConstants.MAX_PAGE_SIZE) {
            throw new InvalidBookDataException("페이지 크기는 " + BookConstants.MAX_PAGE_SIZE + "을 초과할 수 없습니다");
        }

        if (pageable.getPageSize() <= 0) {
            throw new InvalidBookDataException("페이지 크기는 1 이상이어야 합니다");
        }

        if (pageable.getPageNumber() < 0) {
            throw new InvalidBookDataException("페이지 번호는 0 이상이어야 합니다");
        }
    }

    /**
     * 검색 키워드 검증
     */
    public void validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidBookDataException("검색어를 입력해주세요");
        }

        if (keyword.trim().length() > BookConstants.MAX_SEARCH_KEYWORD_LENGTH) {
            throw new InvalidBookDataException("검색어는 " + BookConstants.MAX_SEARCH_KEYWORD_LENGTH + "자를 초과할 수 없습니다");
        }
    }

    /**
     * ID 검증
     */
    public void validateId(Long id, String fieldName) {
        if (id == null) {
            throw new InvalidBookDataException(fieldName + "는 필수입니다");
        }

        if (id <= 0) {
            throw new InvalidBookDataException("유효하지 않은 " + fieldName + "입니다");
        }
    }

    /**
     * 재고 업데이트 검증
     */
    public void validateStockUpdate(Long id, int quantity) {
        validateId(id, "도서 ID");

        if (quantity < BookConstants.MIN_STOCK) {
            throw new InvalidBookDataException("재고는 " + BookConstants.MIN_STOCK + " 이상이어야 합니다");
        }

        if (quantity > BookConstants.MAX_STOCK) {
            throw new InvalidBookDataException("재고는 " + BookConstants.MAX_STOCK + "개를 초과할 수 없습니다");
        }
    }

    /**
     * 재고 변경 검증
     */
    public void validateStockChange(Long id, int quantity, String operation) {
        validateId(id, "도서 ID");

        if (quantity <= 0) {
            throw new InvalidBookDataException("재고 " + operation + " 수량은 1 이상이어야 합니다");
        }

        if (quantity > BookConstants.MAX_STOCK_BATCH_CHANGE) {
            throw new InvalidBookDataException("한 번에 " + operation + "할 수 있는 재고는 " +
                    BookConstants.MAX_STOCK_BATCH_CHANGE + "개를 초과할 수 없습니다");
        }
    }

    /**
     * 정렬 필드 검증 및 정규화
     */
    public String validateAndNormalizeSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return BookConstants.SORT_BY_CREATED_AT;
        }

        String normalizedField = sortBy.trim().toLowerCase();
        String mappedField = SORT_FIELD_MAP.get(normalizedField);

        if (mappedField == null) {
            throw new InvalidBookDataException("지원하지 않는 정렬 필드입니다: " + sortBy);
        }

        return mappedField;
    }

    // ========== 개별 필드 검증 메서드들 ==========

    private void validateTitle(String title, boolean required) {
        if (required && (title == null || title.trim().isEmpty())) {
            throw new InvalidBookDataException("도서 제목은 필수입니다");
        }

        if (title != null) {
            if (title.trim().isEmpty()) {
                throw new InvalidBookDataException("도서 제목은 비어있을 수 없습니다");
            }

            if (title.trim().length() < BookConstants.MIN_TITLE_LENGTH) {
                throw new InvalidBookDataException("도서 제목은 최소 " + BookConstants.MIN_TITLE_LENGTH + "자 이상이어야 합니다");
            }

            if (title.trim().length() > BookConstants.MAX_TITLE_LENGTH) {
                throw new InvalidBookDataException("도서 제목은 " + BookConstants.MAX_TITLE_LENGTH + "자를 초과할 수 없습니다");
            }
        }
    }

    private void validateAuthor(String author, boolean required) {
        if (required && (author == null || author.trim().isEmpty())) {
            throw new InvalidBookDataException("저자명은 필수입니다");
        }

        if (author != null) {
            if (author.trim().isEmpty()) {
                throw new InvalidBookDataException("저자명은 비어있을 수 없습니다");
            }

            if (author.trim().length() > BookConstants.MAX_AUTHOR_LENGTH) {
                throw new InvalidBookDataException("저자명은 " + BookConstants.MAX_AUTHOR_LENGTH + "자를 초과할 수 없습니다");
            }
        }
    }

    private void validateIsbn(String isbn) {
        if (isbn != null && !isbn.trim().isEmpty()) {
            validateIsbnFormat(isbn.trim());
        }
    }

    private void validateIsbnFormat(String isbn) {
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");

        if (!cleanIsbn.matches("^\\d{10}$") && !cleanIsbn.matches("^\\d{13}$")) {
            throw new InvalidBookDataException("ISBN은 10자리 또는 13자리 숫자여야 합니다");
        }

        if (cleanIsbn.length() == BookConstants.ISBN_13_LENGTH) {
            validateIsbn13Checksum(cleanIsbn);
        }
    }

    private void validateIsbn13Checksum(String isbn13) {
        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(isbn13.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int checkDigit = (10 - (sum % 10)) % 10;
            int providedCheckDigit = Character.getNumericValue(isbn13.charAt(12));

            if (checkDigit != providedCheckDigit) {
                throw new InvalidBookDataException("올바르지 않은 ISBN-13 형식입니다");
            }
        } catch (Exception e) {
            throw new InvalidBookDataException("ISBN 형식 검증 중 오류가 발생했습니다");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price != null) {
            if (price.compareTo(BookConstants.MIN_PRICE) < 0) {
                throw new InvalidBookDataException("가격은 " + BookConstants.MIN_PRICE + " 이상이어야 합니다");
            }

            if (price.compareTo(BookConstants.MAX_PRICE) > 0) {
                throw new InvalidBookDataException("가격은 " + BookConstants.MAX_PRICE + "원을 초과할 수 없습니다");
            }
        }
    }

    private void validateStock(Integer stock) {
        if (stock != null) {
            if (stock < BookConstants.MIN_STOCK) {
                throw new InvalidBookDataException("재고는 " + BookConstants.MIN_STOCK + " 이상이어야 합니다");
            }

            if (stock > BookConstants.MAX_STOCK) {
                throw new InvalidBookDataException("재고는 " + BookConstants.MAX_STOCK + "개를 초과할 수 없습니다");
            }
        }
    }

    private void validatePageCount(Integer pageCount) {
        if (pageCount != null) {
            if (pageCount < BookConstants.MIN_PAGE_COUNT) {
                throw new InvalidBookDataException("페이지 수는 " + BookConstants.MIN_PAGE_COUNT + " 이상이어야 합니다");
            }

            if (pageCount > BookConstants.MAX_PAGE_COUNT) {
                throw new InvalidBookDataException("페이지 수는 " + BookConstants.MAX_PAGE_COUNT + "페이지를 초과할 수 없습니다");
            }
        }
    }

    private void validatePublisher(String publisher) {
        if (publisher != null && publisher.trim().length() > BookConstants.MAX_PUBLISHER_LENGTH) {
            throw new InvalidBookDataException("출판사명은 " + BookConstants.MAX_PUBLISHER_LENGTH + "자를 초과할 수 없습니다");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.trim().length() > BookConstants.MAX_DESCRIPTION_LENGTH) {
            throw new InvalidBookDataException("도서 설명은 " + BookConstants.MAX_DESCRIPTION_LENGTH + "자를 초과할 수 없습니다");
        }
    }

    private void validatePublicationDate(LocalDate publicationDate) {
        if (publicationDate != null) {
            if (publicationDate.isAfter(LocalDate.now())) {
                throw new InvalidBookDataException("출판일은 미래 날짜일 수 없습니다");
            }

            if (publicationDate.isBefore(BookConstants.MIN_PUBLICATION_DATE)) {
                throw new InvalidBookDataException("출판일이 너무 과거입니다");
            }
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null) {
            if (minPrice.compareTo(maxPrice) > 0) {
                throw new InvalidBookDataException("최소 가격이 최대 가격보다 클 수 없습니다");
            }
        }

        if (minPrice != null && minPrice.compareTo(BookConstants.MIN_PRICE) < 0) {
            throw new InvalidBookDataException("최소 가격은 " + BookConstants.MIN_PRICE + " 이상이어야 합니다");
        }

        if (maxPrice != null && maxPrice.compareTo(BookConstants.MAX_FILTER_PRICE) > 0) {
            throw new InvalidBookDataException("최대 가격이 너무 큽니다");
        }
    }
}