package cn.zzzzbw.primary.proxy.reflect;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author by zzzzbw
 * @since 2020/09/15 10:12
 */
@Slf4j
public class TransactionalHandler extends PrivateProxyInvocationHandler {

    public TransactionalHandler(Object subject) {
        super(subject);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Transaction start!");

        Object result;
        try {
            result = method.invoke(getSubject(), args);
        } catch (Exception e) {
            log.info("Transaction rollback!");
            throw new Throwable(e);
        }
        log.info("Transaction commit!");

        return result;
    }
}
