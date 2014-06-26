package org.z.store.kafka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.store.intf.DictInfo;

public class KafkaDict implements DictInfo {
	private static Logger logger = LoggerFactory.getLogger(KafkaDict.class);
	private static Producer<String, String> inner = null;
	@Override
	public boolean init() {
		Properties originalProps = new Properties();
		try {
			originalProps.load(ClassLoader.getSystemResourceAsStream("producer.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ProducerConfig config = new ProducerConfig(originalProps);
		inner = new Producer<String, String>(config);
		logger.info("init KafkaDict success");
		return true;
	}

	public static void send(String topic, Collection<String> message) {
		ArrayList<KeyedMessage<String, String>> messages = new ArrayList<KeyedMessage<String, String>>();
		for (String inster : message) {
			KeyedMessage<String, String> me = new KeyedMessage<>(topic, inster);
			messages.add(me);
		}
		inner.send(messages);
	}
}
