package dreamern9527.selfspringmvc.framework.servlet;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HandlerAdapter {

	private Map<String, Integer> paramMapping;
	
	protected Handler handler;
	
	public HandlerAdapter(Handler handler, Map<String, Integer> paramMapping) {
		this.paramMapping = paramMapping;
		this.handler = handler;
	}

	// 主要目的是用反射调用url对应的method
	public SelfModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Handler handler) throws Exception{
		// 为什么要传request,response,handler
		Class<?> [] paramTypes = handler.method.getParameterTypes();
		
		// 要想给参数赋值，只能通过索引号来找到具体的某个参数
		Object [] paramValues = new Object[paramTypes.length];
		
		Map<String, String[]> params = req.getParameterMap();
		for(Entry<String, String[]> param : params.entrySet()){
			String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
			if(!this.paramMapping.containsKey(param.getKey())){
				continue;
			}

			
			// 单个赋值不行
			int index = this.paramMapping.get(param.getKey());
			paramValues[index] = castStringValue(value, paramTypes[index]);
		}
		
		//request 和 response 要赋值
		String reqName = HttpServletRequest.class.getName();
		if(this.paramMapping.containsKey(reqName)){
			int reqIndex = this.paramMapping.get(reqName);
			paramValues[reqIndex] = req;
		}
		
		
		String resqName = HttpServletResponse.class.getName();
			if(this.paramMapping.containsKey(resqName)){
			int respIndex = this.paramMapping.get(resqName);
			paramValues[respIndex] = resp;
		}
		
		boolean isModelAndView = handler.method.getReturnType() == SelfModelAndView.class;
		Object rtnObj = handler.method.invoke(handler.controller, paramValues);
		if(isModelAndView){
			return (SelfModelAndView) rtnObj;
		} else {
			return null;
		}
	}
	
	
	private Object castStringValue(String value,Class<?> clazz){
		if(clazz == String.class){
			return value;
		}else if(clazz == Integer.class){
			return Integer.valueOf(value);
		}else if(clazz == int.class){
			return Integer.valueOf(value).intValue();
		}else{
			return null;
		}
	}
}
