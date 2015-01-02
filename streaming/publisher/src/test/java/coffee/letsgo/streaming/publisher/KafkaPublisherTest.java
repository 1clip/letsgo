package coffee.letsgo.streaming.publisher;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Created by xbwu on 1/2/15.
 */
public class KafkaPublisherTest {
    private Publisher pub;

    @Test
    public void testPublishMsg() {
        pub.publish("unit_test_publish", UUID.randomUUID().toString());
    }

    @BeforeTest
    public void setup() {
        pub = PublisherKafkaImpl.getInstance();
    }
}
