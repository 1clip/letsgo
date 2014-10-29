package coffee.letsgo.hangout.exception;

/**
 * Created by xbwu on 10/28/14.
 */
public class HangoutRuntimeException extends RuntimeException {
    public HangoutRuntimeException() {
        super();
    }

    public HangoutRuntimeException(String msg) {
        super(msg);
    }

    public HangoutRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
