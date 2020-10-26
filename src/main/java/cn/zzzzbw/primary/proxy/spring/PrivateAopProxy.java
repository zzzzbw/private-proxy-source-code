package cn.zzzzbw.primary.proxy.spring;

import cn.zzzzbw.primary.proxy.reflect.PrivateProxy;
import cn.zzzzbw.primary.proxy.reflect.TransactionalHandler;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author by zzzzbw
 * @since 2020/09/15 15:52
 */
public class PrivateAopProxy implements AopProxy {
    private final AdvisedSupport advised;

    /**
     * 构造方法
     * <p>
     * 直接复制JdkDynamicAopProxy构造方法逻辑
     *
     * @param config
     * @throws AopConfigException
     */
    public PrivateAopProxy(AdvisedSupport config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
            throw new AopConfigException("No advisors and no TargetSource specified");
        }
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        // 获取目标类接口
        Class<?>[] interfaces = this.advised.getTargetClass().getInterfaces();
        TransactionalHandler handler;
        try {
            // 生成切面, 这里写死为TransactionalHandler
            handler = new TransactionalHandler(this.advised.getTargetSource().getTarget());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 返回代理类对象
        return PrivateProxy.newProxyInstance(classLoader, interfaces, handler);
    }
}
