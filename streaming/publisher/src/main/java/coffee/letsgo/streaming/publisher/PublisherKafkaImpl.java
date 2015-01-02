package coffee.letsgo.streaming.publisher;

import coffee.letsgo.common.ConfigurationReader;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * Created by xbwu on 1/1/15.
 */
public class PublisherKafkaImpl implements Publisher {
    private final ConfigurationReader config = new ConfigurationReader("kafka");

    private PublisherKafkaImpl() {
    }

    private static class PublisherHolder {
        private static Publisher instance = new PublisherKafkaImpl();
    }

    public static Publisher getInstance() {
        return PublisherHolder.instance;
    }

    @Override
    public void publish(String topic, String msg) {
        Properties properties = config.prop();
        ProducerConfig producerConfig = new ProducerConfig(properties);
        kafka.javaapi.producer.Producer<String, String> producer = new kafka.javaapi.producer.Producer<>(producerConfig);
        KeyedMessage<String, String> message = new KeyedMessage<>(topic, msg);
        producer.send(message);
        producer.close();
    }

    @Override
    public void publish(String topic, Object obj) {

    }
}
