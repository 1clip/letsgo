package coffee.letsgo.identity.server.exception;

/**
 * Created by xbwu on 10/29/14.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super();
    }

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
