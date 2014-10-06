package coffee.letsgo.dev;

import coffee.letsgo.gateway.GatewayServer;
import coffee.letsgo.hangout.server.HangoutServer;
import coffee.letsgo.iceflake.config.IceflakeWorkerConfig;
import coffee.letsgo.iceflake.server.IceflakeServer;
import coffee.letsgo.identity.server.IdentityServer;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

/**
 * Created by xbwu on 10/5/14.
 */
public class ServiceStarter {
    private static IceflakeWorkerConfig iceflakeWorkerConfig;
    private static IceflakeServer iceflakeServer;
    private static IdentityServer identityServer;
    private static HangoutServer hangoutServer;

    public static void main(String[] args) throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        startALl();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log("shutting down all services");
                //stopAll();
                log("all services shut down");
            }
        });
    }

    private static void startALl() throws Exception {
        startIceflakeServer();
        startIdentityServer();
        startHangoutServer();
        startGateway();
    }

    private static void stopAll() {
        stopHangoutServer();
        stopIdentityServer();
        stopIceflakeServer();
    }

    private static void startIceflakeServer() {
        log("starting iceflake server");
        iceflakeWorkerConfig = new IceflakeWorkerConfig("localhost", 7609, 11);
        iceflakeServer = new IceflakeServer(iceflakeWorkerConfig.getId(), iceflakeWorkerConfig.getPort());
        iceflakeServer.start();
        log("iceflake server started");
    }

    private static void stopIceflakeServer() {
        log("stopping iceflake server");
        try {
            iceflakeServer.shutdown();
            log("iceflake server stopped");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            iceflakeServer = null;
        }
    }

    private static void startIdentityServer() {
        log("starting identity server");
        identityServer = new IdentityServer(IdentityServer.defaultServerPort);
        identityServer.start();
        log("identity server started");
    }

    private static void stopIdentityServer() {
        log("stopping identity server");
        try {
            identityServer.shutdown();
            log("identity server stopped");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            identityServer = null;
        }
    }

    private static void startHangoutServer() {
        log("starting hangout server");
        hangoutServer = new HangoutServer(HangoutServer.defaultServerPort);
        hangoutServer.start();
        log("hangout server started");
    }

    private static void stopHangoutServer() {
        log("stopping hangout server");
        try {
            hangoutServer.shutdown();
            log("hangout server stopped");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            hangoutServer = null;
        }
    }

    private static void startGateway() throws Exception {
        log("accepting request from gateway server");
        GatewayServer.main(null);
    }

    private static void log(String format, Object... objects) {
        System.out.println(String.format(format, objects));
    }
}
