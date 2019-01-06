package io.codebit.support.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.codebit.support.net.ClassPathURLStreamHandler;
import io.codebit.support.util.ResourceBundle.Control.ConfigResourceBundleControl;

import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Config{

	private static final Logger logger = Logger.getLogger(Config.class.getName());

	public static final String[] DEFAULT_FILES = {
			"classpath://application.yaml"
			, "classpath://application.properties"
			, "classpath://application.xml"
	};

	private static LoadingCache<String, Optional<ResourceBundle>> BUNDLES_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.softValues()
//			.removalListener(MY_LISTENER)
			.build(
				new CacheLoader<String, Optional<ResourceBundle>>() {
					public Optional<ResourceBundle> load(String key) {
						return Optional.ofNullable(findBundle(key));
					}
				});

	private List<String> paths = new ArrayList<>();

	private String prefix;

	public static Config create(String path, String ... otherPath) {
		return new Config(path, otherPath);
	}

	@Deprecated
	public static Config of(String ... paths) {
		return new Config(DEFAULT_FILES, paths);
	}

	private Config(String[] defaultFile, String ... otherPath)
	{
		for(String p : defaultFile){
			this.paths.add(p);
		}
		for(String p : otherPath){
			this.paths.add(p);
		}
	}

	private Config(String path, String ... otherPath)
	{
		this.paths.add(path);
		for(String p : otherPath){
			this.paths.add(p);
		}
	}

	public Config prefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public Config profiles(String ... profiles) {
//		this.profiles = profiles;
		return this;
	}

	public String getString(String key)  {
		if(this.prefix != null) {
			key = this.prefix+"."+key;
		}

		RuntimeException exception = null;
		for(String path: paths) {
			try {
				Optional<ResourceBundle> bundle = BUNDLES_CACHE.get(path);
				if(bundle.isPresent()){
					ResourceBundle resource = bundle.get();
					if(resource != null && resource.containsKey(key)) {
						String value = resource.getString(key);
						return value;
					}
				}
			} catch (Exception e) {
				if(exception == null)
					exception = new RuntimeException(e);
				else
					exception.initCause(e);
			}
		}
		throw exception;
	}

	public String getString(String key, String defaultValue) {
		try	{
			return  getString(key);
		}catch(Exception e) 	{
			return defaultValue;
		}
	}

	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}

	public int getInt(String key, int defaultValue) {
		try	{
			return getInt(key);
		}catch(Exception e) 	{
			return defaultValue;
		}
	}

	public long getLong(String key) {
		return Long.parseLong(getString(key));
	}

	public long getLong(String key, long defaultValue) {
		try	{
			return getLong(key);
		}catch(Exception e) 	{
			return defaultValue;
		}
	}

	private static ResourceBundle findBundle(String p) {
		try{
			URL url = new URL(null, p , new ClassPathURLStreamHandler());
			URLClassLoader urlLoader = new URLClassLoader(new URL[]{url});

			String bundleName = url.getFile();
			if(bundleName == null || bundleName.isEmpty()) {
				bundleName = url.getAuthority();
			}
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.ROOT, urlLoader, new ConfigResourceBundleControl());
			return bundle;
		} catch (MissingResourceException e) {
			//skip
			logger.info(e.getMessage());
		}catch (Exception e){
			logger.severe("throw : "+e.getMessage());
		}
		return null;
	}
}