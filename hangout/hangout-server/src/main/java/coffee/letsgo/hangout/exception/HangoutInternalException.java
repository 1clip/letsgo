package coffee.letsgo.hangout.exception;

/**
 * Created by xbwu on 10/28/14.
 */
public class HangoutInternalException extends HangoutRuntimeException {
    public HangoutInternalException() {
        super();
    }

    public HangoutInternalException(String msg) {
        super(msg);
    }

    public HangoutInternalException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
