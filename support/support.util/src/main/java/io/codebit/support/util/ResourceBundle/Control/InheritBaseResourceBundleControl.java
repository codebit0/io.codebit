package io.codebit.support.util.ResourceBundle.Control;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * {link http://javalove.egloos.com/v/97918}
 * {link http://stackoverflow.com/questions/4614465/is-it-possible-to-include-resource-bundle-files-within-a-resource-bundle}
 * {link http://docs.oracle.com/javase/tutorial/i18n/serviceproviders/resourcebundlecontrolprovider.html}
 * The Class BaseResourceBundleControl.
 */
public class InheritBaseResourceBundleControl extends Control
{
	private String mode = null; 
	
	@Override
	public List<Locale> getCandidateLocales(String baseName, Locale locale)
	{
		if (baseName == null)
			throw new NullPointerException();
		List<Locale> candidateLocales = super.getCandidateLocales(baseName, locale);
		
		mode = System.getProperty("application.mode");
		if(mode == null)
		{
			mode = System.getenv("application_mode");
		}
		if(mode == null)
		{
			mode = System.getenv("APPLICATION_MODE");
		}
		
		if(mode != null)
		{
//			log.debug("InheritBaseResourceBundle application mode: {}", mode);
			candidateLocales.add(0,new Locale(locale.getLanguage(), locale.getCountry(), mode));
			candidateLocales.add(1,new Locale(locale.getLanguage(), "", mode));
			candidateLocales.add(2,new Locale("", "", mode));
		}
		// public Locale(String language, String country, String variant)
		candidateLocales.add(new Locale(locale.getLanguage(), locale.getCountry(), "base"));
		candidateLocales.add(new Locale(locale.getLanguage(), "", "base"));
		candidateLocales.add(new Locale("", "", "base"));

		return candidateLocales;
		// Builder localeBuilder = new Locale.Builder();
		// localeBuilder.setLanguage("ko").setExtension('a', "abc").build();
		// return Arrays.asList(
		// locale,
		// Locale.ROOT,
		// new Locale("default")
		// //,new Locale("default",locale.getCountry())
		// );
	}

	@Override
	public String toBundleName(String baseName, Locale locale)
	{
//		if (locale == Locale.ROOT) 
//		{
//            return baseName;
//        }
		String variant = locale.getVariant();
		if (variant.equals("base"))
		{
			return toBundleName(baseName, locale, variant);
		}else if(mode != null && variant.equals(mode))
		{
			return toBundleName(baseName, locale, mode);
		}
		
		return super.toBundleName(baseName, locale);
	}

	private String toBundleName(String baseName, Locale locale, String variant)
	{
		String language = locale.getLanguage();
		// String script = locale.getScript();
		String country = locale.getCountry();
		StringBuilder sb = new StringBuilder(baseName);
		if (!country.isEmpty())
		{
			sb.append('_').append(language).append('_').append(country).append('_')
					.append(variant);
		} else if (!language.isEmpty())
		{
			sb.append('_').append(language).append('_').append(variant);
		} else
		{
			sb.append('_').append(variant);
		}
		return sb.toString();
	}
	
	@Override
	public ResourceBundle newBundle(
			String baseName, Locale locale, String format, ClassLoader loader,
			boolean reload)
			throws IllegalAccessException, InstantiationException, IOException
	{
		ResourceBundle newBundle = super.newBundle(baseName, locale, format, loader, reload);
		//FIXME LOG
//		log.debug("format {} : bundleName {} : bundle {}", format , toBundleName(baseName, locale), newBundle);
		return newBundle;
	}
}
