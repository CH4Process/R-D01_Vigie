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

import com.ch4process.properties.PropertiesReader;

import javax.mail.internet.InternetAddress;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;



/**
 * @author Alex
 *
 */
public class Mail extends Thread
{
	private String config = "mail.properties";
	
	private final static int AUTH_NONE = 0;
	private final static int AUTH_SSL = 1;
	private final static int AUTH_TLS = 2;
	private Session session;
	private Properties properties;
	private Message message;
	
	private String smtp_host;
	private Integer authenticationType;
	private String port;
	private String from;
	private String to;
	private String username;
	private String password;
	private String subject;
	private String text;
	
	private boolean busy = false;

	
	public Mail()
	{
		try
		{
			PropertiesReader propReader = new PropertiesReader();
			Properties prop = propReader.getPropValues(config);
			
			this.smtp_host = prop.getProperty("host");
			this.authenticationType = Integer.valueOf(prop.getProperty("authentication"));
			this.username = prop.getProperty("username");
			this.password = prop.getProperty("password");
			this.port = prop.getProperty("port");
			
			prop = null;
			propReader = null;
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getSmtp_host()
	{
		return smtp_host;
	}

	public void setSmtp_host(String smtp_host)
	{
		this.smtp_host = smtp_host;
	}

	public Integer getAuthenticationType()
	{
		return authenticationType;
	}

	public void setAuthenticationType(Integer authenticationType)
	{
		this.authenticationType = authenticationType;
	}

	public String getFrom()
	{
		return from;
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public String getTo()
	{
		return to;
	}

	public void setTo(String to)
	{
		this.to = to;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
	
	public boolean isBusy()
	{
		return busy;
	}

	public void start()
	{
		super.start();
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private boolean init()
	{
		properties = new Properties();
		properties.put("mail.smtp.host", smtp_host);
		properties.put("mail.smtp.timeout", 10000);
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
	
	private void securityProtocol(int authenticationType)
	{
		switch (authenticationType)
		{
		case AUTH_NONE : 
		{
			properties.put("mail.smtp.auth", "false");
			break;
		}
		case AUTH_TLS :
		{
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.port", port);
			properties.put("mail.smtp.ssl.trust", smtp_host);
			break;
		}
		case AUTH_SSL :
		{
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.smtp.port", port);
			properties.put("mail.smtp.ssl.trust", smtp_host);
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
	public boolean sendMail()
	{
		try 
		{
			busy = true;
			init();
			Transport.send(message);
			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			busy = false;
		}
	}
	
	/**
	 * @param paths - String containing the path to the file to attach. Multiple strings can be input "folder/file1","folder/file2" etc.. 
	 * @return
	 */
	public boolean addAttachments(String...paths)
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
	public boolean addAttachmentsFolder(String folderPath)
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
