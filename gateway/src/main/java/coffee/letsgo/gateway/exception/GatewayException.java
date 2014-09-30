package coffee.letsgo.gateway.exception;

/**
 * Created by xbwu on 9/29/14.
 */
public class GatewayException extends Exception {
    public GatewayException(String msg) {
        super(msg);
    }

    public GatewayException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
