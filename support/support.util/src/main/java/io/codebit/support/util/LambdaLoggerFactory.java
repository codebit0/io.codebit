package io.codebit.support.util;

import org.slf4j.LoggerFactory;

public class LambdaLoggerFactory {

    public static LambdaLogger getLogger(String name) {
        return new LambdaLogger(LoggerFactory.getLogger(name));
    }

    public static LambdaLogger getLogger(Class<?> klass) {
        return new LambdaLogger(LoggerFactory.getLogger(klass));
    }
}
