package dreamern9527.selfspringmvc.framework.context;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


import dreamern9527.selfspringmvc.framework.annotation.SelfAutowired;
import dreamern9527.selfspringmvc.framework.annotation.SelfController;
import dreamern9527.selfspringmvc.framework.annotation.SelfService;


public class SelfApplicationContext {

	private Map<String, Object> intstanceMapping = new ConcurrentHashMap<String, Object>();
	
	// 类似于beanDefinitions ,类似于内部的配置信息，对外界是透明的
	// 我们能够看到的只有IOC容器，是通过getBean()方法来间接调用的
	private List<String> classCache = new ArrayList<String>();
	
	private Properties config = new Properties();
	
	public SelfApplicationContext(String location){
		
		// 先加载配置文件
		// IOC加载过程 定位、载入、注册、初始化、注入
		InputStream is = null;
		try {
			
			// 定位
			is = this.getClass().getClassLoader().getResourceAsStream(location);

			// 载入
			config.load(is);
			
			// 注册为beanDefintion,载入配置文件的依赖关系
			String packageName = config.getProperty("scanPackage");
			doRegister(packageName);
			
			//实例化需要IOC的对象，把配置文件信息以及注解信息装在IOC容器中
			doCreateBean();
			
			//注入
			populateBean();
			
			System.out.println("IOC 容器已经初始化完毕！");
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	// 把所有的符合条件的class全部找出来，注册到缓存中
	private void doRegister(String packageName){
		URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
		
		File dir = new File(url.getFile());
		for(File file : dir.listFiles()){
			// 如果是一个文件夹，继续递归
			if(file.isDirectory()){
				doRegister(packageName + "." + file.getName());
			} else {
				classCache.add(packageName + "." + file.getName().replaceAll(".class", ""));
			}
		}
	}
	
	// 初始化Bean
	private void doCreateBean(){
		// 检查看是否有注册信息,注册信息保存了所有的配置文件的class的名字
		// 例如BeanDefinition中保存了类的名字，以及类与类之间的关系（Map, List,ref,set,parent）
		if(classCache.size() == 0){ return;}
		
		try {
			for(String className : classCache){
				// 判断是jdk还是cglib interface判断
				Class<?> clazz = Class.forName(className);
				
				// 判断哪个类需要初始化，哪个类不需要初始化
				// 通过只要加了@Service, @Controller等注解都要进行初始化
				// Spring中的processArray方法
				if(clazz.isAnnotationPresent(SelfController.class)){
					//名字是类名首字母小写
					String id = lowerFirstChar(clazz.getSimpleName());
					intstanceMapping.put(id, clazz.newInstance());
				} else if(clazz.isAnnotationPresent(SelfService.class)){
					SelfService service = clazz.getAnnotation(SelfService.class);
					
					// 如果自定义了名字，就优先使用自己定义的名字
					String id = service.value();
					if(!"".equals(id.trim())){
						intstanceMapping.put(id, clazz.newInstance());
						continue;
					}
					// 如果是空的，就用默认规则
					// 1、类名首字母小写
					// 如果这个类是接口
					// 2、可以根据类型匹配
					Class<?>[] interfaces = clazz.getInterfaces();
					// 如果这个类实现了接口，就用接口的类型作为id
					for(Class<?> i : interfaces){
						intstanceMapping.put(i.getName(), clazz.newInstance());
					}
				} else {
					continue;
				}
				
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 依赖注入Bean
	 */
	private void populateBean(){
		// 首先判断IOC容器是否存在值
		// 取出IOC容器中已经注册的bean的关系
		if(intstanceMapping.isEmpty()){return;}
		
		for(Entry<String, Object> entry: intstanceMapping.entrySet()){
			Field [] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field field : fields){
				if(!field.isAnnotationPresent(SelfAutowired.class)){continue;}
				
				SelfAutowired autowired = field.getAnnotation(SelfAutowired.class);
				String id = autowired.value().trim();
				
				// 如果id为空，也就是说自己没有设置，那么默认根据类型进行注入
				if("".equals(id)){
					id = field.getType().getName();
				}
				
				// 把私有变量设置为开放访问权限
				field.setAccessible(true);
				
				try {
					field.set(entry.getValue(), intstanceMapping.get(id));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	public Object getBean(String name){
		return null;
	}
	
	public Map getAll(){
		return intstanceMapping;
	}
	
	private String lowerFirstChar(String str){
		char[] chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
	
	public Properties getConfig(){
		return config;
	}
}
