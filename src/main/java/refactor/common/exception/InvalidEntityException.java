package refactor.common.exception;

public class InvalidEntityException extends IllegalArgumentException {
    public InvalidEntityException(String message) {
        super(message);
    }
}