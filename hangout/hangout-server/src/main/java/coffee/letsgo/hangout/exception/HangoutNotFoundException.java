package coffee.letsgo.hangout.exception;

/**
 * Created by xbwu on 10/28/14.
 */
public class HangoutNotFoundException extends HangoutRuntimeException {
    public HangoutNotFoundException() {
        super();
    }

    public HangoutNotFoundException(String msg) {
        super(msg);
    }

    public HangoutNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
