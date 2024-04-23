package me.patrzyk;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
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

		JsonDeserializationSchema<Entry> entrySchema = new JsonDeserializationSchema<>(Entry.class);

		final var contentSource = new RMQSource<Entry>(
			connectionConfig,
			"contentqueue",
			true,
			entrySchema
		);

		JsonSerializationSchema<Detect> detectSchema = new JsonSerializationSchema<>();

		final var contentSink = new RMQSink<Detect>(
			connectionConfig,
			"observerqueue",
			detectSchema
		);

		final DataStream<Entry> content = env
			.addSource(contentSource)
			.setParallelism(1);

		var processFunc = new MapFunction<Entry, Detect>() {
			@Override
			public Detect map(Entry entry) {
				var ts = entry.getTs();
				var content = entry.getContent();
				var detect = new Detect(ts, ts, "todo");
				return detect;
			}
		};
		DataStream<Detect> detect = content.map(processFunc);
		detect.addSink(contentSink);

		content.print();
		detect.print();

		env.execute("Change Detector");
	}
}
