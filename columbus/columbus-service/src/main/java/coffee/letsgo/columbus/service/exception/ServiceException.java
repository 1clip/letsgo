package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/7/14.
 */
public class ServiceException extends Exception {
    public ServiceException() {
        super();
    }

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable inner) {
        super(inner);
    }

    public ServiceException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ServiceException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
