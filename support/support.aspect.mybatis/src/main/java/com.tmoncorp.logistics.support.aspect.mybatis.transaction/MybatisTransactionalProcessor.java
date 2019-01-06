package com.tmoncorp.logistics.support.aspect.mybatis.transaction;

import io.codebit.support.aspect.transation.TransationalProcessor;
import org.apache.ibatis.session.SqlSession;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

@Aspect
public class MybatisTransactionalProcessor implements IMessageHandler {

    private static Logger log = LoggerFactory.getLogger(MybatisTransactionalProcessor.class);
    // && !within(TransationalProcessor)
    @Pointcut("call(org.apache.ibatis.session.SqlSession org.apache.ibatis.session.SqlSessionFactory.openSession(..))  && !within(in.java.support.aspect.mybatis.transaction.MybatisTransactionalProcessor)")
    private void openSessionPointcut(){
    }

    @Pointcut("call(void org.apache.ibatis.session.SqlSession.close(..)) && !within(in.java.support.aspect.mybatis.transaction.MybatisTransactionalProcessor)")
    private void closePointcut(){
    }

    @Pointcut("call(void org.apache.ibatis.session.SqlSession.commit(..)) && !within(in.java.support.aspect.mybatis.transaction.MybatisTransactionalProcessor)")
    private void commitPointcut(){
    }

//    @Pointcut("execution(void org.apache.ibatis.session.SqlSession.rollback()) && !within(TransationalProcessor)")
//    private void rollbackPointcut(){
//    }


    @Around("openSessionPointcut()")
    public Object open(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("session open");
        //Transactional annotaion , map<Session 객체, commit callback or rollback callback>
        if(!TransationalProcessor.isTransaction()) {
            return joinPoint.proceed();
        } else {
            //어노테이션은 하나인데 세션은 여러개 열릴 수 있음
//            SqlSessionFactory sessionFactory = (SqlSessionFactory) joinPoint.getThis();
            Object[] args = joinPoint.getArgs();

            SqlSession session = getSqlSession(joinPoint, args);
            Callable<Object> commit = () -> {
                session.commit();
                return true;
            };
            Callable<Object> rollback = () -> {
                session.rollback();
                return true;
            };
            Callable<Object> close = () -> {
                session.close();
                return true;
            };

            TransationalProcessor.addOperation(commit, rollback, close);
            return session;
        }
    }

    @Around("commitPointcut() || closePointcut()")
    public Object commit(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("session commit or close");
        if(TransationalProcessor.isTransaction()) {
            return null;
        }
        return joinPoint.proceed();
    }

    private SqlSession getSqlSession(ProceedingJoinPoint joinPoint, Object[] args) throws Throwable {
        SqlSession session;
        if(args.length == 1 && args[0] instanceof Boolean) {
            args[0] = false;
            session = (SqlSession) joinPoint.proceed(args);
        } else if (args.length == 2 && args[1] instanceof Boolean) {
            args[1] = false;
            session = (SqlSession) joinPoint.proceed(args);
        }else {
            session = (SqlSession) joinPoint.proceed();
        }
        return session;
    }

    @Override
    public boolean handleMessage(IMessage message) throws AbortException {
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
