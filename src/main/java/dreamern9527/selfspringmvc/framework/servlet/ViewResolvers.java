package dreamern9527.selfspringmvc.framework.servlet;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewResolvers {

	private String viewName;
	private File file;
	
	protected ViewResolvers(String viewName, File file){
		this.viewName = viewName;
		this.file = file;
		
	}
	
	
	/**
	 * 解析ModelAndView
	 * @param modelAndView
	 * @return
	 * @throws Exception
	 */
	protected String parse(SelfModelAndView modelAndView) throws Exception{
		
		StringBuilder sb = new StringBuilder();
		RandomAccessFile ra = new RandomAccessFile(this.file, "r");
		
		try {
			// 模板框架的语法是非常复杂的，但是原理都是一样的
			// 无非都是用正则表达式来处理字符串而已
			String line = null;
			while (null != (line = ra.readLine())) {
				Matcher m = matcher(line);
				while (m.find()) {
					for (int i = 1; i <= m.groupCount(); i++) {
						String paramName = m.group(i);
						Object paramValue = modelAndView.getModel().get(paramName);
						if (null == paramValue) {
							continue;
						}
						line = line.replaceAll("@\\{" + paramName + "\\}", paramValue.toString());
					}
				}

				sb.append(line);
			} 
		} finally {
			ra.close();
		}
		return sb.toString();
	}

	public String getViewName() {
		return viewName;
	}
	
	private Matcher matcher(String str){
		Pattern pattern = Pattern.compile("@\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(str);
		return m;
	}
}
