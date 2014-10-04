package coffee.letsgo.gateway.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by xbwu on 10/4/14.
 */
public class BadRequestException extends GatewayException {

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
