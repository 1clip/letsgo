package coffee.letsgo.columbus.server.exception;

/**
 * Created by xbwu on 9/21/14.
 */
public class ColumbusSvrRuntimeException extends RuntimeException {
    public ColumbusSvrRuntimeException() {
        super();
    }

    public ColumbusSvrRuntimeException(String msg) {
        super(msg);
    }

    public ColumbusSvrRuntimeException(Throwable inner) {
        super(inner);
    }

    public ColumbusSvrRuntimeException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ColumbusSvrRuntimeException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
