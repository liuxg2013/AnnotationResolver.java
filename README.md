# AnnotationResolver
一个解析注解的工具类，能够把注解方法上的参数直接绑定到注解类中

具体用法举个demo

我们有这样子的需求，需要记录用户操作某个方法的信息并记录到日志里面，例如，用户在保存和更新任务的时候，我们需要记录下用户的ip，具体是保存还是更新，调用的是哪个方法，保存和更新的任务名称以及操作是否成功。

这里最好的技术就是spring aop + annotation，首先我来定义个注解类

```
/**
 * 参数命名好麻烦，我就随便了，只是演示下用法
 * @author liuxg
 * @date 2016年4月13日 上午7:53:52
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Logger {
	String param1() default "";
	String param2() default "" ;
	String param3() default "" ;
	String param4() default "" ;	
}

``


```
然后我们在controller中定义一个方法，即用户具体调用的保存或者更新的方法
```
@RequestMapping("/mvc24")
@Logger(param1 = "#{task.project.projectName}",param2 = "#{task.taskName}",param3 = "#{name}",param4 = "常量")
public void mvc24(Task task ,String name){
	
	//...
}
```

在这里我们就可以把参数中的task或者name的相关信息绑定到注解类中
然后我们再定义一个切面，我们就可以动态的获取和处理注解类的一些信息了

```
/**
 * 日志切面
 * @author liuxg
 * @date 2015年10月13日 下午5:55:44
 */
@Component
@Aspect
public class LoggerAspect {

	
	@Around("@annotation(com.liuxg.logger.annotation.Logger)")
	public Object around(JoinPoint joinPoint)  {

		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		Logger logger =  (Logger) method.getAnnotation(Logger.class);
		
		Object value1 = AnnotationResolver.newInstance().resolver(joinPoint, logger.param1()); //利用AnnotationResolver进行解析
		Object value2 = AnnotationResolver.newInstance().resolver(joinPoint, logger.param1());
		Object value3 = AnnotationResolver.newInstance().resolver(joinPoint, logger.param1());
		Object value4 = AnnotationResolver.newInstance().resolver(joinPoint, logger.param1());
		
		return null ;

	}
	
}
```

AnnotationResolver的具体用法如上，利用该解析器，可以把注解类中这样子的语法直接解析#{方法变量名}
该解析器只有唯一的一个方法

```
/**
 * 解析注解上的值
 * @param joinPoint 切面类，直接在aop里面获取，参考上面的例子
 * @param str 需要解析的字符串
 * @return
 */
public Object resolver(JoinPoint joinPoint, String str) 
```
