package vn.iotstar.exceptions;

/** Token đã hết hạn (claim exp) */
public class ExpiredJwtTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExpiredJwtTokenException(String message) {
        super(message);
    }
}
