package coffee.letsgo.gateway.handler;

import coffee.letsgo.gateway.exception.RequestNotMatchException;
import coffee.letsgo.gateway.model.AbstractResponse;
import coffee.letsgo.gateway.processor.RequestProcessor;
import coffee.letsgo.gateway.processor.UserProcessor;
import coffee.letsgo.gateway.util.RequestMatcher;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xbwu on 9/29/14.
 */
@ChannelHandler.Sharable
public class RouterHandler extends SimpleChannelInboundHandler<HttpRequest> {
    private final Map<RequestMatcher, RequestProcessor> routingTable = new HashMap<RequestMatcher, RequestProcessor>();

    public RouterHandler() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gsonBuilder.setDateFormat("yyyy-MM-dd");
        Gson gson = gsonBuilder.create();
        routingTable.put(new RequestMatcher("/users"), new UserProcessor(gson));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
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
        AbstractResponse resp = processor.process(ctx, req);

    }
}
