package cn.itcast.bos.web.action.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

import org.springframework.data.domain.Page;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

/**
 * 代码重构, 抽取Action的公共代码, 简化开发
 * @author tao
 *
 * @param <T>
 */
@SuppressWarnings("all")
public abstract class BaseAction<T> extends ActionSupport implements ModelDriven<T>  {

	//模型驱动 , 修饰符用protected 
	protected T model ;
	
	@Override
	public T getModel() {
		return model;
	}
	
	public BaseAction(){
		//this.getClass() 是获取子类的字节码  
		//getGenericSuperclass() 是获取父类的泛型
		Type genericSuperclass = this.getClass().getGenericSuperclass();
		
		//获取类型 的第一个参数类型
		ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
		
		Class<T> modelClass =  (Class<T>) parameterizedType.getActualTypeArguments()[0];
				
		 try {
			 //最终获取模型驱动的实例
			model = modelClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			System.err.println("模型构造失败...");
		}
	}
	
	//接收分页查询参数
	protected int page;
	protected int rows;

	public void setPage(int page) {
		this.page = page;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	
	protected void pushPageDataValueStack(Page<T> pageData){
		HashMap<String, Object> result = new HashMap<String , Object>();
		result.put("total", pageData.getTotalElements());
		result.put("rows", pageData.getContent());
		
		ActionContext.getContext().getValueStack().push(result);
	}
	
	
	
	
}
