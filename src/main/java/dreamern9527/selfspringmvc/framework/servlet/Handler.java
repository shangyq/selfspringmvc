package dreamern9527.selfspringmvc.framework.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class Handler {

	// 加了@Controller注解的类的对象
	protected Object controller;
	protected Method method;
	protected Pattern pattern;
	
	
	protected Handler(Pattern pattern, Object controller, Method method){
		this.controller = controller;
		this.method = method;
		this.pattern = pattern;
	}
}
