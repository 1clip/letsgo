package coffee.letsgo.columbus.server.exception;

/**
 * Created by xbwu on 9/21/14.
 */
public class ColumbusServerException extends Exception {
    public ColumbusServerException() {
        super();
    }

    public ColumbusServerException(String msg) {
        super(msg);
    }

    public ColumbusServerException(Throwable inner) {
        super(inner);
    }

    public ColumbusServerException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ColumbusServerException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
