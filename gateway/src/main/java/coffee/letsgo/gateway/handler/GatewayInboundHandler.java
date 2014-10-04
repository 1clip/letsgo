package coffee.letsgo.gateway.handler;

import coffee.letsgo.gateway.exception.GatewayException;
import coffee.letsgo.gateway.exception.UnsupportedRequestException;
import coffee.letsgo.gateway.util.Constants;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by xbwu on 10/4/14.
 */
public abstract class GatewayInboundHandler extends ChannelInboundHandlerAdapter {
    private final boolean autoRelease;

    public GatewayInboundHandler() {
        this(true);
    }

    public GatewayInboundHandler(boolean autoRelease) {
        this.autoRelease = autoRelease;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            HttpRequest req = toHttpRequest(msg);
            HttpResponse resp = process(ctx, req);
            ctx.write(resp).addListener(ChannelFutureListener.CLOSE);
        } finally {
            if (autoRelease) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        if (cause instanceof GatewayException) {
            status = ((GatewayException) cause).getStatus();
        }
        HttpResponse response = createResponse(String.format("{ \"error\": \"%s\" }", cause.getMessage()), status);
        ctx.writeAndFlush(response);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private HttpRequest toHttpRequest(Object msg) throws UnsupportedRequestException {
        if (!(msg instanceof HttpRequest)) {
            throw new UnsupportedRequestException("only accepts http request");
        }
        return (HttpRequest) msg;
    }

    protected abstract HttpResponse process(ChannelHandlerContext ctx, HttpRequest req) throws Exception;

    protected HttpResponse createResponse() {
        return createResponse("");
    }

    protected HttpResponse createResponse(String msg) {
        return createResponse(msg, HttpResponseStatus.OK);
    }

    protected HttpResponse createResponse(String msg, HttpResponseStatus status) {
        HttpResponse response = new DefaultFullHttpResponse(
                Constants.defaultHttpVersion,
                status,
                Unpooled.wrappedBuffer(msg.getBytes(Constants.defaultCharset)));
        response.headers().set("Content-Type", "application/json");
        return response;
    }
}
