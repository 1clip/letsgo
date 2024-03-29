package coffee.letsgo.columbus.server;

import coffee.letsgo.columbus.server.exception.ColumbusServerRuntimeException;
import coffee.letsgo.columbus.service.ServiceDeamon;
import coffee.letsgo.columbus.service.manager.ServiceManager;
import coffee.letsgo.columbus.service.manager.ServiceManagerZookeeperImpl;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/18/14.
 */
public abstract class ColumbusServer {
    protected String serviceName;
    protected String localhost;
    protected int port;
    private String serviceUri;
    private ServiceDeamon serviceDeamon;
    private boolean startedSwitch = false;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected abstract void run();

    protected abstract void stop();

    public final void start() {
        if (startedSwitch) {
            logger.warn("columbus server already started");
            return;
        }
        serviceDeamon = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ServiceManager.class).to(ServiceManagerZookeeperImpl.class);
                binder.bindConstant().annotatedWith(Names.named("service name")).to(verifyNotNull(serviceName));
            }
        }).getInstance(ServiceDeamon.class);
        try {
            serviceDeamon.start();
            serviceDeamon.awaitInitialized(60 * 1000);
            serviceUri = (localhost == null || localhost.isEmpty()) ?
                    serviceDeamon.addMember(port) :
                    serviceDeamon.addMember(String.format("%s:%d", localhost.trim(), port));
            logger.info("starting columbus server on {}", serviceUri);
            run();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    logger.warn("shutting down {}", serviceName);
                    try {
                        shutdown();
                        logger.warn("service {} stopped", serviceName);
                    } catch (Exception ex) {
                        logger.error("failed to shutdown service " + serviceName, ex);
                    }
                }
            });
            serviceDeamon.activate(serviceUri);
            startedSwitch = true;
            logger.info("columbus server on {} started", serviceUri);
        } catch (Exception ex) {
            logger.error("failed to start columbus server " + serviceUri, ex);
            serviceDeamon.deactivate(serviceUri);
            serviceDeamon.shutdown();
            throw new ColumbusServerRuntimeException("failed to start columbus server", ex);
        }
    }

    public final void shutdown() {
        if (!startedSwitch) {
            logger.warn("columbus service {} not started", serviceName);
            return;
        }
        try {
            logger.warn("deactivating service {}", serviceName);
            serviceDeamon.deactivate(serviceUri);
            startedSwitch = false;
        } catch (Exception ex) {
            logger.error("failed to deactivate server " + serviceUri, ex);
        }
        try {
            serviceDeamon.shutdown();
            serviceDeamon = null;
            startedSwitch = false;
        } catch (Exception ex) {
            logger.error("failed to shutdown service deamon for " + serviceName, ex);
        }
        if (startedSwitch) {
            throw new ColumbusServerRuntimeException("failed to shutdown columbus server {}", serviceName);
        }
        try {
            stop();
        } catch (Exception ex) {
            logger.error("failed to stop concrete server " + serviceName, ex);
            throw new ColumbusServerRuntimeException("failed to stop concrete server " + serviceName, ex);
        }
    }
}
