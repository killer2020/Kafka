import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

class MultithreadingDemo extends Thread
{
	public void run()
	{
		try
		{
			String topicName = "topic_3";

			Properties props = setProducerProperties();

			Producer<String, String> producer = new KafkaProducer<String, String>(props);

			for (int i = 0; i < 100; i++)
			{
				String key = Long.toString(Thread.currentThread().getId());
				String value = Integer.toString(i);
				
				syso("Key:" + key +" Value:"+value);
				
				sendToTopic(topicName, producer, key, value);
				int partition = 1;
//				sendToTopicAndPartition(topicName,partition, producer, key, value);
				
				syso("Message sent");
			}
			
			producer.close();

		} catch (Exception e)
		{
			System.out.println("Exception is caught");
		}
	}
	
	
	
	private static void syso(Object o)
	{
		System.out.println(o);
	}

	private static void sendToTopic(String topicName, Producer<String, String> producer, String key, String value)
	{
		producer.send(new ProducerRecord<String, String>(topicName, key, value));
	}
	
	private static void sendToTopicAndPartition(String topicName,int partition, Producer<String, String> producer, String key, String value)
	{
		
		producer.send(new ProducerRecord<String,String>(topicName,partition, key, value));
	}
	

	private static Properties setProducerProperties()
	{
		Properties props = new Properties();

		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");

		props.put("retries", 0);

		props.put("batch.size", 16384);

		props.put("linger.ms", 1);

		props.put("buffer.memory", 33554432);

		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return props;
	}
}

public class MultiThreadedKafkaProducer
{
	public static void main(String[] args)
	{
		int n = 8; // Number of threads
		for (int i = 0; i < 8; i++)
		{
			MultithreadingDemo object = new MultithreadingDemo();
			object.start();
		}
	}
	
	

}