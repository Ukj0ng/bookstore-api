package ukjong.bookstore_api.constants;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 도서 관련 상수 정의 클래스
 */
public final class BookConstants {

    // 인스턴스 생성 방지
    private BookConstants() {}

    // === 길이 제한 ===
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MIN_TITLE_LENGTH = 1;
    public static final int MAX_AUTHOR_LENGTH = 100;
    public static final int MAX_PUBLISHER_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    // === 숫자 제한 ===
    public static final int MAX_STOCK = 100000;
    public static final int MIN_STOCK = 0;
    public static final int HIGH_STOCK_THRESHOLD = 100; // 삭제 시 경고 기준
    public static final int MAX_STOCK_BATCH_CHANGE = 10000; // 한 번에 변경 가능한 재고량

    public static final int MAX_PAGE_COUNT = 50000;
    public static final int MIN_PAGE_COUNT = 1;

    // === 가격 제한 ===
    public static final BigDecimal MAX_PRICE = new BigDecimal("1000000");
    public static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    public static final BigDecimal MAX_FILTER_PRICE = new BigDecimal("10000000"); // 필터링 시 최대 가격

    // === 페이징 제한 ===
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MIN_PAGE_NUMBER = 0;

    // === 날짜 제한 ===
    public static final LocalDate MIN_PUBLICATION_DATE = LocalDate.of(1000, 1, 1);

    // === ISBN 관련 ===
    public static final int ISBN_10_LENGTH = 10;
    public static final int ISBN_13_LENGTH = 13;

    // === 검색 관련 ===
    public static final int MIN_SEARCH_KEYWORD_LENGTH = 1;
    public static final int MAX_SEARCH_KEYWORD_LENGTH = 100;

    // === 정렬 필드 ===
    public static final String SORT_BY_TITLE = "title";
    public static final String SORT_BY_AUTHOR = "author";
    public static final String SORT_BY_PRICE = "price";
    public static final String SORT_BY_STOCK = "stock";
    public static final String SORT_BY_CREATED_AT = "createdAt";
    public static final String SORT_BY_PUBLICATION_DATE = "publicationDate";
    public static final String SORT_BY_PAGE_COUNT = "pageCount";

    // === 정렬 방향 ===
    public static final String SORT_DIRECTION_ASC = "asc";
    public static final String SORT_DIRECTION_DESC = "desc";
}