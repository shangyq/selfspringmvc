package dreamern9527.selfspringmvc.framework.servlet;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dreamern9527.selfspringmvc.framework.annotation.SelfController;
import dreamern9527.selfspringmvc.framework.annotation.SelfRequestMapping;
import dreamern9527.selfspringmvc.framework.annotation.SelfRequestParam;
import dreamern9527.selfspringmvc.framework.context.SelfApplicationContext;

public class SelfDispatcherServlet extends HttpServlet{
	
	/**   **/
	private static final long serialVersionUID = 1L;

	private static final String LOCATION = "contextConfigLocation";
	
	// 正则匹配
//	private Map<Pattern, Handler> handlerMapping = new HashMap<Pattern, Handler>();
	
//	private Map<Handler, HandlerAdapter> adapterMapping = new HashMap<Handler, HandlerAdapter>();
	
	private List<HandlerAdapter> adapterMapping = new ArrayList<HandlerAdapter>();;
	
	
	private List<Handler> handlerMapping = new ArrayList<Handler>();
	
	private List<ViewResolvers> viewResolvers= new ArrayList<ViewResolvers>();


	// 初始化IOC容器
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("Spring MVC 初始化开始");
		
		// 容器必须要初始化才行
		// 假装IOC容器已经启动
		SelfApplicationContext context = new SelfApplicationContext(config.getInitParameter(LOCATION));
		
		// 请求解析
		initMultipartResolver(context);
		// 多语言国际化
		initLocaleResolver(context);
		// 主题VIEW层
		initThemeResolver(context);
		
		//-----重要----//
		// 解析url和Method的关联关系URM(URL RELATION MAPPING)
		initHandlerMappings(context);
		//适配器(匹配的过程)
		initHandlerAdapters(context);
		//-----重要----//
		
		//异常解析
		initHandlerExceptionResolvers(context);
		// 视图转发（根据视图名字匹配到具体的模板）
		initRequestToViewNameTranslator(context);
		
		//-----重要----//
		// 解析模板中的内容(拿到服务器传输过来的数据，生成html代码)
		initViewResolvers(context);
		//-----重要----//
		
		initFlashMapManager(context);
		
		System.out.println("Spring MVC 初始化完毕");

	}
	
	
	private void initMultipartResolver(SelfApplicationContext context){
		
	}
	
	// 多语言国际化
	private void initLocaleResolver(SelfApplicationContext context){};
	// 主题VIEW层
	private void initThemeResolver(SelfApplicationContext context){};
	
	// 解析url和Method的关联关系URM(URL RELATION MAPPING)
	private void initHandlerMappings(SelfApplicationContext context){
		Map<String, Object> ioc = context.getAll();
		if(ioc.isEmpty()){return;}
		
		// 只要是由Controller修饰的类，里面的方法全部找出来
		// 而且这个方法上应该加上RequestMapping，如果没加这个注解，那么这个方法是不能被外部访问的
		for(Entry<String, Object> entry : ioc.entrySet()){
			Class<?> clazz = entry.getValue().getClass();
			if(!clazz.isAnnotationPresent(SelfController.class)){continue;}
			
			String url = "";
			if(clazz.isAnnotationPresent(SelfRequestMapping.class)){
				SelfRequestMapping requestMapping = clazz.getAnnotation(SelfRequestMapping.class);
				url = requestMapping.value();
			}
			
			// 扫描controller下面的所有的方法
			// RequestMapping会配置一个url,那么一个url就对应一个方法，并将这个关系保存到Map中
			Method [] methods = clazz.getMethods();
			for(Method method : methods){
				if(!method.isAnnotationPresent(SelfRequestMapping.class)){continue;}
				SelfRequestMapping requestMapping = method.getAnnotation(SelfRequestMapping.class);
				// 去掉多余的斜杠
				String regex = (url + requestMapping.value()).replaceAll("/+", "/");
				Pattern pattern = Pattern.compile(regex);
				
				handlerMapping.add(new Handler(pattern, entry.getValue(), method));
				
				System.out.println("Mapping---"+regex+"---"+method.toString());
			}
			
			
		}
		
	};
	
	
	//初始化适配器(匹配的过程)
	// 主要是用来动态匹配我们的参数
	private void initHandlerAdapters(SelfApplicationContext context){
		if(handlerMapping.isEmpty()){return;}
		
		// 参数类型作为key,参数的索引作为值
		Map<String, Integer> paramMapping = new HashMap<String, Integer>();
		
		// 只需要取出具体的某个方法
		for(Handler handler : handlerMapping){
			// 把这个方法上面所有的参数全部获取到
			Class<?> [] paramsTypes = handler.method.getParameterTypes();
			
			// 参数是有顺序的，但是通过反射，是无法拿到我们参数的名字
			// 这里是匹配自定义参数列表
			for(int i = 0; i < paramsTypes.length; i++){
				Class<?> type = paramsTypes[i];
				
				if(type == HttpServletRequest.class || type == HttpServletResponse.class){
					paramMapping.put(type.getName(), i);
				}
				
			}
			
			// 这里是匹配Request和Response
			Annotation [][] pa = handler.method.getParameterAnnotations();
			for(int i = 0; i < pa.length; i++){
				for(Annotation a : pa[i]){
					if(a instanceof SelfRequestParam){
						String paramName = ((SelfRequestParam) a).value();
						if(!"".equals(paramName.trim())){
							paramMapping.put(paramName, i);
						}
					}
				}
			}
			
			
			adapterMapping.add(new HandlerAdapter(handler,paramMapping));
			paramMapping = new HashMap<String, Integer>();
		}
		
	}
	
	
	//异常解析
	private void initHandlerExceptionResolvers(SelfApplicationContext context){};
	// 视图转发（根据视图名字匹配到具体的模板）
	private void initRequestToViewNameTranslator(SelfApplicationContext context){};
	
	
	// 解析模板中的内容(拿到服务器传输过来的数据，生成html代码)
	private void initViewResolvers(SelfApplicationContext context){
		// 模板一般是不会方法webRoot下的，而是放在WEB-INF下，或者classes下，这样就避免了用户直接请求到模板
		// 获取配置文件中资源文件信息
		// 模板初始化主要做了两件事情：
		// 1. 加载模板的个数，存储和到缓存中
		// 2. 检查模板中的语法错误
		String templateRoot = context.getConfig().getProperty("templateRoot");
		
		String rootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
		File rootDir = new File(rootPath);
		for(File template : rootDir.listFiles()){
			viewResolvers.add(new ViewResolvers(template.getName(), template));
		}
		
		
	}
	
	private void initFlashMapManager(SelfApplicationContext context){};

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	// 这里调用自己写的Controller
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500 Error, Msg: "+ Arrays.toString(e.getStackTrace()));
			// TODO: handle exception
		}
	}
	
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
		
		try {
			// 获取Handler，从HandlerMapping中获取
			Handler handler = getHandler(req);
			if (handler == null) {
				resp.getWriter().write("404 Not found");
				return;
			}
			// 获取HandlerAdapter适配器
			// 再由适配器去调用我们具体的方法
			HandlerAdapter ha = getHandlerAdapter(handler);
			
			SelfModelAndView mv= ha.handle(req, resp, handler);
			
			// 写一个模板框架
			// 比如@{name}
			applyDefaultViewName(resp, mv);
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}
	
	/**
	 * 根据用户设置的值，找到一个对应的模板
	 * @param resp
	 * @param mv
	 * @throws Exception 
	 */
	public void applyDefaultViewName(HttpServletResponse resp, SelfModelAndView mv) throws Exception{
		if(null == mv){return;}
		if(viewResolvers.isEmpty()){return;}
		for(ViewResolvers resolvers : viewResolvers){
			if(!mv.getView().equals(resolvers.getViewName())){continue;}
			
			String rtnObj = resolvers.parse(mv);
			if(rtnObj != null){
				resp.getWriter().write(rtnObj);
			}
		}
		
	}
	
	/**
	 * 循环HandlerMapping获取Handler
	 * @param req
	 * @return
	 */
	private Handler getHandler(HttpServletRequest req){
		if(handlerMapping.isEmpty()){
			return null;
		}
		
		// 获取请求的url，根据正则表达式进行匹配
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		for(Handler handler : handlerMapping){
			Matcher matcher = handler.pattern.matcher(url);
			if(!matcher.matches()){continue;}
			
			return handler;
		}
		
		return null;

	}
	
	
	/**
	 * 获取处理器适配器
	 * @param handler
	 * @return
	 */
	private HandlerAdapter getHandlerAdapter(Handler handler){
		if(handlerMapping.isEmpty()){return null;}
		
		for(HandlerAdapter handlerAdapter : adapterMapping){
			if(handlerAdapter.handler == handler){
				return handlerAdapter;
			}
		}
		
		return null;
	}
}
