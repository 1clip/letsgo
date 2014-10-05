package coffee.letsgo.hangout.client;

/**
 * Created by xbwu on 9/21/14.
 */
public class HangoutClientException extends RuntimeException {
    public HangoutClientException() {
        super();
    }

    public HangoutClientException(String msg) {
        super(msg);
    }

    public HangoutClientException(Throwable inner) {
        super(inner);
    }

    public HangoutClientException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public HangoutClientException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
