package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/7/14.
 */
public class NoAvailableNodeException extends ServiceException {
    public NoAvailableNodeException() {
        super();
    }

    public NoAvailableNodeException(String msg) {
        super(msg);
    }

    public NoAvailableNodeException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
