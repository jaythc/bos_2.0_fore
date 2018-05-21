package cn.itcast.bos.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;  
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class MailUtils {
	//设置发送的是网易邮箱
	private static String smtp_host = "smtp.163.com";
	//发件人的邮箱地址
	private static String from = "taohongchao@163.com"; // 使用当前账户
	private static String username = "taohongchao@163.com"; // 邮箱账户
	private static String password = "1048576thc"; // 邮箱授权码
	
	//项目激活邮箱地址
	public static String activeUrl = "http://localhost:9003/bos_fore/customer_activeMail.action";

	/**
	 * 发送邮件的静态方法. 
	 * @param subject 发送邮件的主题
	 * @param content 发送邮件的内容
	 * @param to 收件人
	 */
	public static void sendMail(String subject, String content, String to) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", smtp_host);
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.auth", "true");
		Session session = Session.getInstance(props);
		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.setRecipient(RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setContent(content, "text/html;charset=utf-8");
			Transport transport = session.getTransport();
			transport.connect(smtp_host, username, password);
			transport.sendMessage(message, message.getAllRecipients());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("邮件发送失败...");
		}
	}

	public static void main(String[] args) {
		sendMail("测试java邮件标题", "邮件内容java", "806832493@qq.com");
		System.out.println("成功");
	}
}
