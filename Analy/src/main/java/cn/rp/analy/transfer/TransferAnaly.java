package cn.rp.analy.transfer;

import cn.rp.util.KafkaUtils;
import cn.rp.util.TransferUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author Rp
 * @date 2026.6.9 16:30
 **/
public class TransferAnaly {
    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
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

        SingleOutputStreamOperator<String> filterMap = stream
                .filter(new FilterFunction<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {
                        JSONObject jsonObject = JSONObject.parseObject(value);
                        String type = jsonObject.getString("type");
                        if("INSERT".equals( type)){
                            return true;
                        }
                        return false;
                    }
                });


        SingleOutputStreamOperator<String> mapStream = filterMap.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                JSONObject jsonObject = JSONObject.parseObject(s);
                String tableName = jsonObject.getString("table");
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                JSONObject jsonObjectfinal = new JSONObject();
                jsonObjectfinal.put("tableName", tableName);
                jsonObjectfinal.put("data", JSONObject.toJSONString((jsonObject1)));

                TransferUtil.transferAndInsert(tableName, jsonObject1);
                return JSONObject.toJSONString(jsonObjectfinal);

            }
        });
        mapStream.addSink(new SinkFunction<String>() {
            @Override
            public void invoke(String value) throws Exception {
                JSONObject jsonObject = JSONObject.parseObject(value);
                String tableName = jsonObject.getString("tableName");
                String data = jsonObject.getString("data");

                KafkaUtils.sendData(tableName, data);
            }
        });


        // 开启 Checkpoint，5秒一次（和原代码一致）
        try {
            env.execute("TransferAnaly");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
