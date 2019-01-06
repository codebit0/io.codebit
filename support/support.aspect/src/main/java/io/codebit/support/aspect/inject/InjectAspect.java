package io.codebit.support.aspect.inject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Provider;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.FieldSignature;

/**
 * Jsr330
 * 현재는 필드 인젝션만 지원
 * @author bootcode
 *
 * {@link https://docs.oracle.com/javaee/6/api/javax/inject/Inject.html} 
 * https://dzone.com/articles/injecting-string-resource
 * https://antoniogoncalves.org/2011/09/25/injection-with-cdi-part-iii/
 * 
 * https://www.petrikainulainen.net/programming/spring-framework/spring-from-the-trenches-injecting-property-values-into-configuration-beans/
 * @Produces : https://docs.oracle.com/javaee/6/api/javax/enterprise/inject/Produces.html 
 * http://buraktas.com/cdi-dependency-injection-producer-method-example/
 */
@Aspect("issingleton()")
public class InjectAspect {

	final Logger log = Logger.getLogger(InjectAspect.class.getName());

	/*
	 * golbal pointcut
	 */
//    @Pointcut("execution(@javax.inject.Inject * *(..))")
//    public void injectMethodArgs() {
//    }
//
//	/**
//	 *  @Inject method에 객체 주입
//	 */
//	@Before("injectMethodArgs()")
//	public void paramInject(JoinPoint jp) throws Throwable {
//		Object[] args = jp.getArgs();
//		for (Object signatureArg: args) {
//			System.out.println("Arg: " + signatureArg);
//		}
//		System.out.println(jp);
//	}
	@Pointcut("get(@javax.inject.Inject * *.*)")
	public void fieldInjectPoint(){
	}

	/**
	 * @Inject 필드에 객체 주입
	 * @param joinPoint 조인 포인트
	 * @return 반환값
	 * @throws Throwable 주입 시점 예외 처리
     */
//	@Around("get(@javax.inject.Inject * *.*)")
	@Around("fieldInjectPoint()")
    public Object injectField(ProceedingJoinPoint joinPoint) throws Throwable {
		FieldSignature signature = (FieldSignature)joinPoint.getSignature();
		Class<?> type = signature.getFieldType();
		Field field = signature.getField();
		Type genericType = field.getGenericType();
//		Annotation[] annotations = field.getAnnotations();
		//Type rawType = type;
		Type actual = genericType;
		if(!type.equals(genericType) && (genericType instanceof ParameterizedType)){
			ParameterizedType pType = ((ParameterizedType)genericType);
			//rawType = pType.getRawType();         
	        actual = pType.getActualTypeArguments()[0];   
		}
        CDI<Object> current = CDI.current();

        if(type.isAssignableFrom(Provider.class)) {
			Provider<?> provider = current.select((Class<?>)actual);
			return provider;
		}else {
			Object bean = current.select((Class<?>)type).get();
			return bean;
		}
    }
}
