package coffee.letsgo.streaming.publisher;

/**
 * Created by xbwu on 1/2/15.
 */
public class PublisherFactory {
    public Publisher get() {
        return PublisherKafkaImpl.getInstance();
    }
}
