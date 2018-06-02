package cn.itcast.bos.web.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bos.domain.constant.Constants;
import cn.itcast.bos.domain.page.PageBean;
import cn.itcast.bos.domain.take_delivery.Promotion;
import cn.itcast.bos.web.action.common.BaseAction;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Namespace("/")
@ParentPackage("json-default")
@Controller
@Scope("prototype")
@SuppressWarnings("all")
public class PromotionAction extends BaseAction<Promotion> {

	// 前端显示 活动页面
	@Action(value = "promotion_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {
		// 从后台工程获取 活动数据
		PageBean<Promotion> pageBean = WebClient.create(Constants.BOS_MANAGEMENT_HOST
				+ "/bos_management/services/promotionService/pageQuery?page=" + page + "&rows=" + rows)
				.accept(MediaType.APPLICATION_JSON).get(PageBean.class);
		ActionContext.getContext().getValueStack().push(pageBean);
		return SUCCESS;
	}

	/*	测试获取活动分页的
	 * public static void main(String[] args) {
		PageBean<Promotion> pageBean = WebClient.create(Constants.BOS_MANAGEMENT_URL
				+ "/bos_management/services/promotionService/pageQuery?page=" + 1 + "&rows=" + 10)
				.accept(MediaType.APPLICATION_JSON).get(PageBean.class);
		System.out.println(pageBean);
	}*/
	
	// 测试活动详情的
	public static void main(String[] args) {
		Promotion promotion = WebClient.create(Constants.BOS_MANAGEMENT_HOST
				+ "/bos_management/services/promotionService/promotion/"+42)
				.accept(MediaType.APPLICATION_JSON).get(Promotion.class);
		System.out.println(promotion);
	}
	
	//  显示活动详情
	@Action(value = "promotion_showDetail")
	public String showDetail() throws IOException, TemplateException {
		// 获取 生成的模板的文件夹的磁盘路径
		String htmlRealPath = ServletActionContext.getServletContext().getRealPath("/freemarker");
		
		// 以活动的id为名字, 以html为后缀, 以模板 生成文件
		File htmlFile = new File(htmlRealPath+"/"+model.getId()+".html");
		
		if (!htmlFile.exists()) {
			Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
			// 获取模板的路径
			File templateFile = new File(ServletActionContext.getServletContext().getRealPath("/WEB-INF/freemarker_templates"));
			
			configuration.setDirectoryForTemplateLoading(templateFile);
			
			// 获取模板对象
			Template template = configuration.getTemplate("promotion_detail.ftl");
			
			//  利用webService 获取动态的详情数据
			Promotion promotion = WebClient.create(Constants.BOS_MANAGEMENT_HOST
					+ "/bos_management/services/promotionService/promotion/"+model.getId())
					.accept(MediaType.APPLICATION_JSON).get(Promotion.class);
			
			HashMap<String, Object> parameterMap = new HashMap<String,Object>();
			parameterMap.put("promotion", promotion);
			
			//合并输出 , 产生 html 文件 
			template.process(parameterMap,new OutputStreamWriter(new FileOutputStream(htmlFile)));
		}
		
		ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
		// 把生成的html页面, 写回给浏览器
		FileUtils.copyFile(htmlFile, ServletActionContext.getResponse().getOutputStream());
		
		return NONE;
	}
	
	
	
}
