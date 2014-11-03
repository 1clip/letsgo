package coffee.letsgo.identity.server.exception;

/**
 * Created by xbwu on 10/28/14.
 */
public class IdentityInternalException extends IdentityRuntimeException {
    public IdentityInternalException() {
        super();
    }

    public IdentityInternalException(String msg) {
        super(msg);
    }

    public IdentityInternalException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
