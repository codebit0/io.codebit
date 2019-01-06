package in.java.support.spring.mvc;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

/**
 * {link http://springsource.tistory.com/16}
 * The Class JDK1.8 Optional ArgumentConverter.
 * 제네릭 타입을 구분하지 못해 형변환할 수 없음
 */
//@Component
public class OptionalArgumentConverter implements Converter<String, Optional<?>> 
{
	//@Inject 
	private GenericConversionService gcs;
	
	@Override
    public Optional<?> convert(String source) 
	{
        return Optional.of(source);
    }
	
	//@PostConstruct
	public void init()
	{
		gcs.addConverter(this);
	}
}
