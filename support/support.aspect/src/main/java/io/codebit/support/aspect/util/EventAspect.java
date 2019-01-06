package io.codebit.support.aspect.util;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class EventAspect implements IMessageHandler{

    @Around("execution(@io.codebit.support.aspect.annotation.EventHandle * * (..))")
    public Object execution(final ProceedingJoinPoint point) {
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDdd ");
        return  null;
    }

    @Override
    public boolean handleMessage(IMessage message) throws AbortException {
        System.out.println(message);
        return false;
    }

    @Override
    public boolean isIgnoring(IMessage.Kind kind) {
        return false;
    }

    @Override
    public void dontIgnore(IMessage.Kind kind) {

    }

    @Override
    public void ignore(IMessage.Kind kind) {

    }
}
