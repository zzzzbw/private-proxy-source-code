package cn.zzzzbw.primary.proxy.spring;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.*;
import org.springframework.stereotype.Component;

/**
 * @author by zzzzbw
 * @since 2020/09/15 15:10
 */
// @Component(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME)
public class PrivateProxyAdvisorAutoProxyCreator extends AnnotationAwareAspectJAutoProxyCreator {

    @Override
    protected Object createProxy(Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {
        // 由于AutoProxyUtils.exposeTargetClass不是public方法, 且与本文功能无关, 这里就不作改造, 直接注释掉
        /*
        if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
            AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
        }
        */

        ProxyFactory proxyFactory = new ProxyFactory();
        // 设置aopProxyFactory为PrimaryAopProxyFactory
        proxyFactory.setAopProxyFactory(new PrimaryAopProxyFactory());
        proxyFactory.copyFrom(this);

        if (!proxyFactory.isProxyTargetClass()) {
            if (shouldProxyTargetClass(beanClass, beanName)) {
                proxyFactory.setProxyTargetClass(true);
            } else {
                evaluateProxyInterfaces(beanClass, proxyFactory);
            }
        }

        Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
        proxyFactory.addAdvisors(advisors);
        proxyFactory.setTargetSource(targetSource);
        customizeProxyFactory(proxyFactory);


        proxyFactory.setFrozen(isFrozen());
        if (advisorsPreFiltered()) {
            proxyFactory.setPreFiltered(true);
        }

        return proxyFactory.getProxy(getProxyClassLoader());
    }

    class PrimaryAopProxyFactory implements AopProxyFactory {
        @Override
        public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
            return new PrivateAopProxy(config);
        }
    }
}
