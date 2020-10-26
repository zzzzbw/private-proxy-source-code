package cn.zzzzbw.primary.proxy.spring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author by zzzzbw
 * @since 2020/09/23 10:09
 */
@Slf4j
@Aspect
@Component
public class TransactionalAop {

    @Around("@within(org.springframework.transaction.annotation.Transactional)")
    public Object recordLog(ProceedingJoinPoint p) throws Throwable {
        log.info("Transaction start!");

        Object result;
        try {
            result = p.proceed();
        } catch (Exception e) {
            log.info("Transaction rollback!");
            throw new Throwable(e);
        }
        log.info("Transaction commit!");

        return result;
    }
}
