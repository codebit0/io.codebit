package io.codebit.support.cdi;


import io.codebit.support.cdi.annotation.Spring;
import org.springframework.context.ApplicationContext;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SpringExtention implements Extension {

    private ApplicationContext context;

    public static Spring spring = new Spring(){
        @Override
        public Class<? extends Annotation> annotationType() {
            return Spring.class;
        }
    };

    public SpringExtention(ApplicationContext context) {
        this.context = context;
    }

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        System.out.println("before discover "+ bbd);
        System.out.println("1 BeanManager : "+ bm);

        for(String beanName : context.getBeanDefinitionNames()) {
            Class<?> type = context.getType(beanName);
            if(type.getCanonicalName().startsWith("org.springframework")) {
                continue;
            }
            System.out.println("spring bena" +  type + " "+ beanName);
//            bbd.
        }
        System.out.println("Spring end");
    }

    public void processInjectionTarget (@Observes ProcessInjectionTarget<?> pit, BeanManager bm) {

        Set<InjectionPoint> injectionPoints = pit.getInjectionTarget().getInjectionPoints();
        System.out.println("inject Point : "+ injectionPoints);
        System.out.println("inject Taeget : "+ pit.getInjectionTarget());
        System.out.println("2 BeanManager : "+ bm);
        System.out.println("2 ProcessInjectionTarget : "+ pit.getAnnotatedType());
//        inject Point : []
//        inject Taeget : InjectionTarget for Managed Bean [class org.jboss.weld.environment.se.beans.ParametersFactory] with qualifiers [@Any @Default]

        for (InjectionPoint point: injectionPoints) {

            if (!(point.getType() instanceof Class<?>)) {
                continue;
            }

            Class<?> injectionType = (Class<?>) point.getType();
            System.out.println("## TYPE ## "+injectionType);
        }
        System.out.println("-- pit "+ pit);
        /*synchronized (springBeans) {
            for (InjectionPoint point: injectionPoints){

                if (!(point.getType() instanceof Class<?>)) {
                    continue;
                }

                Class<?> injectionType = (Class<?>) point.getType();
                Spring spring = point.getAnnotated().getAnnotation(Spring.class);
                if (spring!=null) {
                    SpringBean springBean = new SpringBean(pit.getAnnotatedType(), spring, injectionType, bm);
                    springBeans.put(springBean.key(), springBean); //we can do some validation to make sure that this bean is compatible with the one we are replacing.
                } else {
                    SpringLookup springLookup = point.getAnnotated().getAnnotation(SpringLookup.class);
                    if (springLookup!=null) {
                        SpringBean springBean = new SpringBean(springLookup, injectionType, bm);
                        springBeans.put(springBean.key(), springBean);
                    }
                }
            }
        }*/
    }


    //https://docs.jboss.org/weld/reference/latest/en-US/html/extend.html
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        System.out.println("after " + abd);
        for(String beanName : context.getBeanDefinitionNames()) {
            Class<?> type = context.getType(beanName);

            if(type.getCanonicalName().startsWith("org.springframework")) {
                continue;
            }
//            AnnotatedType<SecurityManager> at = bm.createAnnotatedType(SecurityManager.class);
            System.out.println("spring bena" +  type + " "+ beanName);
            Annotation[] annotations = type.getAnnotations();
            abd.addBean(new SpringBean(beanName, type, bm));
        }
        /*synchronized (springBeans) {
            for (SpringBean bean : springBeans.values()) {
                abd.addBean(bean);
            }
        }*/
    }

    class SpringBean implements Bean <Object> {
        //InjectionTarget<Object> it;
        Class<?> injectionType;
        BeanManager bm;

        AnnotatedType<?> annotatedType;
        String name;

        SpringBean(String name, /*AnnotatedType<?> annotatedType, */  Class<?> injectionType, BeanManager bm){
            this.injectionType = injectionType;
            this.bm = bm;
//            this.annotatedType = annotatedType;
            this.name = name;
            //it = bm.createInjectionTarget(at);
        }

        /*public String key () {
            return "" + this.getName() + "::" + injectionType.toString();
        }*/

        @SuppressWarnings("all")
        class NamedLiteral extends AnnotationLiteral<Named> implements Named {

            @Override
            public String value() {
                return name;
            }

        }

        @Override
        public Class<?> getBeanClass() {
            return this.injectionType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.EMPTY_SET;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<Annotation>();
//            Proxy.newProxyInstance(Default.class.getClassLoader(), )
            Default aDefault = new Default(){
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Default.class;
                }
            };
//            Any any = new AnnotationInstanceProvider().get(Any.class, Collections.emptyMap());
            Any any = new Any(){
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Any.class;
                }
            };

            qualifiers.add(aDefault);
            qualifiers.add(any);
            qualifiers.add(new NamedLiteral()); //Added this because it causes OWB to fail if there is a Named
            qualifiers.add(spring);
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public Set<Type> getTypes() {
            Set<Type> types = new HashSet<Type>();
            types.add(this.injectionType);
            types.add(Object.class);
            return types;
        }

        //빈 결정 시점을 미룸
        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public boolean isNullable() {
            return true;
//            return spring != null ? !spring.required() : false;
        }
        @Override
        public Object create(CreationalContext<Object> ctx) {
            Object bean = context.getBean(this.injectionType);
            return bean;
            /*ApplicationContext applicationContext = ApplicationContextLocatorManager.getInstance().locateApplicationContext();
            if  (applicationContext==null) {
                if (spring !=null) {
                    System.err.printf("############## spring name=%s type=%s \n\n\n", spring.name(), spring.type());
                } else {
                    System.err.printf("############## lookup value=%s \n\n\n", lookup.value());
                }
                throw new IllegalStateException("applicationContext was null");
            }
            Object instance = null;
            if (spring!=null) {
                if (!spring.name().trim().equals("")) {
                    if(!spring.required()) {
                        if (applicationContext.containsBean(spring.name())) {
                            instance = applicationContext.getBean(spring.name(), spring.type());
                        }
                    } else {
                        instance = applicationContext.getBean(spring.name(), spring.type());
                    }
                } else {
                    instance = applicationContext.getBean(spring.type());
                }
            } else {
                instance = applicationContext.getBean(lookup.value());
            }*/
//            return instance;
        }

        @Override
        public void destroy(Object instance, CreationalContext<Object> ctx) {
            ctx.release();
        }

        public String toString() {
            return String.format("SpringBean(hc=%d, hc=%d, annotatedType=%s, qualifiers=%s)", this.hashCode(), SpringExtention.this.hashCode(), this.annotatedType, this.getQualifiers() );
        }

    }
}