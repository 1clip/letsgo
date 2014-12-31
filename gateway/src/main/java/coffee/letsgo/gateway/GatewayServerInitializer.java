package coffee.letsgo.gateway;

/**
 * Created by xbwu on 9/25/14.
 */

import coffee.letsgo.gateway.handler.ApiVersionHandler;
import coffee.letsgo.gateway.handler.RouterHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import static com.google.common.base.Verify.verifyNotNull;

public class  GatewayServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public GatewayServerInitializer(SslContext sslCtx) {
        this.sslCtx = verifyNotNull(sslCtx);
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(sslCtx.newHandler(ch.alloc()))
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ApiVersionHandler(),
                        new RouterHandler());
    }
}