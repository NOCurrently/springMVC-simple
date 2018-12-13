/**
 * 
 */
package springframwork.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import springframwork.annotation.XiaoAutowired;
import springframwork.annotation.XiaoCompont;
import springframwork.annotation.XiaoController;
import springframwork.annotation.XiaoRequestMapping;
import springframwork.annotation.XiaoServer;
import springframwork.exception.Ex;
import springframwork.until.Assert;
import test.web.TestAction;

/**
 *
 * @author 肖超
 * @date: 2018年12月12日
 */
public class XiaoServlet extends HttpServlet {
	private Set<String> className = new HashSet<String>();
	Map<String, Object> aliseName = new HashMap<String, Object>();
	Map<String, Method> handleMapping = new HashMap<String, Method>();
	Properties configurationFiles = new Properties();
	/**
	 * 
	 */
	private static final long serialVersionUID = 18828867147641980L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			String initParameter = config.getInitParameter("config");
			// 获取web.xml的配置文件名字
			getConfig(initParameter);
			String scannerPageake = configurationFiles.getProperty("scanner.pageckag");
			if (scannerPageake == null || scannerPageake.trim().equals("")) {
				System.out.println("scannerPageake未配置，采用默认值");
				scannerPageake = "";
				configurationFiles.setProperty("scanner.pageckag", scannerPageake);
			}
			// 加载扫描包下的所有类的类名
			doScanner(scannerPageake);
			System.out.println(className);
			// 加载server类和controll类
			doinstent();
			System.err.println(aliseName);
			// 注入类
			doAutowir();
			// 加载handelmapping的类和url的关系
			doHandel();
			System.out.println(handleMapping);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {

			String requestURI = req.getRequestURI();
			String contextPath = req.getContextPath();
			String requestPath = requestURI.replaceAll(contextPath, "");
			String standardPath = requestPath.replaceAll("/+", "/");
			// 获取对应的method
			Method method = handleMapping.get(standardPath);
			if (method == null) {
				resp.getOutputStream().write("404".getBytes());
				return;
			}
			// 获取请求参数
			Map<String, String[]> parameterMap = req.getParameterMap();
			// 获取方法的参数
			Parameter[] parameters = method.getParameters();
			// 获取方法对应的class对象
			Object clazz = aliseName.get(method.getDeclaringClass().getSimpleName());
			// 请求参数转换成方法参数
			Object[] methodArg = getMethodArg(parameterMap, parameters);
			// 执行方法
			Object invoke = method.invoke(clazz, methodArg);
			resp.getOutputStream().write(invoke.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw, true);
//			e.printStackTrace(pw);
//			pw.flush();
//			sw.flush();
//			resp.getOutputStream().write(sw.toString().getBytes());
			throw new Ex();
		}
	}

	/**
	 * 
	 * 请求参数转换成方法参数
	 * 
	 * @author: 肖超
	 * @date: 2018年12月13日
	 * @param requestArgMap
	 * @param methodArgArr
	 * @return
	 */
	private Object[] getMethodArg(Map<String, String[]> requestArgMap, Parameter[] methodArgArr) {
		Object[] methodArg = new Object[methodArgArr.length];
		// 方法上有参数
		for (int i = 0; i < methodArgArr.length; i++) {
			// 参数名
			String MethodArgName = methodArgArr[i].getName();
			// 参数类型
			Class<?> MethodArgType = methodArgArr[i].getType();
			// 判断请求参数是否在方法参数中
			if (requestArgMap.containsKey(MethodArgName)) {

				String[] requestValue = requestArgMap.get(MethodArgName);
				String arg = requestValue[0];
				if (MethodArgType == String[].class) {
					methodArg[i] = requestValue;
				} else if (MethodArgType == String.class) {
					methodArg[i] = arg;
				} else if (MethodArgType == Integer.TYPE || MethodArgType == Integer.class) {
					int parseInt = Integer.parseInt(arg);

					methodArg[i] = parseInt;
				} else if (MethodArgType == Byte.TYPE || MethodArgType == Byte.class) {
					byte parseInt = Byte.parseByte(arg);

					methodArg[i] = parseInt;
				} else if (MethodArgType == Long.TYPE || MethodArgType == Long.class) {
					long parseInt = Long.parseLong(arg);

					methodArg[i] = parseInt;
				} else if (MethodArgType == Double.TYPE || MethodArgType == Double.class) {
					double parseInt = Double.parseDouble(arg);

					methodArg[i] = parseInt;
				} else if (MethodArgType == Float.TYPE || MethodArgType == Float.class) {
					float parseInt = Float.parseFloat(arg);

					methodArg[i] = parseInt;
				} else if (MethodArgType == Character.TYPE || MethodArgType == Character.class) {

					methodArg[i] = arg.charAt(0);
				} else if (MethodArgType == Short.TYPE || MethodArgType == Short.class) {
					short parseInt = Short.parseShort(arg);

					methodArg[i] = parseInt;
				} else if (MethodArgType == Boolean.TYPE || MethodArgType == Boolean.class) {
					boolean parseInt = Boolean.parseBoolean(arg);

					methodArg[i] = parseInt;
				} else {
					methodArg[i] = null;
				}

			}
		}
		return methodArg;
	}

	/**
	 * 加载handelmapping的类和url的关系
	 * 
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月12日
	 */
	private void doHandel() throws Exception {
		// 获取ioc中的所有对象
		Collection<Object> values = aliseName.values();
		for (Object string : values) {
			Class<? extends Object> clazz = string.getClass();
			// class是否有XiaoController注解
			if (clazz.isAnnotationPresent(XiaoController.class)) {
				String baseUrl = "";
				// class是否有XiaoRequestMapping注解并获取基础url
				if (clazz.isAnnotationPresent(XiaoRequestMapping.class)) {
					XiaoRequestMapping annotation = clazz.getAnnotation(XiaoRequestMapping.class);
					// 获取基础的url
					baseUrl = annotation.value();
				}

				Method[] methods = clazz.getMethods();
				// 获取方法的url并加入map中
				for (Method method : methods) {
					if (method.isAnnotationPresent(XiaoRequestMapping.class)) {
						XiaoRequestMapping annotation = method.getAnnotation(XiaoRequestMapping.class);
						String methodUrl = annotation.value();
						// 获取最终的url
						String url = "/" + baseUrl + "/" + methodUrl;
						// 获取标准url
						String standardUrl = url.replaceAll("/+", "/");
						// 判断是否重复
						boolean containsKey = handleMapping.containsKey(standardUrl);
						if (containsKey) {
							throw new Exception("url重复----》" + url);
						}
						handleMapping.put(standardUrl, method);
					}
				}
			}

		}

	}

	/**
	 * 注入
	 *
	 * @author: 肖超
	 * @date: 2018年12月13日
	 * @throws Exception
	 */
	private void doAutowir() throws Exception {
		// 获取ioc中的所有对象
		Collection<Object> values = aliseName.values();
		// 给XiaoAutowired注解的字段注入对象
		for (Object obj : values) {
			// 获取class所有的字段
			Class<? extends Object> clazz = obj.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				// 判断字段是否有XiaoAutowired注解，并注入
				if (field.isAnnotationPresent(XiaoAutowired.class)) {
					XiaoAutowired xiaoAutowired = field.getAnnotation(XiaoAutowired.class);
					// 获取注解的value
					String value = xiaoAutowired.value();
					field.setAccessible(true);
					Object object;
					if (value.equals("")) {
						value = field.getType().getSimpleName();
					}
					object = aliseName.get(value);
					if (object == null) {
						throw new Exception("注入时找不到class--》" + value);
					}
					field.set(obj, object);
				}
			}

		}

	}

	/**
	 * 加载XiaoServer和XiaoController注解的类
	 * 
	 * @author: 肖超
	 * @date: 2018年12月12日
	 */
	private void doinstent() throws Exception {
		if (className.isEmpty()) {
			System.out.println("扫描路径下没有.class文件");
			return;
		}

		for (String string : className) {
			Class<?> clazz = Class.forName(string);
			// 判断class是有XiaoServer和XiaoController注解
			String value;
			if (clazz.isAnnotationPresent(XiaoServer.class)) {
				XiaoServer annotation = clazz.getAnnotation(XiaoServer.class);
				value = annotation.value();

			} else if (clazz.isAnnotationPresent(XiaoController.class)) {
				XiaoController controller = clazz.getAnnotation(XiaoController.class);
				value = controller.value();
			} else {
				continue;
			}
			if (value.equals("")) {
				value = clazz.getSimpleName();
			}
			if (aliseName.containsKey(value)) {
				throw new Exception("类加载重复--》" + value);
			}
			// 加载
			aliseName.put(value, clazz.newInstance());
		}

	}

	/**
	 * 递归加载扫描包下的所有类的类名
	 * 
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月12日
	 */
	private void doScanner(String scannerPageake) throws Exception {
		// 获取扫描包路径
		URL resource = getClass().getClassLoader().getResource(scannerPageake.replaceAll("\\.", "/"));
		Assert.notNull(resource, "扫描包路径不存在");

		File f = new File(resource.toURI());
		// 获取子包
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			if (file.isDirectory()) {
				doScanner(scannerPageake + "/" + file.getName());

			} else {
				if (file.getName().endsWith(".class")) {
					className.add(
							scannerPageake.replaceAll("/", ".") + "." + file.getName().replaceAll(".class", "").trim());
				}
			}
		}

	}

	/**
	 * 获取web.xml的配置文件位置,并加载配置文件
	 * 
	 * @author: 肖超
	 * @date: 2018年12月12日
	 * @param config
	 * @throws Exception
	 */
	private void getConfig(String initParameter) throws Exception {
		Assert.notBlank(initParameter, "获取web.xml的配置文件位置异常");

		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(initParameter);
		Assert.notNull(resourceAsStream, "获取配置文件位置异常");
		configurationFiles.load(resourceAsStream);

	}

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		Class<TestAction> class1 = TestAction.class;
		Annotation[] annotations = class1.getAnnotations();
		Annotation[] declaredAnnotations = class1.getDeclaredAnnotations();
		System.out.println(declaredAnnotations.length);
	}
}
