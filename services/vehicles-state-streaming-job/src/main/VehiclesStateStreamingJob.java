package main;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisProducer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.util.Properties;

/**
 * A Kinesis Data Analytics for Java application that calculates last know positions
 * of vehicles in a given Kinesis stream over a sliding window
 * and overrides the operator level parallelism in the flink application.
 * <p>
 * Note that the maximum parallelism in the Flink code cannot be greater than
 * provisioned parallelism (default is 1). To get this application to work,
 * use following AWS CLI commands to set the parallelism configuration of the
 * Kinesis Data Analytics for Java application.
 * <p>
 * 1. Fetch the current application version Id using following command:
 * aws kinesisanalyticsv2 describe-application --application-name <Application Name>
 * 2. Update the parallelism configuration of the application using version Id:
 * aws kinesisanalyticsv2 update-application
 *      --application-name <Application Name>
 *      --current-application-version-id <VersionId>
 *      --application-configuration-update "{\"FlinkApplicationConfigurationUpdate\": { \"ParallelismConfigurationUpdate\": {\"ParallelismUpdate\": 5, \"ConfigurationTypeUpdate\": \"CUSTOM\" }}}"
 */
public class VehiclesStateStreamingJob {
    private static final String region = "us-central-1";
    private static final String inputStreamName = "vehicles-state-stream";
    private static final String outputStreamName = "ExampleOutputStream";

    private static DataStream<String> createSourceFromStaticConfig(
            StreamExecutionEnvironment env) {
        Properties inputProperties = new Properties();
        inputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
        inputProperties.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION,
                "LATEST");

        return env.addSource(new FlinkKinesisConsumer<>(inputStreamName,
                new SimpleStringSchema(), inputProperties));
    }

    private static FlinkKinesisProducer<String> createSinkFromStaticConfig() {
        Properties outputProperties = new Properties();
        outputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);

        FlinkKinesisProducer<String> sink = new FlinkKinesisProducer<>(new
                SimpleStringSchema(), outputProperties);
        sink.setDefaultStream(outputStreamName);
        sink.setDefaultPartition("0");
        return sink;
    }

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> input = createSourceFromStaticConfig(env);
        ObjectMapper jsonParser = new ObjectMapper();
        input.map(value -> {
            JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);
            return new Tuple2<>(jsonNode.get("id").asText(), jsonNode.get("timestamp").asDouble());
        }).returns(Types.TUPLE(Types.STRING, Types.LONG, Types.FLOAT, Types.FLOAT))
                .keyBy(0)
                .timeWindow(Time.seconds(5), Time.seconds(1)) // Sliding window definition
                .max(1) // Calculate latest timestamp
                .setParallelism(3) // Set parallelism for the min operator
                .map(value -> value.f0 + ":  (with parallelism 3) - " + value.f1.toString() + "\n")
                .addSink(createSinkFromStaticConfig());

        env.execute("Latest state");
    }
}