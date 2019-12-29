import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

public class KafkaConsumerApp
{

	public static void main(String[] args) throws Exception
	{
		String topicName = "topic_3";

		KafkaConsumer<String, String> consumer = createKafkaConsumer("localhost:9092", "console-consumer-25074");

		TopicPartition topicPartition_0 = new TopicPartition(topicName, 0);
		TopicPartition topicPartition_1 = new TopicPartition(topicName, 1);
		List<TopicPartition> partitionList = Arrays.asList(topicPartition_0);
		 
//		subscribeToPartition(consumer, partitionList);
		subscribeToTopic(topicName, consumer);

		System.out.println("Subscribed to topic " + topicName);

		syso("Consumer started:");
		
		
		int startingOffset = 1300;
//		startReadingFromOffset(consumer, topicPartition_1, startingOffset);
		
		while (true)
		{
			ConsumerRecords<String, String> records = consumer.poll(100);

			for (ConsumerRecord<String, String> record : records)
			{
				System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
			}
		}
	}

	private static void startReadingFromOffset(KafkaConsumer<String, String> consumer, TopicPartition topicPartition_0,
			int startingOffset)
	{
		dummyPollCallBeforeSeek(consumer);
		consumer.seek(topicPartition_0, startingOffset);
	}

	private static void dummyPollCallBeforeSeek(KafkaConsumer<String, String> consumer)
	{
		consumer.poll(1);
	}

	private static KafkaConsumer<String, String> createKafkaConsumer(String kafkaBrokers , String consumerGroup)
	{
		return new KafkaConsumer<String, String>(createKafkaConsumerProperties(kafkaBrokers, consumerGroup));
	}

	private static void subscribeToPartition(KafkaConsumer<String, String> consumer, List<TopicPartition> asList)
	{
		consumer.assign(asList);
	}

	private static void subscribeToTopic(String topicName, KafkaConsumer<String, String> consumer)
	{
		consumer.subscribe(Arrays.asList(topicName));
	}

	private static Properties createKafkaConsumerProperties(String kafkaBrokers , String consumerGroup)
	{
		Properties props = new Properties();

		props.put("bootstrap.servers", kafkaBrokers);
		props.put("group.id", consumerGroup);
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		return props;
	}

	private static void syso(Object string)
	{
		System.out.println(string);

	}
}