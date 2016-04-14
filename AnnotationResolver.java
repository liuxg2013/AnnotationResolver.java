package com.liuxg.logger.handler;

import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;


/**
 * 该类的作用可以把方法上的参数绑定到注解的变量中,注解的语法#{变量名}
 * 能解析类似#{task}或者#{task.taskName}或者{task.project.projectName}
 * @author liuxg
 * @date 2016年4月13日 下午8:42:34
 */
public class AnnotationResolver {

	private static AnnotationResolver resolver ;
	
	
	public static AnnotationResolver newInstance(){
		
		if (resolver == null) {
			return resolver = new AnnotationResolver();
		}else{
			return resolver;
		}
		
	}
	
	
	/**
	 * 解析注解上的值
	 * @param joinPoint
	 * @param str 需要解析的字符串
	 * @return
	 */
	public Object resolver(JoinPoint joinPoint, String str) {

		if (str == null) return null ;
		
		Object value = null;
		if (str.matches("#\\{\\D*\\}")) {// 如果name匹配上了#{},则把内容当作变量
			String newStr = str.replaceAll("#\\{", "").replaceAll("\\}", "");
			if (newStr.contains(".")) { // 复杂类型
				try {
					value = complexResolver(joinPoint, newStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				value = simpleResolver(joinPoint, newStr);
			}
		} else { //非变量
			value = str;
		}
		return value;
	}

	
	private Object complexResolver(JoinPoint joinPoint, String str) throws Exception {

		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

		String[] names = methodSignature.getParameterNames();
		Object[] args = joinPoint.getArgs();
		String[] strs = str.split("\\.");

		for (int i = 0; i < names.length; i++) {
			if (strs[0].equals(names[i])) {
				Object obj = args[i];
				Method dmethod = obj.getClass().getDeclaredMethod(getMethodName(strs[1]), null);
				Object value = dmethod.invoke(args[i]);
				return getValue(value, 1, strs);
			}
		}

		return null;

	}

	private Object getValue(Object obj, int index, String[] strs) {

		try {
			if (obj != null && index < strs.length - 1) {
				Method method = obj.getClass().getDeclaredMethod(getMethodName(strs[index + 1]), null);
				obj = method.invoke(obj);
				getValue(obj, index + 1, strs);
			}

			return obj;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getMethodName(String name) {
		return "get" + name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
	}

	
	private Object simpleResolver(JoinPoint joinPoint, String str) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		String[] names = methodSignature.getParameterNames();
		Object[] args = joinPoint.getArgs();

		for (int i = 0; i < names.length; i++) {
			if (str.equals(names[i])) {
				return args[i];
			}
		}
		return null;
	}

}
