package io.codebit.support.bci;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.loadtime.Aj;
import org.aspectj.weaver.loadtime.ClassPreProcessor;
import org.aspectj.weaver.loadtime.DefaultMessageHandler;
import org.aspectj.weaver.loadtime.DefaultWeavingContext;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.tools.WeavingAdaptor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public  class AspectClassFileTransformer implements ClassFileTransformer {

    private ClassPreProcessor classPreProcessor;
    WeavingContext context;

    public AspectClassFileTransformer(Class<?> ... classes) {
        this(new WeavingContext(classes));
//        org.aspectj.bridge.IMessageHandler messageHandler;
//        org.aspectj.weaver.loadtime.DefaultMessageHandler d;
//        org.aspectj.weaver.tools.WeavingAdaptor.WeavingAdaptorMessageHolder adaptorMessageHolder;
    }

    public AspectClassFileTransformer(WeavingContext context) {
        this.context = context;
        classPreProcessor =  new Aj(context);
        classPreProcessor.initialize();
    }

    public WeavingContext context() {
        return this.context;
    }

    /**
     * Invokes the weaver to modify some set of input bytes.
     *
     * @param loader the defining class loader
     * @param className the name of class being loaded
     * @param classBeingRedefined is set when hotswap is being attempted
     * @param protectionDomain the protection domain for the class being loaded
     * @param bytes the incoming bytes (before weaving)
     * @return the woven bytes
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] bytes) throws IllegalClassFormatException {
        try{
            if (classBeingRedefined != null) {
//                System.out.println("classBeingRedefined >####################################"+className);
                classPreProcessor.prepareForRedefinition(loader, className);
//                System.out.println("classBeingRedefined <<<<<<<  ####################################"+className);
            }
//            System.out.println(">####################################"+className);
            byte[] process = classPreProcessor.preProcess(className, bytes, loader, protectionDomain);
//            if(bytes.length != process.length)
//                System.out.println("<<<<<<<  ####################################"+className);
            return process;
        }catch (Throwable t){
            System.out.println("Error " + t);
            throw t;
        }
    }

    public static class WeavingContext extends DefaultWeavingContext {

        private Class<?>[] aspectClasses;
        private String options = "";
        private List<String> excludes = new ArrayList<>();
        private List<String> includes = new ArrayList<>();
        private List<String> dumps = new ArrayList<>();

        public WeavingContext(Class<?> ... aspectClasses) {
            super(Thread.currentThread().getContextClassLoader());
            this.aspectClasses = aspectClasses;
        }

        public void includes(List<String> includes) {
            this.includes = includes;
        }

        public void excludes(List<String> excludes) {
            this.excludes = excludes;
        }

        public void dumpPatterns(List<String> dumps) {
            this.dumps = dumps;
        }

        public List<String> includes() {
            return this.includes;
        }

        public List<String> excludes() {
            return this.excludes;
        }

        public List<String> dumpPatterns() {
            return this.dumps;
        }

        public void options(String options) {
            this.options = options;
        }

        public List<Definition> getDefinitions(final ClassLoader loader, final WeavingAdaptor adaptor) {

//        List<Definition> definitionsv = super.getDefinitions(loader, adaptor);
            ArrayList<Definition> definitions = new ArrayList<>();
            for (Class<?> klass : aspectClasses) {
                WeavingContext.WDefinition definition = new WeavingContext.WDefinition();
                definition.weaverOptions = this.options;
                definition.getAspectClassNames().add(klass.getName());
                if (this.includes != null && this.includes.size() > 0)
                    definition.getIncludePatterns().addAll(this.includes);
                if (this.excludes != null && this.excludes.size() > 0)
                    definition.getExcludePatterns().addAll(this.excludes);
                if (this.dumps != null && this.dumps.size() > 0) {
                    definition.getDumpPatterns().addAll(this.dumps);
//                    definition.setCreateDumpDirPerClassloader(true);
//                    definition.setDumpBefore(true);
                }
                definitions.add(definition);
            }
            return definitions;
        }

        static class WDefinition extends Definition {
            String weaverOptions;

            @Override
            public String getWeaverOptions() {
//                return weaverOptions;
                return weaverOptions+ " -XmessageHandlerClass:AspectMessagesHandler";
            }
        }
    }
}