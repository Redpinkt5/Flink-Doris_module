package cn.rp.analy.orderAnaly;

import cn.rp.analy.map.AbTestPvMap;
import cn.rp.analy.map.AbTestPvTotalMap;
import cn.rp.analy.orderAnaly.map.OrderAnalyMap;
import cn.rp.analy.orderAnaly.reduce.OrderAnalyReduce;
import cn.rp.analy.orderAnaly.sink.OrderAnalySink;
import cn.rp.analy.reduce.AbTestReduce;
import cn.rp.analy.sink.AbTestLeavePvSink;
import cn.rp.analy.sink.AbTestTotalLeavePvSink;
import cn.rp.entity.AbTestPvResult;
import cn.rp.entity.OrderAnalyInfoResult;
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
public class OrderAnaly {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 构建新一代 KafkaSource（替代已删除的 FlinkKafkaConsumer）
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.88.101:9092")
                .setTopics("Order")
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

        SingleOutputStreamOperator<OrderAnalyInfoResult> sreamLeaveMap = stream
                .flatMap(new OrderAnalyMap());

        SingleOutputStreamOperator<OrderAnalyInfoResult> streamLeaveReduce = sreamLeaveMap
                .keyBy("groupField")
                .timeWindow(Time.minutes(11))
                .reduce(new OrderAnalyReduce());

        streamLeaveReduce.addSink(new OrderAnalySink());



        // 开启 Checkpoint，5秒一次（和原代码一致）
        env.enableCheckpointing(5000);
        stream.print();

        try {
            env.execute("OrderAnaly");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
