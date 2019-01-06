package io.codebit.support.bci;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.List;

/**
 * Class Load Time 에 클래스 변형기를 적용하기 위한 클래스
 */
public class LoadtimeInstrument {

    private static Logger log = LoggerFactory.getLogger(LoadtimeInstrument.class);

    public static void transform(List<ClassFileTransformer> classFileTransformers) throws UnmodifiableClassException {
        final Instrumentation instrumentation = ByteBuddyAgent.install();
        for (ClassFileTransformer transformer : classFileTransformers) {
            instrumentation.addTransformer(transformer, true);
        }
    }

    /*public static void transform(List<ClassFileTransformer> classFileTransformers, List<String> includes) throws UnmodifiableClassException {
        transform(classFileTransformers, includes, Collections.emptyList());
    }*/

    /*public static void transform(List<ClassFileTransformer> classFileTransformers, List<String> includes, List<String> excludes) throws UnmodifiableClassException {
        final Instrumentation instrumentation = ByteBuddyAgent.install();
        for (ClassFileTransformer transformer : classFileTransformers) {
            instrumentation.addTransformer(transformer, true);
        }


        List<Pattern> excludePatterns = excludes.stream().map(exclude -> {
            // ..*   =  (?:\w+\.)*
            String packageName = exclude.replace("..*", "(?:.\\w+)*")
                    .replace(".*", ".\\w+")
                    .replace(".", "\\.");
            return Pattern.compile("^" + packageName+"$");
        }).collect(Collectors.toList());

        List<Class<?>> classes = new ArrayList<>();

        final ClassLoader cld = Thread.currentThread().getContextClassLoader();

        for (String include : includes) {
            String packageName;
            String _includePattern = "";

            //.* ..* 구분
            if (include.endsWith("..*")) {
                packageName = include.substring(0, include.lastIndexOf("..*"));
                _includePattern = packageName;
            } else if (include.endsWith(".*")) {
                packageName = include.substring(0, include.lastIndexOf(".*"));
                _includePattern = packageName + "$";
            } else {
                packageName = include;
            }
//            packageName = include.replace("..*", "(?:.\\w+)*")
//                    .replace(".*", ".\\w+")
//                    .replace(".", "\\.");

            Pattern includePattern = Pattern.compile(_includePattern.replace(".", "\\.").replace("*", ".*"));

            Enumeration<URL> cldResources = null;
            try {
                cldResources = cld.getResources(packageName.replace('.', '/'));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            while (cldResources.hasMoreElements()) {
                try {
                    URL url = cldResources.nextElement();
                    URLConnection connection = url.openConnection();
                    if (connection instanceof JarURLConnection) {
                        final JarFile jarFile = ((JarURLConnection) connection).getJarFile();
                        final Enumeration<JarEntry> entries = jarFile.entries();

                        while (entries.hasMoreElements()) {
                            JarEntry jarEntry = entries.nextElement();
                            String name = jarEntry.getName();

                            if (name.endsWith(".class")) {
                                name = name.substring(0, name.length() - 6).replace('/', '.');
                                if(name.endsWith("package-info") || name.endsWith("module-info")) {
                                    continue;
                                }
                                if (includePattern.matcher(name).find() && !isExclude(name, excludePatterns)) {
                                    try {
                                        classes.add(Class.forName(name));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else if (connection instanceof FileURLConnection) {
                        Path start = new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name())).toPath();
                        Files.walk(start).filter(path ->
                                Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class")
                        ).forEach(path -> {
                            Path relative = start.relativize(path);
                            String file = relative.toString().replace("\\", ".");
                            try {
                                String className = packageName + '.' + file.substring(0, file.length() - 6);
                                if(!className.endsWith("package-info") && className.endsWith("module-info")) {
                                    if (includePattern.matcher(className).find() && !isExclude(className, excludePatterns)) {
                                        classes.add(Class.forName(className));
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        List<Class<?>> list = classes.stream().distinct().collect(Collectors.toList());
        if (list.size() > 0) {
            System.out.println("instrument class: "+ list);
            try {
                instrumentation.retransformClasses(list.toArray(new Class[list.size()]));
            }catch (Throwable t) {
                log.error("retransformClasses error" , t);
            }
        }
        for (ClassFileTransformer transformer : classFileTransformers) {
            instrumentation.removeTransformer(transformer);
        }
    }*/

    /*private static boolean isExclude(String className, List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(className).find()) {
                return true;
            }
        }
        return false;
    }*/
}
