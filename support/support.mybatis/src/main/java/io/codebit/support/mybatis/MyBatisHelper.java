package io.codebit.support.mybatis;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class MyBatisHelper {
    static Logger log = LoggerFactory.getLogger(MyBatisHelper.class);

    private static String DEFAULT_CONFIG = Config.getString("MyBatisHelper.default.config.file"); //$NON-NLS-1$
    private static Marker MARKER = MarkerFactory.getMarker("MYBATIS");

    // private static Object _lock = new Object();
    private static Map<String, SqlSessionFactory> _cache = new HashMap<String, SqlSessionFactory>();

    public static SqlSessionFactory getSqlSessionFactory(String config, Properties properties, String env) {
        if (env == null) {
            env = System.getProperty("application.mode");
            if (env == null) {
                env = System.getenv("application_mode");
            }
            if (env == null) {
                env = System.getenv("APPLICATION_MODE");
            }
        }
        try {
            File file = Resources.getResourceAsFile(config);
            //FIXME LOG
            log.debug(MARKER, "config file :{}", file);
            log.debug(MARKER, "SessionFactory mode :{}", env);
            log.debug(MARKER, "SessionFactory properties :{}", properties);

            // jar 파일안의 file.lastModified() 는 0이 나오나 파일이 풀리고 나서는 변경시간이
            // 정확히 나올것으로 예상
            // 이게 부정확 하게 동작할 경우 파일을 읽어 chceksum 하는 방법으로 해야할 듯
            //0^0^C:\SdkTools\eclipse-jee-luna-R-win32-x86_64\
            //eclipse\file:\C:\SdkTools\vfabric-tc-server-developer-2.9.5.SR1\board\wtpwebapps
            //\in.java.supermom.api\WEB-INF\lib\supermom.repo-1.0.0.jar!\mybatis.xml^dev^null
            String cacheKey = String
                    .format("%s^%s^%s^%s^%s", file.lastModified(), file.length(), file.getAbsolutePath(), env, properties);
//			log.debug(MARKER, "SessionFactory cacheKey :{}", cacheKey);
            SqlSessionFactory sessionFactory = _cache.get(cacheKey);
            if (sessionFactory == null) {
                synchronized (_cache) {
                    // 1/100 확률로 전체 cache를 삭제
                    int r = (int) (Math.random() * 100);
                    if (r == 1)
                        _cache.clear();

                    sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(config), env,
                            properties);
//					TypeHandlerRegistry registry = sessionFactory.getConfiguration().getTypeHandlerRegistry();
//					registry.register(LocalDate.class, LocalDateTypeHandler.class);
//					log.debug(MARKER, "TypeHandlerRegistry :{}", "LocalDateTypeHandler");
                    //registry.register(typeHandler);
                    _cache.put(cacheKey, sessionFactory);
                }
            }
//			else
//			{
//				SqlSessionFactory sessionFactory2 = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(config), env,
//					properties);
//				Configuration configuration = sessionFactory2.getConfiguration();
//				log.debug("Configuration equal check old :{}, new :{}, equals:{} ", 
//					sessionFactory.getConfiguration(), configuration, 
//					sessionFactory.getConfiguration().equals(configuration)  );
//			}
            return sessionFactory;

        } catch (IOException e) {
//			log.error(Config.getString("MyBatisHelper.getsqlsessionfactory.exception.message"), e); //$NON-NLS-1$
            throw new UncheckedIOException(e);
        }
    }

    public static SqlSessionFactory getSqlSessionFactory(String config, String env) {
        return getSqlSessionFactory(config, System.getProperties(), env);
    }

    public static SqlSessionFactory getSqlSessionFactory(Properties properties, String env) {
        return getSqlSessionFactory(DEFAULT_CONFIG, properties, env);
    }

    public static SqlSessionFactory getSqlSessionFactory(Properties properties) {
        return getSqlSessionFactory(DEFAULT_CONFIG, properties, null);
    }

    public static SqlSessionFactory getSqlSessionFactory(String config) {
        return getSqlSessionFactory(config, System.getProperties(), null);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return getSqlSessionFactory(DEFAULT_CONFIG, System.getProperties(), null);
    }
}