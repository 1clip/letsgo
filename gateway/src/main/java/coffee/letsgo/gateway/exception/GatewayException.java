package coffee.letsgo.gateway.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by xbwu on 9/29/14.
 */
public class GatewayException extends Exception {

    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public GatewayException(String msg) {
        super(msg);
    }

    public GatewayException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
