package cn.zzzzbw.primary.proxy.reflect;

import lombok.Getter;

import java.lang.reflect.InvocationHandler;

/**
 * @author by zzzzbw
 * @since 2020/09/18 10:07
 */
@Getter
public abstract class PrivateProxyInvocationHandler implements InvocationHandler {

    private final Object subject;

    public PrivateProxyInvocationHandler(Object subject) {
        this.subject = subject;
    }
}
