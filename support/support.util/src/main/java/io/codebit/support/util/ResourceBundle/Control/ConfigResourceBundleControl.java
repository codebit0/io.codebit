package io.codebit.support.util.ResourceBundle.Control;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.ResourceBundle.Control;

/**
 * {link http://javalove.egloos.com/v/97918}
 * {link http://stackoverflow.com/questions/4614465/is-it-possible-to-include-resource-bundle-files-within-a-resource-bundle}
 * {link http://docs.oracle.com/javase/tutorial/i18n/serviceproviders/resourcebundlecontrolprovider.html}
 * The Class BaseResourceBundleControl.
 */
public class ConfigResourceBundleControl extends Control {

    private static final List<String> FORMAT_LIST = Arrays.asList( "yaml", "properties", "xml");

    private String mode = null;

    @Override
    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
        List<Locale> candidateLocales = super.getCandidateLocales(baseName, Locale.ROOT);
        return candidateLocales;
    }

    @Override
    public String toBundleName(String baseName, Locale locale) {
        return baseName;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        if (baseName == null || locale == null || format == null || loader == null) {
            throw new NullPointerException();
        }
        if(!baseName.endsWith("."+format))
            return null;

        ResourceBundle bundle = null;
        URL url = loader.getResource(baseName);
        if (url != null)
        {
            URLConnection connection = url.openConnection();
            if (connection != null)
            {
                if (reload)
                {
                    // disable caches if reloading
                    connection.setUseCaches(false);
                }
                try (InputStream stream = connection.getInputStream())
                {
                    if (stream != null)
                    {
                        BufferedInputStream bis = new BufferedInputStream(stream);
                        if(baseName.endsWith(".yaml")) {
                            bundle = new YamlResourceBundle(bis);
                        }else if(baseName.endsWith("xml")) {
                            bundle = new XMLResourceBundle(bis);
                        }else if(baseName.endsWith(".properties")) {
                            baseName = replaceLast(baseName, ".properties");
                            bundle = super.newBundle(baseName, Locale.ROOT, format, loader, reload);
                        }
                    }
                }
            }
        }
//        ResourceBundle newBundle = super.newBundle(baseName, Locale.ROOT, format, loader, reload);
        //FIXME LOG
//        log.debug("format {} : bundleName {} : bundle {}", format, toBundleName(baseName, locale), bundle);
        return bundle;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return FORMAT_LIST;
    }

    private String replaceLast(String baseName, String extension) {
        int lastIndex = baseName.lastIndexOf(extension);
        if (lastIndex < 0) return baseName;
          return baseName.substring(0, lastIndex);
    }
}
