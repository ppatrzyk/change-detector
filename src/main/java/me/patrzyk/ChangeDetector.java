package me.patrzyk;

import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

public class ChangeDetector {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// env.enableCheckpointing(10000);

		final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
			.setUri("amqp://rabbit:rabbit@rabbit:5672/%2f")
			.build();

		final var contentSource = new RMQSource<String>(
			connectionConfig,
			"contentqueue",
			true,
			new SimpleStringSchema()
		);

		final var contentSink = new RMQSink<String>(
			connectionConfig,
			"observerqueue",
			new SimpleStringSchema()
		);

		final DataStream<String> content = env
			.addSource(contentSource)
			.setParallelism(1);

		var processFunc = new MapFunction<String, String>() {
			@Override
			public String map(String value) {
				return "processed";
			}
		};
		DataStream<String> detect = content.map(processFunc);
		detect.print();
		
		detect.addSink(contentSink);
		env.execute("Change Detector");
	}
}
