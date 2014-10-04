package coffee.letsgo.gateway.handler;

import coffee.letsgo.gateway.exception.RequestNotMatchException;
import coffee.letsgo.gateway.processor.RequestProcessor;
import coffee.letsgo.gateway.processor.UserProcessor;
import coffee.letsgo.gateway.util.RequestMatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xbwu on 9/29/14.
 */
@ChannelHandler.Sharable
public class RouterHandler extends GatewayInboundHandler {
    private final Map<RequestMatcher, RequestProcessor> routingTable = new HashMap<>();

    public RouterHandler() {
        routingTable.put(new RequestMatcher("/users"), new UserProcessor());
    }

    @Override
    protected HttpResponse process(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        RequestProcessor processor = null;
        for (Map.Entry<RequestMatcher, RequestProcessor> route : routingTable.entrySet()) {
            if(route.getKey().match(req)) {
                processor = route.getValue();
                break;
            }
        }
        if(processor == null) {
            throw new RequestNotMatchException("unrecognized request");
        }

        String msg = processor.process(ctx, req);
        return createResponse(msg);
    }
}
