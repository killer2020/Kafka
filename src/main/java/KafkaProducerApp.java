import java.util.List;
import java.util.Properties;

//import KafkaProducer packages
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
//import ProducerRecord packages
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

//Create java class named “SimpleProducer”
public class KafkaProducerApp
{

	public static void main(String[] args) throws Exception
	{

		String topicName = "topic_3";

		Properties props = setProducerProperties();

		Producer<String, String> producer = new KafkaProducer<String, String>(props);

		printPartitionsInfoOfTopic(topicName, producer);
		
		for (int i = 0; i < 20; i++)
		{
			String key = Integer.toString(i);
			String value = Integer.toString(i);
			
			syso("Key:" + key +" Value:"+value);
			syso("Probable partition : " + determinePartitionOfMessage(topicName, producer, key));
			
			sendToTopic(topicName, producer, key, value);
			int partition = 1;
//			sendToTopicAndPartition(topicName,partition, producer, key, value);
			syso("Message sent");
		}
		
		
		producer.close();
	}




	private static int determinePartitionOfMessage(String topicName, Producer<String, String> producer, String key)
	{
		return Utils.abs(Utils.murmur2(key.getBytes())) % getNumOfPartitions(topicName,producer);
	}
	

	private static void printPartitionsInfoOfTopic(String topicName, Producer<String, String> producer)
	{
		
		List<PartitionInfo> partitionsFor = producer.partitionsFor(topicName);
		
		for (PartitionInfo partitionInfo : partitionsFor)
		{
            syso(partitionInfo);			
		}
		
	}
	
	private static int getNumOfPartitions(String topicName, Producer<String, String> producer)
	{
		
		return producer.partitionsFor(topicName).size();
		
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