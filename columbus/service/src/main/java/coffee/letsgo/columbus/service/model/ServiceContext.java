package coffee.letsgo.columbus.service.model;

/**
 * Created by xbwu on 9/16/14.
 */
public class ServiceContext {
    private ServiceState state;

    public ServiceContext() {
        this.state = ServiceStatus.UNINITIALIZED;
    }

    public ServiceState getState() {
        return state;
    }

    public void setState(ServiceState state) {
        this.state = state;
    }

    public synchronized void process(ServiceEvent event) {
        state.process(this, event);
    }
}
