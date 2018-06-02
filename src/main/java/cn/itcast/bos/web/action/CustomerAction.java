package cn.itcast.bos.web.action;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Controller;

import cn.itcast.bos.domain.constant.Constants;
import cn.itcast.bos.utils.MailUtils;
import cn.itcast.bos.web.action.common.BaseAction;
import cn.itcast.crm.domain.Customer;

@Namespace("/")
@ParentPackage("json-default")
@Controller
@Scope("prototype")
public class CustomerAction extends BaseAction<Customer> {
	private static final long serialVersionUID = 1L;

	// 注入 jmsTemplate 使用消息队列
	@Autowired
	@Qualifier("jmsQueueTemplate")
	private JmsTemplate jmsTemplate;

	// 发送短信
	@Action(value = "customer_sendSms")
	public String sendSms() {
		// 生成随机的验证码
		final String randomCode = RandomStringUtils.randomNumeric(4);
		// 将短信验证码保存到session , 以便后面验证
		ServletActionContext.getRequest().getSession().setAttribute(model.getTelephone(), randomCode);
		System.err.println("生成的验证码为: " + randomCode);
		// MQ生产者调用服务,队列 发送短信
		jmsTemplate.send("bos_sms", new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException { // 建立一个Map类型的
				MapMessage mapMessage = session.createMapMessage(); // 存入电话
				mapMessage.setString("telephone", model.getTelephone()); // 存入随机的验证码
				mapMessage.setString("randomCode", randomCode);
				return mapMessage;
			}
		});
		return NONE;
	}
	// 属性驱动, 获取验证码
	private String checkCode;
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	// 注入RedisTemplate处理激活邮件
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	// 用户注册绑定邮箱   的方法
	@Action(value = "customer_regist", results = {
			@Result(name = "success", type = "redirect", location = "signup-success.html"),
			@Result(name = "input", type = "redirect", location = "signup.html") })
	public String regist() {
		// 验证 验证码是否正确
		String checkCodeSession = (String) ServletActionContext.getRequest().getSession()
				.getAttribute(model.getTelephone());
		if (checkCodeSession == null || !checkCodeSession.equals(checkCode)) {
			System.err.println("验证码错误");
			return INPUT;
		}
		// 利用webService 保存用户信息
		WebClient.create(Constants.CRM_MANAGEMENT_URL + "/services/customerService/savecustomer")
				.type(MediaType.APPLICATION_JSON).post(model);
		System.err.println("用户注册成功");
		// 注册成功后, 激活邮箱 . 生成32位的激活码
		String activeCode = RandomStringUtils.randomNumeric(32);
		// 激活码存入redis, 设置24小时有效期, 以客户的电话号码为键
		redisTemplate.opsForValue().set(model.getTelephone(), activeCode, 24, TimeUnit.HOURS);
		/*
		 * 写入邮件的内容 br为换行 href为拼接的a标签的点击地址 MailUtils.activeUrl 为定义的请求路径
		 * 注意端口号要于定义的一致
		 * http://localhost:9003/bos_fore/customer_activeMail.action 通过? 和 & 拼接
		 * 参数
		 */
		String content = "尊敬的客户您好，请于24小时内，进行邮箱账户的绑定，点击下面地址完成绑定:<br/><a href='" + MailUtils.activeUrl + "?telephone="
				+ model.getTelephone() + "&activeCode=" + activeCode + "'>速运快递邮箱绑定地址</a>";
		// 发送邮件
		MailUtils.sendMail("速运快递激活邮件", content, model.getEmail());
		return SUCCESS;
	}

	// 属性驱动, 获取激活码
	private String activeCode;

	public void setActiveCode(String activeCode) {
		this.activeCode = activeCode;
	}

	// 激活邮箱
	@Action("customer_activeMail")
	public String activeEmail() throws IOException {
		ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
		// 从redis中,获取激活码
		String activeCodeRedis = redisTemplate.opsForValue().get(model.getTelephone());
		if (activeCodeRedis != null && activeCodeRedis.equals(activeCode)) {
			// 激活码正确 . 为了防止重复绑定, 先webService 查询客户信息, 判断是否已经绑定邮箱
			Customer customer = WebClient.create(Constants.CRM_MANAGEMENT_URL
					+ "/services/customerService/customer/telephone/" + model.getTelephone())
					.accept(MediaType.APPLICATION_JSON).get(Customer.class);
			if (customer != null) {
				if (customer.getType() != null && customer.getType() == 1) {
					// 邮箱已经绑定过
					ServletActionContext.getResponse().getWriter().println("邮箱已经绑定, 请勿重复绑定!!!");
				} else {
					// 没有绑定邮箱, 进行绑定
					WebClient.create(Constants.CRM_MANAGEMENT_URL + "/services/customerService/customer/updatetype/"
							+ model.getTelephone()).put(null);
					ServletActionContext.getResponse().getWriter().println("邮箱绑定成功!!!");
				}
			}
			// 删除 redis 中 的 激活码
			redisTemplate.delete(model.getTelephone());
		} else {
			ServletActionContext.getResponse().getWriter().println("激活码失效 !!!");
		}
		return NONE;
	}

	public static void main(String[] args) {
		Customer customer = WebClient
				.create(Constants.CRM_MANAGEMENT_URL + "/services/customerService/customer/login?telephone="
						+ "13071256499" + "&password=" + "123456")
				.accept(MediaType.APPLICATION_JSON).get(Customer.class);
		System.out.println(customer);
	}

	// 用户登陆
	@Action(value = "customer_login", results = { @Result(name = "login", location = "login.html", type = "redirect"),
			@Result(name = "success", location = "index.html#/myhome", type = "redirect") })
	public String login() {
		Customer customer = WebClient
				.create(Constants.CRM_MANAGEMENT_URL + "/services/customerService/customer/login?telephone="
						+ model.getTelephone() + "&password=" + model.getPassword())
				.accept(MediaType.APPLICATION_JSON).get(Customer.class);
		if (customer == null) {
			// 登陆失败 , 跳转到登陆页面 ,重新登陆
			return LOGIN;
		} else {
			// 登陆 成功, 把用户存入 session
			ServletActionContext.getRequest().getSession().setAttribute("customer", customer);
			return SUCCESS;
		}
	}

}
