package ukjong.bookstore_api.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }

    public BookNotFoundException(Long bookId) {
        super("도서를 찾을 수 없습니다. ID: " + bookId);
    }

    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}