package io.codebit.support.aspect.valid;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Path.ParameterNode;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 *Jsr303 308 valudator aop 
 * 
 */
@Aspect("issingleton()")
public class ValidAspect {

	private final static transient Logger log = Logger.getLogger(ValidAspect.class.getName());
	
	/**
	 * JSR-303 Validator.
	 */
	private final transient Validator validator = ValidAspect.build();

	/**
	 * 필드에 값을 읽을때 vaildation check
	 * 
	 * @param joinPoint 필드값을 쓰기 하는 joinPoint
	 * @return 필드값  
	 * @throws Throwable vaildation 실패 시 ConstraintViolationException 타입을 반환함 
	 */
	//@Around("get(@(javax.validation.* || javax.validation.constraints.*) * *.*)")
	@Around("get(@javax.validation.constraints.* * *.*)")
	public Object validationGetField(ProceedingJoinPoint joinPoint) throws Throwable {
		//TODO 매번 값을 읽을때마다 체크 하는건 비효율적인듯 
		FieldSignature signature = (FieldSignature)joinPoint.getSignature();
//		Class<?> type = signature.getFieldType();
		Field field = signature.getField();
		 
//		Type genericType = field.getGenericType();
		//타켓 Object 
		Object target = joinPoint.getTarget();
		//필드 명 
		String propertyName = field.getName();
		Set<ConstraintViolation<Object>> constraints = this.validator.validateProperty(target, propertyName);
		if (!constraints.isEmpty()) {
			throw new ConstraintViolationException(ValidAspect.pack(constraints), (Set<? extends ConstraintViolation<?>>) constraints);
		}
		return joinPoint.proceed();
	}

	/**
	 * 생성자 파라미터 Validate arguments of constructor.
	 *
	 * @param point
	 *            Join point
	 */
	@SuppressWarnings("unchecked")
	@Before("preinitialization(*.new(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
	public void beforeValidationConstructorArguments(final JoinPoint point) {
		
		log.finer(()->point.toString());
		//TODO 없으면 에러나게. 해야 할듯 
		if (this.validator != null) {
			ExecutableValidator execVaildator = this.validator.forExecutables();
			
			@SuppressWarnings("rawtypes")
			Constructor constructor = ConstructorSignature.class.cast(point.getSignature()).getConstructor();
			Set<ConstraintViolation<Object>> constraints = execVaildator.validateConstructorParameters(constructor, point.getArgs());
			if (!constraints.isEmpty()) {
				//파라미터 순서 대로 출력되도록 set ordered 를 지원하는 TreeSet으로 변경 
				Collection<ConstraintViolation<Object>> _constraints = sortParamaterIndex(constraints);
				throw new ConstraintViolationException(ValidAspect.pack(_constraints), (Set<? extends ConstraintViolation<?>>) _constraints);
			}
		}
	}

	/**
	 * Validate arguments of a method.
	 * 메소드 인자 validate 
	 *
	 * <p>
	 * 메소드 실행 이전 Arguments 유효성 검사 진행
	 * Try NOT to change the signature of this method, in order to keep it backward
	 * compatible.
	 *
	 * @param point Join point
	 * @throws ConstraintViolationException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before("execution(* *(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
	public void beforeValidationMethodArguments(final JoinPoint point) throws ConstraintViolationException {
		if (this.validator != null) {
			ExecutableValidator execVaildator = this.validator.forExecutables();
			MethodSignature signature = MethodSignature.class.cast(point.getSignature());
//			Parameter[] parameters = signature.getMethod().getParameters();
			Set<ConstraintViolation<Object>> constraints = execVaildator
					.validateParameters(point.getTarget(), signature.getMethod(), point.getArgs());

			log.finer(()-> "proxy:" + point.getThis() + " target:"+point.getTarget());

			if (!constraints.isEmpty()) {
				//파라미터 순서 대로 출력되도록 set ordered 를 지원하는 TreeSet으로 변경 
				Collection<ConstraintViolation<Object>> _constraints = sortParamaterIndex(constraints);
				throw new ConstraintViolationException(ValidAspect.pack((Collection) _constraints), (Set<? extends ConstraintViolation<?>>) _constraints);
			}
		}
	}

	/**
	 * 메소드 리턴값 Validate
	 *
	 * <p>
	 * @param point
	 *            Join point
	 * @param result
	 *            Result of the method
	 */
	@AfterReturning(pointcut = "execution(@(javax.validation.* || javax.validation.constraints.*) !void *(..))", returning = "result")
	public void aftrMethodReturning(final JoinPoint point, final Object result) {
		//메소드 Signature 확인 
		final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
		//return type 이 void인 경우 무시한다.
//		if(method.getReturnType().equals(Void.TYPE)) {
//			return;
//		}
		//return value 가 null 인 경우  @NotNull이 붙어 있지 않으면 모든 valid annotaion은 패스한다. 
		ExecutableValidator execVaildator = this.validator.forExecutables();
		Set<ConstraintViolation<Object>> violations  = execVaildator.validateReturnValue(point.getTarget(), method, result);
		
		//method 의 return 값을 Valid 하는 경우 
		if (method.isAnnotationPresent(Valid.class) && result != null) {
			//return 되는 타입을 재 체크한다.
			violations.addAll(this.validator.validate(result));
		}
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}

	/**
	 * Pack violations .
	 * 
	 * @param constraints
	 *            All violations
	 * @return violation message 를 줄바꿈으로 연결하여 반환  
	 */
	private static String pack(final Collection<ConstraintViolation<Object>> constraints) {
		StringBuilder error = null;
		for (final ConstraintViolation<Object> violation : constraints) {
			if(error == null) {
				error = new StringBuilder(violation.getMessage());
				continue;
			}
			error.append('\n');
			error.append(violation.getMessage());
		}
		return error.toString();
	}
	
	/**
	 * validator 생성한 후 반환.
	 * 
	 * @return 싱글톤 Validator
	 */
	private static Validator build() {
		Validator val = null;
		try {
			val = Validation.buildDefaultValidatorFactory().getValidator();
			log.info("val ---" + val);
		} catch (final ValidationException ex) {
			log.severe(()->{
				return "validator failed to initialize: "+ ex.getMessage();
			});
		} catch (final Throwable ex) {
			log.severe(()->{
				return "validator thrown during initialization: "+ ex.getStackTrace();
			});
		}
		return val;
	}
	
	/**
	 * Collection ConstraintViolation 이 Set타입으로 반환될때  파라미터 순서되로 정렬되지 않는 문제 해결 
	 * @param constraints validation 제약 오류 컬렉션 
	 * @return 파라미터 순서대로 정렬된 Collection 
	 */
	private static <T> Collection<ConstraintViolation<T>> sortParamaterIndex(Collection<ConstraintViolation<T>> constraints) {
		Collection<ConstraintViolation<T>> _constraints = new TreeSet<ConstraintViolation<T>>((o1, o2) -> {
			//파라미터 경로 객체 가져옴  
			Path path = o1.getPropertyPath();
			Path path2 = o2.getPropertyPath();
			//마지막 노드값의 위치 확인 
			int i = 0;
			int j = 0;
			for (final Node n : path )
		    {
				if(n.getKind().equals(ElementKind.PARAMETER)) {
					i = ((ParameterNode) n).getParameterIndex();
				}
		    }
			
			for (final Node n : path2 )
		    {
				if(n.getKind().equals(ElementKind.PARAMETER)) {
					j = ((ParameterNode) n).getParameterIndex();
				}
		    }
			return Integer.compare(i , j);
		});
		_constraints.addAll((Collection<? extends ConstraintViolation<T>>) constraints);
		return _constraints;
	}
}
