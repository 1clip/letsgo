package coffee.letsgo.common.exception;

/**
 * Created by xbwu on 10/15/14.
 */
public class ZookeeperException extends RuntimeException {
    public ZookeeperException() {
        super();
    }

    public ZookeeperException(String msg) {
        super(msg);
    }

    public ZookeeperException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
