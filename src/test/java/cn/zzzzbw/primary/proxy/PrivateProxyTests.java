package cn.zzzzbw.primary.proxy;

import cn.zzzzbw.primary.proxy.reflect.PrivateProxy;
import cn.zzzzbw.primary.proxy.reflect.PrivateProxyInvocationHandler;
import cn.zzzzbw.primary.proxy.service.HelloService;
import cn.zzzzbw.primary.proxy.service.impl.HelloServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author by ZHANGBOWEN469
 * @since 2020/10/13 14:29
 */
@Slf4j
public class PrivateProxyTests {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PrivateProxyInvocationHandler handler = new PrivateProxyInvocationHandler(new HelloServiceImpl()) {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info("PrivateProxyInvocationHandler!");
                return method.invoke(getSubject(), args);
            }
        };

        Object o = PrivateProxy.newProxyInstance(ClassLoader.getSystemClassLoader(), HelloServiceImpl.class.getInterfaces(), handler);
        log.info("{}", o);

        HelloService helloService = (HelloService) o;
        helloService.hello("hello");

        Method primaryHello = helloService.getClass().getDeclaredMethod("privateHello", Integer.class);
        primaryHello.setAccessible(true);
        Object invoke = primaryHello.invoke(helloService, 10);
        log.info("privateHello result: {}", invoke);
    }
}
