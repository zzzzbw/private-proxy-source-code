package cn.zzzzbw.primary.proxy;

import cn.zzzzbw.primary.proxy.service.HelloService;
import cn.zzzzbw.primary.proxy.spring.PrivateProxyAdvisorAutoProxyCreator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author by zzzzbw
 * @since 2020/09/15 10:00
 */
@Slf4j
@SpringBootTest
public class HelloTests {

    /**
     * 将PrimaryProxyAdvisorAutoProxyCreator添加到AopConfigUtils中
     */
    static {
        try {
            Field apc_priority_list = AopConfigUtils.class.getDeclaredField("APC_PRIORITY_LIST");
            apc_priority_list.setAccessible(true);
            List<Class<?>> o = (List<Class<?>>) apc_priority_list.get(AopConfigUtils.class);
            o.add(PrivateProxyAdvisorAutoProxyCreator.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private HelloService helloService;

    @Test
    public void helloService() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        helloService.hello("hello");

        Method privateHello = helloService.getClass().getDeclaredMethod("privateHello", Integer.class);
        privateHello.setAccessible(true);
        Object invoke = privateHello.invoke(helloService, 10);
        log.info("privateHello result: {}", invoke);
    }

}
