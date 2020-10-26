package cn.zzzzbw.primary.proxy;

import cn.zzzzbw.primary.proxy.spring.PrivateProxyAdvisorAutoProxyCreator;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

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

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
