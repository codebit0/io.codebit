package io.codebit.support.util.ResourceBundle.Control;

import io.codebit.support.io.ReaderInputStream;
import sun.util.ResourceBundleEnumeration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bootcode on 2018-07-11.
 */
public class XMLResourceBundle extends ResourceBundle {
    private Map<String, Object> lookup;

    @SuppressWarnings(
			{"rawtypes", "unchecked"})
    public XMLResourceBundle(InputStream stream) throws IOException {
        // props = new Properties();
        Properties properties = new Properties();
        properties.loadFromXML(stream);
        lookup = new ConcurrentHashMap(properties);
    }

    @SuppressWarnings(
			{"unchecked", "rawtypes", "unused"})
    public XMLResourceBundle(Reader reader) throws IOException {
        Properties properties = new Properties();

        properties.loadFromXML(new ReaderInputStream(reader));
        lookup = new HashMap(properties);
    }

    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    @SuppressWarnings("restriction")
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(), (parent != null) ? parent.getKeys() : null);
    }
}
