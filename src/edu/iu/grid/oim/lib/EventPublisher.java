package edu.iu.grid.oim.lib;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import edu.iu.grid.oim.model.db.record.LogRecord;

public class EventPublisher {
	static Logger log = Logger.getLogger(EventPublisher.class);  
	
	Boolean post = false;
	
    public EventPublisher() {
    	String flag = StaticConfig.conf.getProperty("rabbitmq.postevent");
    	if(flag != null && flag.equals("true")) {
    		post = true;
    	}
    }
    public void publishLog(String type, String model_full, String xml) {
    	if(!post) {
    		log.debug("event:"+type+" model:"+model_full);
    		log.debug(xml);
    	} else {
			try {
				//connect to rabbitmq server
				ConnectionFactory factory = new ConnectionFactory();
				factory.setUsername(StaticConfig.conf.getProperty("rabbitmq.username"));
				factory.setPassword(StaticConfig.conf.getProperty("rabbitmq.pass"));
				factory.setVirtualHost(StaticConfig.conf.getProperty("rabbitmq.vhost"));
				factory.setHost(StaticConfig.conf.getProperty("rabbitmq.host"));
				Connection conn = factory.newConnection();
				Channel channel = conn.createChannel();
				
				//public message
				String exchange = StaticConfig.conf.getProperty("rabbitmq.exchange");
		        channel.exchangeDeclare(exchange, "topic");
				String [] model = model_full.split("\\.");
				String routing_key = model[model.length-1] + "." + type;
				channel.basicPublish(exchange, routing_key, null, xml.getBytes());
				
				//close it up
				channel.close();
				conn.close();
				
				log.debug("posted to oim event server");
			} catch (Exception e) {
				log.error("Failed to publish OIM event", e);
			}	
    	}
    }
}
