package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/9/14.
 */
public class DeamonNotStartedException extends ServiceException {
    public DeamonNotStartedException() {
        super();
    }

    public DeamonNotStartedException(String msg) {
        super(msg);
    }

    public DeamonNotStartedException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
