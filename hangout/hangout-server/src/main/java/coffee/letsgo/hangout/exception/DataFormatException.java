package coffee.letsgo.hangout.exception;

/**
 * Created by xbwu on 10/27/14.
 */
public class DataFormatException extends HangoutRuntimeException {
    public DataFormatException() {
        super();
    }

    public DataFormatException(String msg) {
        super(msg);
    }

    public DataFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
