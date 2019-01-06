package io.codebit.support.aspect.transation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import javax.transaction.TransactionRequiredException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

//https://doanduyhai.wordpress.com/2011/12/05/advanced-aspectj-part-i-instanciation-model/

/**
 * JSR907 Transactional 구현을 위한 aspect class
 * @see javax.transaction.Transactional
 */
@Aspect("percflow(topLevelPointcut())")
public class TransationalProcessor {

    public static class TransactionalOperation {
        Transactional transactional;
        List<Operation> operations =  new ArrayList<Operation>();

        public static class Operation {
            private Callable<Object> commit;
            private Callable<Object> rollback;
            private Callable<Object> close;
            public Operation(Callable<Object> commit, Callable<Object> rollback, Callable<Object> close){
                this.commit = commit;
                this.rollback = rollback;
                this.close = close;
            }
        }

        public TransactionalOperation(Transactional transactional) {
            this.transactional = transactional;
        }
    }

    public static final ThreadLocal<ConcurrentLinkedDeque<TransactionalOperation>> OPERATIONS = ThreadLocal.withInitial((Supplier<ConcurrentLinkedDeque<TransactionalOperation>>) () -> new ConcurrentLinkedDeque());

    private boolean isTransactionStart = false;
    private boolean isRollback = false;


    public static void addOperation(Callable commit, Callable rollback, Callable close) {
        ConcurrentLinkedDeque<TransactionalOperation> operations = OPERATIONS.get();
        TransactionalOperation last = operations.getLast();
        last.operations.add(new TransactionalOperation.Operation(commit, rollback, close));
    }

    public static boolean isTransaction() {
        ConcurrentLinkedDeque<TransactionalOperation> operations = OPERATIONS.get();
        if(operations.isEmpty()) {
            return false;
        }
        TransactionalOperation last = operations.peekLast();
        Transactional.TxType txType = last.transactional.value();
        if(txType.equals(Transactional.TxType.NOT_SUPPORTED)) {
            return false;
        }
        return true;
    }

    @Pointcut("execution(@javax.transaction.Transactional+ * *(..))")
    private void methodPointcut(){
    }

    @Pointcut("execution(* (@javax.transaction.Transactional *+).*(..))")
    private void typePointcut(){
    }

//    @Pointcut("(methodPointcut() || typePointcut()) && !within(TransationalProcessor)")
    @Pointcut("methodPointcut() || typePointcut()")
    private void methodAndTypePointcut(){
    }

    @Pointcut("methodAndTypePointcut() && !cflowbelow(methodAndTypePointcut())")
    private void topLevelPointcut(){
    }

    @Around("methodAndTypePointcut()")
    public Object transational(ProceedingJoinPoint joinPoint) throws Throwable {
        Transactional transactional;
        Signature signature = joinPoint.getSignature();
        if(signature instanceof MethodSignature) {
            transactional = ((MethodSignature)signature).getMethod().getAnnotation(Transactional.class);
        }else {
            transactional = (Transactional) signature.getDeclaringType().getAnnotation(Transactional.class);
        }

        Transactional.TxType txType = transactional.value();
        /*
        https://docs.oracle.com/javaee/6/tutorial/doc/bncij.html
        REQUIRED : 부모 트랜잭션 내에서 실행하며 부모 트랜잭션이 없을 경우 새로운 트랜잭션을 생성합니다.
        REQUIRES_NEW : 부모 트랜잭션을 무시하고 무조건 새로운 트랜잭션이 생성되도록 합니다.
        SUPPORT : 부모 트랜잭션 내에서 실행하며 부모 트랜잭션이 없을 경우 nontransactionally로 실행됩니다.
        MANDATORY : 부모 트랜잭션 내에서 실행되며 부모 트랜잭션이 없을 경우 예외가 발생됩니다.
        NOT_SUPPORTED : nontransactionally로 실행하며 부모 트랜잭션 내에서 실행될 경우 무시(일시정지) 됩니다.(부모 트랜젝션이 있어도 non transaction)
        NEVER : nontransactionally로 실행되며 부모 트랜잭션이 존재한다면 예외가 발생합니다.
        NESTED : spring 에만 있음 - 해당 메서드가 부모 트랜잭션에서 진행될 경우 별개로 커밋되거나 롤백될 수 있습니다. 둘러싼 트랜잭션이 없을 경우 REQUIRED와 동일하게 작동합니다.
        */

        ConcurrentLinkedDeque<TransactionalOperation> operations = OPERATIONS.get();
        Object proceed = null;
        try {
            if(txType.equals(Transactional.TxType.REQUIRED) && operations.isEmpty()) {
                isTransactionStart = true;
                TransactionalOperation operation = new TransactionalOperation(transactional);
                operations.addLast(operation);
            }else if(txType.equals(Transactional.TxType.REQUIRES_NEW)) {
                isTransactionStart = true;
                TransactionalOperation operation = new TransactionalOperation(transactional);
                operations.addLast(operation);
            }else if(txType.equals(Transactional.TxType.MANDATORY) && operations.isEmpty()) {
                throw new TransactionRequiredException();
            }else if(txType.equals(Transactional.TxType.NOT_SUPPORTED)) {
                TransactionalOperation operation = new TransactionalOperation(transactional);
                operations.addLast(operation);
            }else if(txType.equals(Transactional.TxType.NEVER) && !operations.isEmpty()) {
                throw new TransactionRequiredException();
            }
            proceed = joinPoint.proceed();
        } catch (TransactionRequiredException e) {
            operations.forEach(t->{
                t.operations.forEach(o->{
                    try {
                        o.rollback.call();
                        o.close.call();
                    } catch (Exception e1) {
                        //e1.printStackTrace();
                    }
                });
            });
            operations.clear();
            throw e;
        } catch (Throwable e) {
            //dont가 우선순위가 높음
            Class[] dontRollbackOn = transactional.dontRollbackOn();
            Class[] rollbackOn = transactional.rollbackOn();

            if(dontRollbackOn.length > 0) {
                if(hasException(dontRollbackOn, e.getClass())) {
                    //커밋
                    throw e;
                }
            }

            if(rollbackOn.length > 0) {
                if(hasException(rollbackOn, e.getClass())) {
                    //롤백
                    isRollback = true;
                }
                throw e;
            }
            //둘다 없으면 그냥 롤백
            isRollback = true;
            throw e;
        } finally {
            if(isTransactionStart) {
                isTransactionStart = false;
//            operations.peekLast(); //삭제하지 않고 가져오기만 함 큐가 비어있으면 null을 반환
                TransactionalOperation last = operations.pollLast();    //마지막을 가져오고 삭제함 없으면 null 반환
                if(last != null) {
//                Transactional.TxType txType = last.transactional.value();
                    if(!txType.equals(Transactional.TxType.NOT_SUPPORTED)) {
                        if(isRollback) {
                            last.operations.forEach(o->{
                                try {
                                    o.rollback.call();
                                    o.close.call();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            });

                        }else {
                            last.operations.forEach(o->{
                                try {
                                    o.commit.call();
                                    o.close.call();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            });
                        }
                        if(txType.equals(Transactional.TxType.REQUIRES_NEW)) {
                            isRollback = false;
                        }
                    }
                }
            }
        }
        return proceed;
    }

    private boolean hasException(Class[] classes, Class klass) {
        for (Class cla : classes) {
            if(cla.isAssignableFrom(klass)) {
                return true;
            }
        }
        return false;
    }
}
