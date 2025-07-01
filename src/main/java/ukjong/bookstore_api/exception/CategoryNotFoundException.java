package ukjong.bookstore_api.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(Long categoryId) {
        super("카테고리를 찾을 수 없습니다. ID: " + categoryId);
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}