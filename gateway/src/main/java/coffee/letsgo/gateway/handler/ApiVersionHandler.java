package coffee.letsgo.gateway.handler;

import coffee.letsgo.gateway.exception.ApiVersionException;
import coffee.letsgo.gateway.exception.UnsupportedRequestException;
import coffee.letsgo.gateway.util.Constants;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xbwu on 9/28/14.
 */
@ChannelHandler.Sharable
public class ApiVersionHandler extends ChannelInboundHandlerAdapter {
    private static final AttributeKey<Integer> versionKey = AttributeKey.valueOf(Constants.apiVersionKeyName);
    private static final Pattern versionPattern = Pattern.compile("^/v(\\d+)(/.*)$");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            throw new UnsupportedRequestException("http request only");
        }
        HttpRequest req = (HttpRequest) msg;
        System.out.println(req.getUri());
        Matcher versionMatcher = versionPattern.matcher(req.getUri());
        if (!versionMatcher.find()) {
            throw new ApiVersionException("api version not specified");
        }
        Attribute<Integer> attr = ctx.attr(versionKey);
        int ver;
        try{
            ver = Integer.parseInt(versionMatcher.group(1));
        } catch (NumberFormatException ex) {
            throw new ApiVersionException("incorrect version format", ex);
        }
        attr.set(ver);
        req.setUri(versionMatcher.group(2));
        ctx.fireChannelRead(req);
    }
}
