package io.codebit.support.cdi;

import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.*;
//import javax.inject.Qualifier;

/**
 * Created by sunny on 2018-08-24.
 */
public class SpringCDIProvider implements CDIProvider {

    private final ApplicationContext context;

    public SpringCDIProvider(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public CDI<Object> getCDI() {
        CDI<Object> cdi = new SpringInstance<Object>();
        return cdi;
    }

    public class SpringInstance<T> extends CDI<T> {
        Class<T> type;

        Map<String, T> instances;

        SpringInstance() {
        }

        SpringInstance(Annotation... qualifiers) {
            beanByTypeAndAnnotation(qualifiers);
        }

        SpringInstance(Class<T> type, Annotation... qualifiers) {
            this.type = type;
            beanByType(type);
            beanByTypeAndAnnotation(qualifiers);
        }

        SpringInstance(TypeLiteral<T> subtype, Annotation... qualifiers) {
            String[] beanNamesForType = context.getBeanNamesForType(ResolvableType.forRawClass(subtype.getRawType()));
            Map<String, T> instances = new HashMap<>(beanNamesForType.length);
            for(String beanName : beanNamesForType) {
                instances.put(beanName, (T) context.getBean(beanName));
            }
            beanByTypeAndAnnotation(qualifiers);
        }

        @Override
        public T get() {
            if(type != null) {
                if(type.isArray()) {
                    return (T) this.instances.values().toArray();
                }else if(type.isAssignableFrom(Collections.class)) {
                    return (T) this.instances.values();
                }else {
                    return  this.instances.values().iterator().next();
                }
            }
            return (T) this.instances.values();
        }

        @Override
        public Instance<T> select(Annotation... qualifiers) {
            return new SpringInstance(qualifiers);
        }

        @Override
        public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
            return new SpringInstance(subtype, qualifiers);
        }

        @Override
        public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            return new SpringInstance(subtype, qualifiers);
        }

        @Override
        public boolean isUnsatisfied() {
            return this.instances == null;
        }

        @Override
        public boolean isAmbiguous() {
            return this.instances.size() > 0;
        }

        @Override
        public void destroy(T instance) {
            this.instances.values().remove(instance);
        }

        @Override
        public Iterator<T> iterator() {
            return (Iterator<T>) instances.values().iterator();
        }

        @Override
        public BeanManager getBeanManager() {
            throw new UnsupportedOperationException("SpringCDIProvider BeanManager not support");
        }

        private  void beanByType(Class<T> type){
            if(this.instances == null) {
                this.instances = context.getBeansOfType(type);
            }else {
                this.instances.keySet().retainAll(context.getBeansOfType(type).keySet());
            }
        }

        private void beanByTypeAndAnnotation(Annotation[] qualifiers) {
            if (this.instances != null && this.instances.size() <= 0) {
                return;
            }
            Map<String, T> retainAnnotatedBeans = null;
            for (Annotation qualifier : qualifiers) {
                if (qualifier.getClass().equals(Qualifier.class)) {
                    //spring Qualifier class
                    Qualifier q = (Qualifier) qualifier;
                    String name = q.value();
                    retainAnnotatedBeans = (Map<String, T>) context.getBeansWithAnnotation(qualifier.getClass());
                    //같은 이름이 아니면 제거
                    retainAnnotatedBeans.entrySet().removeIf(entry -> !entry.getKey().equals(name));
                } else if (qualifier.getClass().isAnnotationPresent(Qualifier.class)) {
                    //Qualifier annotaion 을 할당받은 spring class
                    retainAnnotatedBeans = (Map<String, T>) context.getBeansWithAnnotation(qualifier.getClass());
                } else if (qualifier.getClass().isAnnotationPresent(javax.inject.Qualifier.class)) {
                    retainAnnotatedBeans = (Map<String, T>) context.getBeansWithAnnotation(qualifier.getClass());
                }
                if (this.instances == null) {
                    this.instances = retainAnnotatedBeans;
                } else {
                    this.instances.keySet().retainAll(retainAnnotatedBeans.keySet());
                }
            }
            if (this.instances == null)
                this.instances = Collections.emptyMap();
        }
    }
}
