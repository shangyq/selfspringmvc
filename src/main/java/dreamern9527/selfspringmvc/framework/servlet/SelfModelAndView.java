package dreamern9527.selfspringmvc.framework.servlet;

import java.util.Map;

public class SelfModelAndView {

	private String view;
	
	private Map<String, Object> model;
	
	public SelfModelAndView(String view){
		this.view = view;
	}
	
	public SelfModelAndView(String view, Map<String, Object> model){
		this.view = view;
		this.model = model;
	}

	public String getView() {
		return view;
	}

	public Map<String, Object> getModel() {
		return model;
	}
	
	
}
