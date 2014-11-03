package coffee.letsgo.identity.server.exception;

/**
 * Created by xbwu on 10/28/14.
 */
public class IdentityRuntimeException extends RuntimeException {
    public IdentityRuntimeException() {
        super();
    }

    public IdentityRuntimeException(String msg) {
        super(msg);
    }

    public IdentityRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
