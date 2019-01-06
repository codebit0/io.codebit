package io.codebit.support.util.ResourceBundle.Control;

import io.codebit.support.io.ReaderInputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;
import sun.util.ResourceBundleEnumeration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by bootcode on 2018-07-10.
 */
public class YamlResourceBundle extends ResourceBundle {

    private static final Logger logger = Logger.getLogger(YamlResourceBundle.class.getName());

    private Map<String, Object> lookup;

    public YamlResourceBundle(InputStream stream)  throws IOException {
        Properties properties = new Properties();
        load(properties, stream);
        lookup = new ConcurrentHashMap(properties);
    }

    public YamlResourceBundle(Reader reader)  throws IOException  {
        Properties properties = new Properties();
        load(properties, new ReaderInputStream(reader));
        lookup = new ConcurrentHashMap(properties);
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(), (parent != null) ? parent.getKeys() : null);
    }

    void load(Properties result, InputStream input) throws IOException {
        Yaml yaml = new Yaml();
        try (Reader reader = new UnicodeReader(input)) {
            Iterable<Object> objects = yaml.loadAll(reader);
            for(Object object : objects) {
                if (object != null) {
                    Map<String, Object> yamlAsMap = convertToMap(object);
                    result.putAll(flatten(yamlAsMap));
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new IOException("Unable to load yaml from provided stream", e);
        }
//        catch (ScannerException e) {
//            throw new IOException("Unable to load yaml from provided stream", e);
//        } catch (Exception e) {
//            throw new IOException("Unable to load yaml from provided stream", e);
//        }
    }

    Map<String, Object> flatten(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : source.keySet()) {
            Object value = source.get(key);

            if (value instanceof Map) {
                Map<String, Object> subMap = flatten((Map<String, Object>) value);

                for (String subkey : subMap.keySet()) {
                    result.put(key + "." + subkey, subMap.get(subkey));
                }
            } else if (value instanceof Collection) {
                StringBuilder joiner = new StringBuilder();
                String separator = "";

                for (Object element : ((Collection) value)) {
                    Map<String, Object> subMap = flatten(Collections.singletonMap(key, element));
                    joiner
                            .append(separator)
                            .append(subMap.entrySet().iterator().next().getValue().toString());

                    separator = ",";
                }

                result.put(key, joiner.toString());
            } else {
                result.put(key, value);
            }
        }
        return result;
    }


    private Map<String, Object> convertToMap(Object yamlDocument) {

        Map<String, Object> yamlMap = new LinkedHashMap<>();
        // Document is a text block
        if (!(yamlDocument instanceof Map)) {
            yamlMap.put("content", yamlDocument);
            return yamlMap;
        }

        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) yamlDocument).entrySet()) {
            Object value = entry.getValue();

            if (value instanceof Map) {
                value = convertToMap(value);
            } else if (value instanceof Collection) {
                ArrayList<Map<String, Object>> collection = new ArrayList<>();

                for (Object element : ((Collection) value)) {
                    collection.add(convertToMap(element));
                }
                value = collection;
            }
            yamlMap.put(entry.getKey().toString(), value);
        }
        return yamlMap;
    }
}
