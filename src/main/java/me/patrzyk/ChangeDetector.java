package me.patrzyk;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
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

		JsonDeserializationSchema<Entry> entrySchema = new JsonDeserializationSchema<>(Entry.class);

		final var contentSource = new RMQSource<Entry>(
			connectionConfig,
			"contentqueue",
			true,
			entrySchema
		);

		JsonSerializationSchema<ProcessedEntry> detectSchema = new JsonSerializationSchema<>();

		final var contentSink = new RMQSink<ProcessedEntry>(
			connectionConfig,
			"observerqueue",
			detectSchema
		);

		final DataStream<Entry> content = env
			.addSource(contentSource)
			.setParallelism(1);
		
		final KeyedStream<Entry, String> contentKeyed = content
			.keyBy(entry -> entry.getKey());

		DataStream<ProcessedEntry> detect = contentKeyed.flatMap(new CompareProcessFunction());
		detect.addSink(contentSink);

		contentKeyed.print();
		detect.print();

		env.execute("Change Detector");
	}
}
