package cn.rp.analy.marketing;

import cn.rp.analy.marketing.map.MarketingLiuliangMap;
import cn.rp.analy.marketing.reduce.MarketingReduce;
import cn.rp.analy.marketing.sink.MarketingLiuliangSink;
import cn.rp.entity.MarketingInfoResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @author Rp
 * @date 2026.6.9 16:30
 **/
public class MarketingLiuliangAnaly {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 开启 Checkpoint，5秒一次（和原代码一致）
        env.enableCheckpointing(5000);

        // 构建新一代 KafkaSource（替代已删除的 FlinkKafkaConsumer）
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.88.101:9092")
                .setTopics("datacoll")
                .setGroupId("rptest")
                // 从最早位点开始消费（对应原代码的 setStartFromEarliest()）
                .setStartingOffsets(OffsetsInitializer.earliest())
                // 只解析消息体为字符串（对应原代码的 SimpleStringSchema）
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();



        // 使用 fromSource 替代 addSource，必须传入 WatermarkStrategy
        DataStream<String> stream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source"
        );



        SingleOutputStreamOperator<MarketingInfoResult> streamLeaveMap = stream
                .flatMap(new MarketingLiuliangMap());

        SingleOutputStreamOperator<MarketingInfoResult> streamLeaveReduce = streamLeaveMap
                .keyBy("groupField")
                .timeWindow(Time.minutes(11))
                .reduce(new MarketingReduce());


        streamLeaveMap.addSink(new MarketingLiuliangSink());

        try {
            env.execute("MarketingLiuliangAnaly");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
