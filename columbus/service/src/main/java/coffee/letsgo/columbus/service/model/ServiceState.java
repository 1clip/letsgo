package coffee.letsgo.columbus.service.model;

/**
 * Created by xbwu on 9/16/14.
 */
public interface ServiceState {
    void process(ServiceContext ctx, ServiceEvent evt);
}
