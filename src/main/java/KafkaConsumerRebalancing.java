import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

class ProducerThread extends Thread
{
	
	private String topicName;


	public ProducerThread(String topicName)
	{
		this.topicName = topicName;
	}
	public void run()
	{
		try
		{

			Properties props = setProducerProperties();

			Producer<String, String> producer = new KafkaProducer<String, String>(props);

			for (int i = 0; i < 10000; i++)
			{
				String key = Long.toString(Thread.currentThread().getId());
				String value = Integer.toString(i);
				
//				sendToTopic(topicName, producer, key, value);
				int partition = 1;
				sendToTopicAndPartition(topicName,i%2, producer, key, value);
				
				syso("Message sent from Producer:" + value);
				Thread.sleep(500);
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

class ConsumerThread extends Thread
{
	
	private String topicName;
	private String consumerGroup;


	public ConsumerThread(String name ,String topicName, String consumerGroup)
	{
		super(name);
		this.topicName = topicName;
		this.consumerGroup = consumerGroup;
	}
	public void run()
	{
		try
		{

			KafkaConsumer<String, String> consumer = createKafkaConsumer("localhost:9092", consumerGroup);

			TopicPartition topicPartition_0 = new TopicPartition(topicName, 0);
			TopicPartition topicPartition_1 = new TopicPartition(topicName, 1);
			List<TopicPartition> partitionList = Arrays.asList(topicPartition_0);
			 
			subscribeToTopic(topicName, consumer);
			System.out.println("Subscribed to topic " + topicName);
			syso("Consumer started:");
			
//			startReadingFromBeginning(consumer,partitionList);
			
			while (true)
			{
				ConsumerRecords<String, String> records = consumer.poll(100);

				for (ConsumerRecord<String, String> record : records)
				{
					syso(this.getName() +" recieved value :" + record.value());
				}
			}
			
			

		} catch (Exception e)
		{
			System.out.println("Exception is caught");
		}
	}
	
	
	private static void subscribeToTopic(String topicName, KafkaConsumer<String, String> consumer)
	{
		consumer.subscribe(Arrays.asList(topicName));
	}
	private static KafkaConsumer<String, String> createKafkaConsumer(String kafkaBrokers , String consumerGroup)
	{
		return new KafkaConsumer<String, String>(createKafkaConsumerProperties(kafkaBrokers, consumerGroup));
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

public class KafkaConsumerRebalancing
{
	public static void main(String[] args) throws InterruptedException
	{
		String topicName = "topic_p2_r1";
		
		String bootStrapServers = "localhost:9092";
		Properties properties = setAdminProperties(bootStrapServers);
		AdminClient adminClient = AdminClient.create(properties);

		if(!checkTopicExsist(bootStrapServers,topicName))
        {
			createNewTopic(topicName, adminClient, 2, (short)1);
    		
        }
        
        adminClient.close();
		
		
        
		
//		ConsumerThread consumer1 = new ConsumerThread("consumer1" , topicName, "random consumer group");
//		consumer1.start();
//		
//		
//		syso("Starting another consumer in same group now...");
//		
//		ConsumerThread consumer2 = new ConsumerThread("consumer2" , topicName, "random consumer group");
//		consumer2.start();
		
		ProducerThread producer = new ProducerThread(topicName);
		producer.start();
        
		
		
	}


	private static void createNewTopic(String topicName, AdminClient adminClient, int numPartitions, short replicas)
	{
		NewTopic newTopic = new NewTopic(topicName, numPartitions, replicas);

		List<NewTopic> newTopics = new ArrayList<NewTopic>();
		newTopics.add(newTopic);
		adminClient.createTopics(newTopics);
		
		syso(topicName + " topic created.");
	}


	private static boolean checkTopicExsist(String bootStrapServers, String topicName)
	{
		Properties props = new Properties();
		props.put("bootstrap.servers", bootStrapServers);
		props.put("group.id", "test-consumer-group");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		Map<String, List<PartitionInfo>> listTopics = consumer.listTopics();
		
		if(listTopics.containsKey(topicName))
		{
			syso(topicName + " topic exsists.");
			return true;
		}
		
		return false;
	}


	private static void deleteTopic(AdminClient adminClient, String topicToBeDeleted)
	{
		List<String> topicsToBeDeleted = Arrays.asList(new String[] {topicToBeDeleted});
		adminClient.deleteTopics(topicsToBeDeleted);
		syso(topicToBeDeleted + " topic deleted.");
	}
	
	
	private static Properties setAdminProperties(String bootStrapServers)
	{
		Properties props = new Properties();

		props.put("bootstrap.servers", bootStrapServers);
		props.put("group.id", "test");
		props.put("enable.auto.commit", true);
		props.put("auto.commit.interval.ms", 1000);

		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		
		return props;
	}
	
	private static void syso(Object o)
	{
		System.out.println(o);
	}
}	
