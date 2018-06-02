package cn.itcast.bos.web.action;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.domain.constant.Constants;
import cn.itcast.bos.domain.take_delivery.Order;
import cn.itcast.bos.web.action.common.BaseAction;
import cn.itcast.crm.domain.Customer;

@Namespace("/")
@ParentPackage("json-default")
@Controller
@Scope("prototype")
public class OrderAction extends BaseAction<Order> {

	// 属性驱动封装 收件人信息和 发件人信息
	private String sendAreaInfo;
	private String recAreaInfo;

	public void setSendAreaInfo(String sendAreaInfo) {
		this.sendAreaInfo = sendAreaInfo;
	}

	public void setRecAreaInfo(String recAreaInfo) {
		this.recAreaInfo = recAreaInfo;
	}
	
	@Action(value = "order_add", results = { @Result(name = "success", type = "redirect", location = "index.html") })
	public String add() {
		// 手动封装area关联
		Area sendArea = new Area();
		// 省市区地址是以 / 分隔的
		String[] sendAreaData = sendAreaInfo.split("/");
		// 收货地址 以 省市区 分隔

		// 封装 发货地址
		sendArea.setProvince(sendAreaData[0]);
		sendArea.setCity(sendAreaData[1]);
		sendArea.setDistrict(sendAreaData[2]);

		// 封装 收货地址
		String[] recAreaData = recAreaInfo.split("/");
		Area recArea = new Area();
		recArea.setProvince(recAreaData[0]);
		recArea.setCity(recAreaData[1]);
		recArea.setDistrict(recAreaData[2]);

		model.setSendArea(sendArea);
		model.setRecArea(recArea);
		// 订单关联客户
		// 从session 中 获取 客户
		Customer customer = (Customer) ServletActionContext.getRequest().getSession().getAttribute("customer");
		model.setCustomer_id(customer.getId());
		// 调用webService 把订单信息传递到bos_management
		WebClient.create(Constants.BOS_MANAGEMENT_URL + "/services/orderService/order").type(MediaType.APPLICATION_JSON)
				.post(model);
		return SUCCESS;
	}
	
	
}
