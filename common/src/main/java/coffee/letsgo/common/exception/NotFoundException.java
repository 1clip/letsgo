package coffee.letsgo.common.exception;

/**
 * Created by xbwu on 10/14/14.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super();
    }

    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
