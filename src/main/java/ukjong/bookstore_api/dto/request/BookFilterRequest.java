package ukjong.bookstore_api.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class BookFilterRequest {

    private String title;
    private String author;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStockOnly;
    private String sortBy = "createdAt"; // 기본값: 등록일순
    private String sortDirection = "desc"; // 기본값: 내림차순
}