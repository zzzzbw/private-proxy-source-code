package cn.zzzzbw.primary.proxy;

import cn.zzzzbw.primary.proxy.reflect.*;
import cn.zzzzbw.primary.proxy.service.HelloService;
import cn.zzzzbw.primary.proxy.service.impl.HelloServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author by zzzzbw
 * @since 2020/09/18 11:08
 */
@Slf4j
public class PrivateProxyGeneratorTests {

    public static void main(String[] args) throws IOException {
        // 1 生成java源码
        String packageName = "cn.zzzzbw.primary.proxy.reflect";
        String clazzName = "$Proxy0";
        String proxyName = packageName + "." + clazzName;
        Class<?>[] interfaces = HelloServiceImpl.class.getInterfaces();
        PrivateProxyInvocationHandler h = new TransactionalHandler(new HelloServiceImpl());
        String src = PrivateProxyGenerator.generateProxyClass(proxyName, interfaces, h);

        // 2 保存成java文件
        String filePath = PrivateProxy.class.getResource("/").getPath();
        String clzFilePath = filePath + packageName.replace(".", "/") + "/" + clazzName + ".java";
        log.info("clzFilePath: {}", clzFilePath);
        File f = new File(clzFilePath);

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(src);
            fw.flush();
        }
    }
}
