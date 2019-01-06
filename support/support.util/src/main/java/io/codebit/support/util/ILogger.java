package io.codebit.support.util;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface ILogger extends Logger {

    static final Function<Supplier[], Object[]> SUPPLIER_CONV  = new Function<Supplier[], Object[]>() {
        @Override
        public Object[] apply(Supplier[] arguments) {
            return Arrays.asList(arguments).stream().map(arg -> arg.get()).collect(Collectors.toList()).toArray();
        }
    };

    Logger origin();

    default String getName() {
        return this.origin().getName();
    }

    default boolean isTraceEnabled() {
        return this.origin().isTraceEnabled();
    }

    default void trace(String msg) {
        this.origin().trace(msg);
    }

    default void trace(String format, Object arg) {
        this.origin().trace(format, arg);
    }

    default void trace(String format, Object arg1, Object arg2) {
        this.origin().trace(format, arg1, arg2);
    }

    default void trace(String format, Object... arguments) {
        this.origin().trace(format, arguments);
    }

    default void trace(String msg, Throwable t) {
        this.origin().trace(msg, t);
    }

    default boolean isTraceEnabled(Marker marker) {
        return this.origin().isTraceEnabled(marker);
    }

    default void trace(Marker marker, String msg) {
        this.origin().trace(marker, msg);
    }

    default void trace(Marker marker, String format, Object arg) {
        this.origin().trace(marker, format, arg);
    }

    default void trace(Marker marker, String format, Object arg1, Object arg2) {
        this.origin().trace(marker, format, arg1, arg2);
    }

    default void trace(Marker marker, String format, Object... arguments) {
        this.origin().trace(marker, format, arguments);
    }

    default void trace(Marker marker, String msg, Throwable t) {
        this.origin().trace(marker, msg, t);
    }

    default boolean isDebugEnabled() {
        return this.origin().isDebugEnabled();
    }

    default void debug(String msg) {
        this.origin().debug(msg);
    }

    default void debug(String format, Object arg) {
        this.origin().debug(format, arg);
    }

    default void debug(String format, Object arg1, Object arg2) {
        this.origin().debug(format, arg1, arg2);
    }

    default void debug(String format, Object... arguments) {
        this.origin().debug(format, arguments);
    }

    default void debug(String msg, Throwable t) {
        this.origin().debug(msg, t);
    }

    default boolean isDebugEnabled(Marker marker) {
        return this.origin().isDebugEnabled(marker);
    }

    default void debug(Marker marker, String msg) {
        this.origin().debug(marker, msg);
    }

    default void debug(Marker marker, String format, Object arg) {
        this.origin().debug(marker, format, arg);
    }

    default void debug(Marker marker, String format, Object arg1, Object arg2) {
        this.origin().debug(marker, format, arg1, arg2);
    }

    default void debug(Marker marker, String format, Object... arguments) {
        this.origin().debug(marker, format, arguments);
    }

    default void debug(Marker marker, String msg, Throwable t) {
        this.origin().debug(marker, msg, t);
    }

    default boolean isInfoEnabled() {
        return this.origin().isInfoEnabled();
    }

    default void info(String msg) {
        this.origin().info(msg);
    }

    default void info(String format, Object arg) {
        this.origin().info(format, arg);
    }

    default void info(String format, Object arg1, Object arg2) {
        this.origin().info(format, arg1, arg2);
    }

    default void info(String format, Object... arguments) {
        this.origin().info(format, arguments);
    }

    default void info(String msg, Throwable t) {
        this.origin().info(msg, t);
    }

    default boolean isInfoEnabled(Marker marker) {
        return this.origin().isInfoEnabled(marker);
    }

    default void info(Marker marker, String msg) {
        this.origin().info(marker, msg);
    }

    default void info(Marker marker, String format, Object arg) {
        this.origin().info(marker, format, arg);
    }

    default void info(Marker marker, String format, Object arg1, Object arg2) {
        this.origin().info(marker, format, arg1, arg2);
    }

    default void info(Marker marker, String format, Object... arguments) {
        this.origin().info(marker, format, arguments);
    }

    default void info(Marker marker, String msg, Throwable t) {
        this.origin().info(marker, msg, t);
    }

    default boolean isWarnEnabled() {
        return this.origin().isWarnEnabled();
    }

    default void warn(String msg) {
        this.origin().warn(msg);
    }

    default void warn(String format, Object arg) {
        this.origin().warn(format, arg);
    }

    default void warn(String format, Object arg1, Object arg2) {
        this.origin().warn(format, arg1, arg2);
    }

    default void warn(String format, Object... arguments) {
        this.origin().warn(format, arguments);
    }

    default void warn(String msg, Throwable t) {
        this.origin().warn(msg, t);
    }

    default boolean isWarnEnabled(Marker marker) {
        return this.origin().isWarnEnabled(marker);
    }

    default void warn(Marker marker, String msg) {
        this.origin().warn(marker, msg);
    }

    default void warn(Marker marker, String format, Object arg) {
        this.origin().warn(marker, format, arg);
    }

    default void warn(Marker marker, String format, Object arg1, Object arg2) {
        this.origin().warn(marker, format, arg1, arg2);
    }

    default void warn(Marker marker, String format, Object... arguments) {
        this.origin().warn(marker, format, arguments);
    }

    default void warn(Marker marker, String msg, Throwable t) {
        this.origin().warn(marker, msg, t);
    }

    default boolean isErrorEnabled() {
        return this.origin().isErrorEnabled();
    }

    default void error(String msg) {
        this.origin().error(msg);
    }

    default void error(String format, Object arg) {
        this.origin().error(format, arg);
    }

    default void error(String format, Object arg1, Object arg2) {
        this.origin().error(format, arg1, arg2);
    }

    default void error(String format, Object... arguments) {
        this.origin().error(format, arguments);
    }

    default void error(String msg, Throwable t) {
        this.origin().error(msg, t);
    }

    default boolean isErrorEnabled(Marker marker) {
        return this.origin().isErrorEnabled(marker);
    }

    default void error(Marker marker, String msg) {
        this.origin().error(marker, msg);
    }

    default void error(Marker marker, String format, Object arg) {
        this.origin().error(marker, format, arg);
    }

    default void error(Marker marker, String format, Object arg1, Object arg2) {
        this.origin().error(marker, format, arg1, arg2);
    }

    default void error(Marker marker, String format, Object... arguments) {
        this.origin().error(marker, format, arguments);
    }

    default void error(Marker marker, String msg, Throwable t) {
        this.origin().error(marker, msg, t);
    }

    //supplier----------------------------

    default void trace(String format, Supplier<?> arg) {
        if(this.origin().isTraceEnabled())
            this.origin().trace(format, arg.get());
    }

    default void trace(String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isTraceEnabled())
            this.origin().trace(format, arg1.get(), arg2.get());
    }

    default void trace(String format, Supplier... arguments) {
        if(this.origin().isTraceEnabled()) {
            this.origin().trace(format, SUPPLIER_CONV.apply(arguments));
        }
    }

    default void trace(Supplier<String> msgSupplier) {
        if(this.origin().isTraceEnabled())
            this.origin().trace(msgSupplier.get());
    }

    default void trace(Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isTraceEnabled())
            this.origin().trace(msgSupplier.get(), t);
    }

    default void trace(Marker marker, String format, Supplier<?> arg) {
        if(this.origin().isTraceEnabled())
            this.origin().trace(marker, format, arg.get());
    }

    default void trace(Marker marker, String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isTraceEnabled())
            this.origin().trace(marker, format, arg1.get(), arg2.get());
    }

    default void trace(Marker marker, String format, Supplier... arguments) {
        if(this.origin().isTraceEnabled(marker)) {
            this.origin().trace(marker, format, SUPPLIER_CONV.apply(arguments));
        }
    }

    default void trace(Marker marker, Supplier<String> msgSupplier) {
        if(this.origin().isTraceEnabled(marker)) {
            this.origin().trace(marker, msgSupplier.get());
        }
    }

    default void trace(Marker marker, Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isTraceEnabled(marker)) {
            this.origin().trace(marker, msgSupplier.get(), t);
        }
    }

    default void debug(String format, Supplier<?> arg1) {
        if(this.origin().isDebugEnabled()) {
            this.origin().debug(format, arg1.get());
        }
    }

    default void debug(String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isDebugEnabled()) {
            this.origin().debug(format, arg1.get(), arg2.get());
        }
    }

    default void debug(String format, Supplier... arg) {
        if(this.origin().isDebugEnabled()) {
            this.origin().debug(format, SUPPLIER_CONV.apply(arg));
        }
    }

    default void debug(Supplier<String> msgSupplier) {
        if(this.origin().isDebugEnabled()) {
            this.origin().debug(msgSupplier.get());
        }
    }

    default void debug(Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isDebugEnabled()) {
            this.origin().debug(msgSupplier.get(), t);
        }
    }

    default void debug(Marker marker, String format, Supplier<?> arg1) {
        if(this.origin().isDebugEnabled(marker)) {
            this.origin().debug(marker, format, arg1.get());
        }
    }

    default void debug(Marker marker, String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isDebugEnabled(marker))
            this.origin().debug(marker, format, arg1.get(), arg2.get());
    }

    default void debug(Marker marker, String format, Supplier... arg) {
        if(this.origin().isDebugEnabled(marker))
            this.origin().debug(marker, format, SUPPLIER_CONV.apply(arg));
    }

    default void debug(Marker marker, Supplier<String> msgSupplier) {
        if(this.origin().isDebugEnabled(marker))
            this.origin().debug(marker, msgSupplier.get());
    }

    default void debug(Marker marker, Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isDebugEnabled(marker))
            this.origin().debug(marker, msgSupplier.get(), t);
    }

    default void info(String format, Supplier<?> arg1) {
        if(this.origin().isInfoEnabled())
            this.origin().info(format, arg1.get());
    }

    default void info(String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isInfoEnabled())
            this.origin().info(format, arg1.get(), arg2.get());
    }

    default void info(String format, Supplier... arg) {
        if(this.origin().isInfoEnabled())
            this.origin().info(format, SUPPLIER_CONV.apply(arg));
    }

    default void info(Supplier<String> msgSupplier) {
        if(this.origin().isInfoEnabled())
            this.origin().info(msgSupplier.get());
    }

    default void info(Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isInfoEnabled())
            this.origin().info(msgSupplier.get(), t);
    }

    default void info(Marker marker, String format, Supplier<?> arg1) {
        if(this.origin().isInfoEnabled(marker))
            this.origin().info(marker, format, arg1.get());
    }

    default void info(Marker marker, String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isInfoEnabled(marker))
            this.origin().info(marker, format, arg1.get(), arg2.get());
    }

    default void info(Marker marker, String format, Supplier... arg) {
        if(this.origin().isInfoEnabled(marker))
            this.origin().info(marker, format, SUPPLIER_CONV.apply(arg));
    }

    default void info(Marker marker, Supplier<String> msgSupplier) {
        if(this.origin().isInfoEnabled(marker))
            this.origin().info(marker, msgSupplier.get());
    }

    default void info(Marker marker, Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isInfoEnabled(marker))
            this.origin().info(marker, msgSupplier.get(), t);
    }

    default void warn(String format, Supplier<?> arg1) {
        if(this.origin().isWarnEnabled())
            this.origin().warn(format, arg1.get());
    }

    default void warn(String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isWarnEnabled())
            this.origin().warn(format, arg1.get(), arg2.get());
    }

    default void warn(String format, Supplier... arg) {
        if(this.origin().isWarnEnabled())
            this.origin().warn(format, SUPPLIER_CONV.apply(arg));
    }

    default void warn(Supplier<String> msgSupplier) {
        if(this.origin().isWarnEnabled())
            this.origin().warn(msgSupplier.get());
    }

    default void warn(Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isWarnEnabled())
            this.origin().warn(msgSupplier.get(), t);
    }

    default void warn(Marker marker, String format, Supplier<?> arg1) {
        if(this.origin().isWarnEnabled(marker))
            this.origin().warn(marker, format, arg1.get());
    }

    default void warn(Marker marker, String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isWarnEnabled(marker))
            this.origin().warn(marker, format, arg1.get(), arg2.get());
    }

    default void warn(Marker marker, String format, Supplier... arg) {
        if(this.origin().isWarnEnabled(marker))
            this.origin().warn(marker, format, SUPPLIER_CONV.apply(arg));
    }

    default void warn(Marker marker, Supplier<String> msgSupplier) {
        if(this.origin().isWarnEnabled(marker))
            this.origin().warn(marker, msgSupplier.get());
    }

    default void warn(Marker marker, Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isWarnEnabled(marker))
            this.origin().warn(marker, msgSupplier.get(), t);
    }

    default void error(String format, Supplier<?> arg1) {
        if(this.origin().isErrorEnabled())
            this.origin().error(format, arg1.get());
    }

    default void error(String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isErrorEnabled())
            this.origin().error(format, arg1.get(), arg2.get());
    }

    default void error(String format, Supplier... arg) {
        if(this.origin().isErrorEnabled())
            this.origin().error(format, SUPPLIER_CONV.apply(arg));
    }

    default void error(Supplier<String> msgSupplier) {
        if(this.origin().isErrorEnabled())
            this.origin().error(msgSupplier.get());
    }

    default void error(Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isErrorEnabled())
            this.origin().error(msgSupplier.get(), t);
    }

    default void error(Marker marker, String format, Supplier<?> arg1) {
        if(this.origin().isErrorEnabled(marker))
            this.origin().error(marker, format, arg1.get());
    }

    default void error(Marker marker, String format, Supplier<?> arg1, Supplier<?> arg2) {
        if(this.origin().isErrorEnabled(marker))
            this.origin().error(marker, format, arg1.get(), arg2.get());
    }

    default void error(Marker marker, String format, Supplier... arg) {
        if(this.origin().isErrorEnabled(marker))
            this.origin().error(marker, format, SUPPLIER_CONV.apply(arg));
    }

    default void error(Marker marker, Supplier<String> msgSupplier) {
        if(this.origin().isErrorEnabled(marker))
            this.origin().error(marker, msgSupplier.get());
    }

    default void error(Marker marker, Supplier<String> msgSupplier, Throwable t) {
        if(this.origin().isErrorEnabled(marker))
            this.origin().error(marker, msgSupplier.get(), t);
    }
}