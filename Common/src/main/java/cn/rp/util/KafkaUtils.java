package cn.rp.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Rp
 * @date 2026.6.17 17:26
 **/
public class KafkaUtils {
    private static Properties getProps(){
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "node1:9092");
        properties.put("retries",2);
        properties.put("linger.ms",1000);
        properties.put("key.serializer","org.apache.kafka.commmon.serialization.StringSerializern");
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        return properties;
    }
    public static void sendData(String tableName, String data) {
        KafkaProducer<String,String> producer= new KafkaProducer<String,String>(getProps());

        ProducerRecord<String,String> record = new ProducerRecord<String,String>(tableName,data);

        Future<RecordMetadata> recordMetadata = producer.send(record);
        try {
            RecordMetadata recordMeta = recordMetadata.get();
            System.out.println("topic:"+ recordMeta.topic());
            System.out.println("partition:"+ recordMeta.partition());
            System.out.println("offset:"+ recordMeta.offset());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }


    }
}