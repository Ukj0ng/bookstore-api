package ukjong.bookstore_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BookRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 200, message = "제목은 1-200자 사이여야 합니다")
    private String title;

    @NotBlank(message = "저자는 필수입니다")
    @Size(min = 1, max = 100, message = "저자는 1-100자 사이여야 합니다")
    private String author;

    @Size(max = 50, message = "ISBN은 50자를 초과할 수 없습니다")
    private String isbn;

    private String description;

    @DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
    @Digits(integer = 10, fraction = 2, message = "가격 형식이 올바르지 않습니다")
    private BigDecimal price;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    private Integer stock;

    private LocalDate publicationDate;

    @Size(max = 100, message = "출판사는 100자를 초과할 수 없습니다")
    private String publisher;

    @Min(value = 1, message = "페이지 수는 1 이상이어야 합니다")
    private Integer pageCount;

    private Long categoryId;
}