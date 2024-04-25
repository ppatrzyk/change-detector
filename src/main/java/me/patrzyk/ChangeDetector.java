package me.patrzyk;

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
		String rabbit = System.getenv("RABBIT");
		String rabbitSourceQueue = System.getenv("RABBIT_SOURCE_QUEUE");
		String rabbitSinkQueue = System.getenv("RABBIT_SINK_QUEUE");

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
			.setUri(rabbit)
			.build();

		JsonDeserializationSchema<Entry> entrySchema = new JsonDeserializationSchema<>(Entry.class);

		final var contentSource = new RMQSource<Entry>(
			connectionConfig,
			rabbitSourceQueue,
			true,
			entrySchema
		);

		JsonSerializationSchema<ProcessedEntry> detectSchema = new JsonSerializationSchema<>();

		final var contentSink = new RMQSink<ProcessedEntry>(
			connectionConfig,
			rabbitSinkQueue,
			detectSchema
		);

		final DataStream<Entry> content = env
			.addSource(contentSource)
			.setParallelism(1);
		
		final KeyedStream<Entry, String> contentKeyed = content
			.keyBy(entry -> entry.getKey());

		DataStream<ProcessedEntry> detect = contentKeyed.flatMap(new CompareProcessFunction());

		detect.addSink(contentSink);

		env.execute("Change Detector");
	}
}
