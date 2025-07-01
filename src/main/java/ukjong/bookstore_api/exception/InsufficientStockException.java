package ukjong.bookstore_api.exception;

public class InsufficientStockException extends RuntimeException {

    private final int currentStock;
    private final int requestedQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.currentStock = 0;
        this.requestedQuantity = 0;
    }

    public InsufficientStockException(int currentStock, int requestedQuantity) {
        super(String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", currentStock, requestedQuantity));
        this.currentStock = currentStock;
        this.requestedQuantity = requestedQuantity;
    }

    public InsufficientStockException(String message, int currentStock, int requestedQuantity) {
        super(message);
        this.currentStock = currentStock;
        this.requestedQuantity = requestedQuantity;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}