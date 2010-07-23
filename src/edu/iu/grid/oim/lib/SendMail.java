package edu.iu.grid.oim.lib;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

	private String from;
	private ArrayList<String> to;
	private String subject;
	private String text;
	
	public SendMail(String from, ArrayList<String> to, String subject, String text){
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.text = text;
	}
	
	public void send() throws MessagingException{
		
		Properties props = new Properties();
		props.put("mail.smtp.host", "localhost");
		props.put("mail.smtp.port", "25");
		//props.put("mail.debug", true);
		
		Session mailSession = Session.getDefaultInstance(props);
		Message simpleMessage = new MimeMessage(mailSession);
		
		InternetAddress fromAddress = null;
		fromAddress = new InternetAddress(from);
		
		InternetAddress toAddress[] = new InternetAddress[to.size()];
		int to_count = 0;
		for(String _to : to) {
			toAddress[to_count] = new InternetAddress(_to);
			++to_count;
		}

		simpleMessage.setFrom(fromAddress);
		simpleMessage.setRecipients(RecipientType.TO, toAddress);
		simpleMessage.setSubject(subject);
		simpleMessage.setText(text);
			
		Transport.send(simpleMessage);				
	}
	
	static public void sendErrorEmail(String content) throws MessagingException {
		String from = "no-reply@oim.grid.iu.edu";
		ArrayList<String> tos = new ArrayList<String>();
		//tos.add("hayashis@indiana.edu");
		//tos.add("8126067104@txt.att.net");
		tos.add("goc-alert@googlegroups.com");
		String subject = "[oim][critical] error";
		String message = "OIM has detected an error\r\n" + content;

		SendMail sendMail = new SendMail(from, tos, subject, message);
		sendMail.send();
	}
}
