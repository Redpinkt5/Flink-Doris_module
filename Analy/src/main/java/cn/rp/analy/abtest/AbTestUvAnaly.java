package cn.rp.analy.abtest;

import cn.rp.analy.abtest.map.AbTestPvMap;
import cn.rp.analy.abtest.map.AbTestUvDistinctMap;
import cn.rp.analy.abtest.map.AbTestUvTotalMap;
import cn.rp.analy.abtest.reduce.AbTestReduce;
import cn.rp.analy.abtest.sink.AbTestLeavePvSink;
import cn.rp.analy.abtest.sink.AbTestLeaveUvSink;
import cn.rp.analy.abtest.sink.AbTestTotalLeavePvSink;
import cn.rp.analy.abtest.sink.AbTestTotalLeaveUvSink;
import cn.rp.entity.AbTestPvResult;
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
public class AbTestUvAnaly {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 构建新一代 KafkaSource（替代已删除的 FlinkKafkaConsumer）
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.88.101:9092")
                .setTopics("test")
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

        SingleOutputStreamOperator<AbTestPvResult> sreamLeaveMap = stream
                .flatMap(new AbTestPvMap());

        SingleOutputStreamOperator<AbTestPvResult> streamLeaveReduce = sreamLeaveMap
                .keyBy("groupField")
                .timeWindow(Time.minutes(11))
                .reduce(new AbTestReduce());

        SingleOutputStreamOperator<AbTestPvResult> streamDistinctMap = streamLeaveReduce.flatMap(new AbTestUvDistinctMap());

        SingleOutputStreamOperator<AbTestPvResult> streamLeaveDistinctReduce = streamDistinctMap
                .keyBy("groupField")
                .timeWindow(Time.minutes(11))
                .reduce(new AbTestReduce());


        SingleOutputStreamOperator<AbTestPvResult> streamTotalMap = streamLeaveDistinctReduce.map(new AbTestUvTotalMap());


        SingleOutputStreamOperator<AbTestPvResult> streamTotalReduce = streamTotalMap
                .keyBy("groupField")
                .timeWindow(Time.minutes(11))
                .reduce(new AbTestReduce());


        streamLeaveDistinctReduce.addSink(new AbTestLeaveUvSink());
        streamTotalReduce.addSink(new AbTestTotalLeaveUvSink());



        // 开启 Checkpoint，5秒一次（和原代码一致）
        env.enableCheckpointing(5000);
        stream.print();

        try {
            env.execute("AbTestUvAnaly");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
