package io.codebit.support.net;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Created by bootcode on 2018-07-11.
 * @link {https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java, https://docstore.mik.ua/orelly/java/exp/ch09_06.htm}
 */
public class ClassPathURLStreamHandler extends URLStreamHandler {
    private final ClassLoader classLoader;

    public ClassPathURLStreamHandler() {
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    public ClassPathURLStreamHandler(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        final URL resourceUrl = classLoader.getResource(u.getPath());
        return resourceUrl.openConnection();
    }
}
