package com.ch4process.email;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.InternetAddress;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;



/**
 * @author Alex
 *
 */
public class Mail 
{
	private final static int AUTH_NONE = 0;
	private final static int AUTH_SSL = 1;
	private final static int AUTH_TLS = 2;
	private static Session session;
	private static Properties properties;
	private static Message message;

	/**
	 * @param smtp_host : address of the SMTP host
	 * @param authenticationType : 0 = No Authentication / 1 = SLL / 2 = TLS / Wrong parameter ends in No Authentication
	 * @param from : Address of the sender
	 * @param to : Addresses of the recipients separated by a coma
	 * @param username : Username of the sender for authentication on the server
	 * @param password : Password of the sender for authentication on the server
	 * @param subject : Subject of the email to send
	 * @param text : Body of the email
	 * @return
	 */
	public static boolean init(String smtp_host, Integer authenticationType, String from, String to, String username, String password, String subject, String text)
	{
		properties = new Properties();
		properties.put("mail.smtp.host", smtp_host);
		//properties.put("mail.smtp.timeout", 10000);
		securityProtocol(authenticationType);
		
		Authenticator authenticator = new Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(username, password);
			}
		};
		
		session = Session.getDefaultInstance(properties, authenticator);
		
		try 
		{
			message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(text);
			
			return true;		
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private static void securityProtocol(int authenticationType)
	{
		switch (authenticationType)
		{
		case AUTH_NONE : 
		{
			properties.put("mail.smtp.auth", "false");
			break;
		}
		case AUTH_SSL :
		{
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.port", "587");
			properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
			break;
		}
		case AUTH_TLS :
		{
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.smtp.port", "465");
			properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
			break;
		}
		default :
		{
			properties.put("mail.smtp.auth", "false");
			break;
		}
		}
	}

	/**
	 * @return true if mail successfully sent and false otherxise
	 */
	public static boolean sendMail()
	{
		try 
		{
			Transport.send(message);
			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @param paths - String containing the path to the file to attach. Multiple strings can be input "folder/file1","folder/file2" etc.. 
	 * @return
	 */
	public static boolean addAttachments(String...paths)
	{
		try
		{
			Multipart multipart = new MimeMultipart();
			
			for (String element : paths) 
			{
				
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(element);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(element.substring(element.lastIndexOf("/")));
				multipart.addBodyPart(messageBodyPart);		
			}
			
			message.setContent(multipart);
			return true;
		}
		catch (Exception e)
		{
			e.getMessage();
			return false;
		}
	}
	
	/**
	 * @param folderPath - Path to a folder. All CSV files will be sent.
	 * @return
	 */
	public static boolean addAttachmentsFolder(String folderPath)
	{
		try
		{
			File folder = new File(folderPath);
			File[] files = folder.listFiles();
			
			Multipart multipart = new MimeMultipart();
			
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].toString().endsWith(".csv"))
				{
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(files[i]);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(files[i].getName());
					multipart.addBodyPart(messageBodyPart);
				}
			}
			message.setContent(multipart);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
