package ukjong.bookstore_api.exception;

public class DuplicateBookException extends RuntimeException {

    public DuplicateBookException(String message) {
        super(message);
    }

    public DuplicateBookException(String message, Throwable cause) {
        super(message, cause);
    }
}
