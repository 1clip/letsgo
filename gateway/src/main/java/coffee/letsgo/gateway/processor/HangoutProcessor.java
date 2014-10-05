package coffee.letsgo.gateway.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by xbwu on 10/4/14.
 */
public class HangoutProcessor extends RequestProcessor {
    @Override
    public String process(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        return null;
    }
}
