package io.codebit.support.bci;

import org.aspectj.bridge.*;
import org.aspectj.lang.Aspects;
import org.aspectj.weaver.loadtime.DefaultMessageHandler;
import org.aspectj.weaver.tools.ISupportsMessageContext;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;


public class AspectMessagesHandler extends DefaultMessageHandler implements IMessageHandler, ISupportsMessageContext {

//    private IMessageHandler messageHandler;
//    private IMessageContext messageContext;

    public AspectMessagesHandler() {
        super();
    }

    Event<String> debitEvent;

    public boolean handleMessage(@Observes IMessage message) throws AbortException {
//        ISourceLocation location = message.getSourceLocation();
//        String sourceFileName = location.getSourceFileName();
//        System.out.println(sourceFileName);
//        System.out.println("VVVVVVVVVVVVVV  -- "+message);
//        return super.handleMessage(message);
        if(message.getKind().equals(IMessage.WEAVEINFO)) {
            WeaveMessage weaveMessage = (WeaveMessage) message;
            try {
                Class<?> aspectClass = Class.forName(weaveMessage.getAspectname());
                Object o = Aspects.aspectOf(aspectClass);
//                aspectClass.getDeclaredMethod("handleMessage", IMessage.class);
                if(IMessageHandler.class.isAssignableFrom(aspectClass)) {
                    System.out.println(message.getThrown());
                }
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }

        return SYSTEM_ERR.handleMessage(message);
    }

    @Override
    public boolean isIgnoring(IMessage.Kind kind) {

//        return super.isIgnoring(kind);
        return false;
    }

    /*@Override
    public void dontIgnore(IMessage.Kind kind) {
        super.dontIgnore(kind);
    }

    @Override
    public void ignore(IMessage.Kind kind) {
        super.ignore(kind);
    }*/

    @Override
    public void setMessageContext(IMessageContext messageContext) {
//        this.messageContext = messageContext;
//        messageHandler = ((WeavingAdaptor)messageContext).getMessageHolder();
    }
}
