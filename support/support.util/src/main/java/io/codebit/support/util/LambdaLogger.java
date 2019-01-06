package io.codebit.support.util;

import org.slf4j.Logger;

/**
 * Created by bootcode on 2018-07-16.
 */
public class LambdaLogger implements ILogger {

    private final Logger logger;

    public LambdaLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Logger origin() {
        return this.logger;
    }
}
