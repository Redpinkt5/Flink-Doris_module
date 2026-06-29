package cn.rp.analy.abtest.terminal;

import cn.rp.analy.abtest.terminal.map.TerminalLiuliangMap;
import cn.rp.analy.abtest.terminal.reduce.TerminalReduce;
import cn.rp.analy.abtest.terminal.sink.TerminalLiuliangSink;
import cn.rp.entity.TerminalInfoResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * 终端流量实时统计任务
 * <p>
 * 处理流程：<br>
 * Kafka 原始日志(JSON 字符串) → TerminalLiuliangMap 解析拆分 → keyBy 分组 → 窗口聚合(Reduce) → 写入 Doris
 * </p>
 *
 * @author Rp
 * @date 2026.6.9 16:30
 **/
public class TerminalLiuliangAnaly {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 开启 Checkpoint，5秒一次
        env.enableCheckpointing(5000);

        // 1. 构建 KafkaSource，从 datacoll topic 读取原始日志字符串
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.88.101:9092")
                .setTopics("datacoll")
                .setGroupId("rptest")
                // 从最早位点开始消费
                .setStartingOffsets(OffsetsInitializer.earliest())
                // 只解析消息体为字符串
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // 2. 接入 Kafka 数据流，得到 DataStream<String>
        DataStream<String> stream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source"
        );

        // 3. TerminalLiuliangMap 输入 String，输出 TerminalInfoResult
        //    一条日志会拆成 2 条：按天统计、按小时统计
        SingleOutputStreamOperator<TerminalInfoResult> terminalInfoMap = stream
                .flatMap(new TerminalLiuliangMap());

        // 4. 按 groupField 分组，11 分钟滚动窗口，用 TerminalReduce 累加 numbers
        SingleOutputStreamOperator<TerminalInfoResult> streamReduce = terminalInfoMap
                .keyBy(TerminalInfoResult::getGroupField)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(11)))
                .reduce(new TerminalReduce());

        // 5. 聚合结果写入 Doris
        streamReduce.addSink(new TerminalLiuliangSink());


        // 6. 执行任务
        try {
            env.execute("TerminalLiuliangAnaly");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
