package coffee.letsgo.streaming.publisher;

/**
 * Created by xbwu on 12/16/14.
 */
public interface Publisher {
    void publish(String topic, String msg);

    void publish(String topic, Object obj);
}
