package vn.iotstar.exceptions;

/** Chữ ký sai / token sai định dạng / sai thuật toán */
public class InvalidJwtException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidJwtException(String message) {
        super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
