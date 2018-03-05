package dreamern9527.selfspringmvc.demo.mvc.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dreamern9527.selfspringmvc.demo.service.INameService;
import dreamern9527.selfspringmvc.demo.service.IService;
import dreamern9527.selfspringmvc.framework.annotation.SelfAutowired;
import dreamern9527.selfspringmvc.framework.annotation.SelfController;
import dreamern9527.selfspringmvc.framework.annotation.SelfRequestMapping;
import dreamern9527.selfspringmvc.framework.annotation.SelfRequestParam;
import dreamern9527.selfspringmvc.framework.annotation.SelfResponseBody;
import dreamern9527.selfspringmvc.framework.servlet.SelfModelAndView;

@SelfController
@SelfRequestMapping("/web")
public class FirstAction {

	@SelfAutowired
	private IService service;
	
	@SelfAutowired
	private INameService nameService;
	
	@SelfRequestMapping("/query/.*.json")
	@SelfResponseBody
	public SelfModelAndView query(HttpServletRequest request, HttpServletResponse response,
		 @SelfRequestParam(value="name", required=false) String name,
		 @SelfRequestParam(value="addr", required=false) String addr){
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", name);
		model.put("addr", addr);
		
		return new SelfModelAndView("first.selfml",model);
	}
	
	@SelfRequestMapping("/add.json")
	@SelfResponseBody
	public SelfModelAndView query(HttpServletRequest request, HttpServletResponse response){
		
		out(response, "this is out json");
		return null;
	}
	
	@SelfRequestMapping("/show/.*.json")
	@SelfResponseBody
	public void show(HttpServletRequest request, HttpServletResponse response,
		 @SelfRequestParam(value="show", required=false) String show){
		
		out(response, "get params show = "+ show);
		
	}
	
	public void out(HttpServletResponse response, String str){
		try {
			response.getWriter().write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
